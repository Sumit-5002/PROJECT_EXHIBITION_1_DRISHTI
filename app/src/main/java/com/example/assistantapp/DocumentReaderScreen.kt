package com.example.assistantapp

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.speech.tts.TextToSpeech
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun DocumentReaderScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var hasPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var ocrResult by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }

    // Initialize TTS with optimized settings for document reading
    LaunchedEffect(context) {
        tts.value = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.value?.language = Locale.US
                tts.value?.setSpeechRate(0.8f) // Optimized for document reading
                tts.value?.setPitch(1.1f) // Clear pitch for better comprehension
                // Try to use the best available voice engine
                try {
                    val voices = tts.value?.voices
                    val bestVoice = voices?.find { voice ->
                        voice.name.contains("enhanced", ignoreCase = true) ||
                        voice.name.contains("neural", ignoreCase = true) ||
                        voice.name.contains("premium", ignoreCase = true) ||
                        voice.name.contains("high", ignoreCase = true)
                    }
                    if (bestVoice != null) {
                        tts.value?.voice = bestVoice
                    } else {
                        tts.value?.voice = tts.value?.defaultVoice
                    }
                } catch (e: Exception) {
                    // Fallback to default voice
                    tts.value?.voice = tts.value?.defaultVoice
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts.value?.stop()
            tts.value?.shutdown()
            cameraExecutor.shutdown()
        }
    }

    // Welcome message
    LaunchedEffect(Unit) {
        if (tts.value != null) {
            tts.value?.speak(
                "Document Reader activated. Point your camera at any text document, sign, or book page. " +
                "I will read the text aloud for you.",
                TextToSpeech.QUEUE_FLUSH, null, null
            )
        }
    }
    
    

    // Request permissions if not granted
    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            ActivityCompat.requestPermissions(
                (context as Activity),
                arrayOf(Manifest.permission.CAMERA),
                200
            )
        }
    }
    
    // Update permission status
    LaunchedEffect(Unit) {
        while (true) {
            hasPermissions = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
            delay(1000)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (hasPermissions) {
            // Full-screen camera preview
            DocumentReaderCamera(
                isProcessing = isProcessing,
                ocrResult = ocrResult,
                onImageCaptured = { bitmap: Bitmap ->
                    // Skip if already processing to prevent loops - sequential processing
                    if (!isProcessing) {
                        android.util.Log.d("DocumentReader", "Processing captured image: ${bitmap.width}x${bitmap.height}")
                        capturedImage = bitmap
                        isProcessing = true
                        coroutineScope.launch {
                        try {
                            ocrResult = ""
                            tts.value?.speak("Scanning document. Please wait while I read the text.", TextToSpeech.QUEUE_FLUSH, null, null)
                            
                            var hasReceivedText = false
                            var fullText = ""
                            
                            sendFrameToGemini2AI(bitmap, 
                                onPartialResult = { partialResult ->
                                    android.util.Log.d("DocumentReader", "Received partial result: $partialResult")
                                    if (partialResult.isNotBlank()) {
                                        fullText += partialResult + " " // Add space between partial results
                                        hasReceivedText = true
                                        // Update UI with partial results for better feedback
                                        ocrResult = fullText.trim()
                                    }
                                }, 
                                onError = { error ->
                                    android.util.Log.e("DocumentReader", "OCR Error: $error")
                                    tts.value?.speak("Error reading document. Please try again.", TextToSpeech.QUEUE_FLUSH, null, null)
                                    isProcessing = false
                                },
                                onComplete = {
                                    android.util.Log.d("DocumentReader", "OCR processing completed")
                                    // Clean and format text properly
                                    val cleanText = fullText
                                        .replace(Regex("[*#@$%^&+=]"), "") // Remove special characters
                                        .replace(Regex("\\s+"), " ") // Normalize whitespace
                                        .trim()
                                    
                                    ocrResult = cleanText
                                    
                                    // If no response received, provide feedback
                                    if (!hasReceivedText || cleanText.isBlank()) {
                                        tts.value?.speak("No text detected. Please point the camera at text and try again.", TextToSpeech.QUEUE_FLUSH, null, null)
                                        isProcessing = false
                                    } else if (cleanText.isNotBlank()) {
                                        // Read the complete text with proper formatting
                                        val readingText = "Document text detected. Reading now: $cleanText"
                                        tts.value?.speak(readingText, TextToSpeech.QUEUE_FLUSH, null, null)
                                        // Wait for complete text reading before allowing next scan
                                        coroutineScope.launch {
                                            // Improved time calculation based on text length and reading speed
                                            val wordsPerMinute = 200 // Average reading speed for TTS
                                            val wordsInText = fullText.split("\\s+".toRegex()).size
                                            val baseReadingTime = (wordsInText * 60 * 1000) / wordsPerMinute // Convert to milliseconds
                                            
                                            // Add buffer time for processing and user comprehension
                                            val processingBuffer = 3000L // 3 seconds buffer
                                            val comprehensionBuffer = (baseReadingTime * 0.2).toLong() // 20% extra for comprehension
                                            
                                            val totalReadingTime = baseReadingTime + processingBuffer + comprehensionBuffer
                                            
                                            // Set reasonable bounds
                                            val minReadingTime = 5000L // Minimum 5 seconds
                                            val maxReadingTime = 60000L // Maximum 60 seconds
                                            val waitTime = totalReadingTime.coerceIn(minReadingTime, maxReadingTime)
                                            
                                            android.util.Log.d("DocumentReader", "Text length: ${fullText.length} chars, ${wordsInText} words, estimated reading time: ${waitTime}ms")
                                            
                                            delay(waitTime) // Wait for text to be read completely
                                            tts.value?.speak("Document reading complete. Ready for next scan.", TextToSpeech.QUEUE_ADD, null, null)
                                            delay(2000) // Additional wait before allowing next scan
                                            // Clear the text to allow next scan
                                            ocrResult = ""
                                            isProcessing = false
                                        }
                                    } else {
                                        isProcessing = false
                                    }
                                }
                            )
                            
                            // Fallback timeout in case completion callback doesn't work
                            delay(20000) // Wait 20 seconds as fallback for complete processing
                            if (isProcessing) {
                                android.util.Log.w("DocumentReader", "Processing timeout - stopping processing")
                                isProcessing = false
                            }
                            
                        } catch (e: Exception) {
                            android.util.Log.e("DocumentReader", "Exception in processing: ${e.message}")
                            tts.value?.speak("Failed to process document. Please try again.", TextToSpeech.QUEUE_FLUSH, null, null)
                            isProcessing = false
                        }
                        }
                    } else {
                        android.util.Log.d("DocumentReader", "Skipping image - already processing document")
                    }
                },
                cameraExecutor = cameraExecutor
            )
        }
        
        // Overlay UI elements
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top status
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Document Reader",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Point camera at text to read",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Bottom controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (isProcessing) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Processing document...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    
                    if (ocrResult.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Read Text:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = ocrResult,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    if (hasPermissions) {
                        // Automatic document reading - no manual buttons needed
                        Text(
                            text = "Document Reader is working automatically. Point camera at text!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        // Permission required message
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Camera Permission Required",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Please grant camera permission to use Document Reader",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentReaderCamera(
    isProcessing: Boolean,
    ocrResult: String,
    onImageCaptured: (Bitmap) -> Unit,
    cameraExecutor: ExecutorService
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val preview = Preview.Builder()
        .setTargetResolution(android.util.Size(1920, 1080)) // 1080p for maximum quality
        .build()
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY) // Maximum quality for text
            .setTargetResolution(android.util.Size(1920, 1080)) // 1080p for maximum quality
            .setJpegQuality(98) // Maximum quality for text clarity
            .build()
    }

    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageCapture
        )
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }
    

    // Sequential document processing - COMPLETELY STOP scanning until text is fully read
    LaunchedEffect(Unit) {
        while (true) {
            delay(9000) // Check every 5 seconds - longer delay to prevent continuous scanning
            // ONLY capture if not currently processing AND no text is being read
            if (!isProcessing && ocrResult.isBlank()) {
                val tempFile = File.createTempFile("document_reader", ".jpg", context.cacheDir)
                val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile)
                    .setMetadata(ImageCapture.Metadata().apply {
                        // Set metadata for better image quality
                        isReversedHorizontal = false
                        isReversedVertical = false
                    })
                    .build()
                imageCapture.takePicture(
                    outputOptions,
                    cameraExecutor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            val savedUri = outputFileResults.savedUri ?: return
                            val bitmap = BitmapFactory.decodeFile(savedUri.path)
                            if (bitmap != null) {
                                android.util.Log.d("DocumentReader", "Image captured successfully: ${bitmap.width}x${bitmap.height}")
                                onImageCaptured(bitmap)
                            } else {
                                android.util.Log.e("DocumentReader", "Failed to decode captured image")
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            android.util.Log.e("DocumentReader", "Image capture error: ${exception.message}")
                        }
                    }
                )
            }
        }
    }

    AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
}
