package com.example.assistantapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
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
fun VoiceAssistantScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    // Check permissions
    var hasPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    // State variables
    var chatResponse by remember { mutableStateOf("") }
    var currentImage by remember { mutableStateOf<Bitmap?>(null) }
    val ttsState = remember { mutableStateOf<TextToSpeech?>(null) }
    
    // Speech recognizer
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
    }

    // Initialize TTS
    LaunchedEffect(Unit) {
        ttsState.value = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                ttsState.value?.language = Locale.US
                ttsState.value?.setSpeechRate(0.8f)
            }
        }
    }
    

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            ttsState.value?.shutdown()
            speechRecognizer.destroy()
            cameraExecutor.shutdown()
        }
    }

    // Speech recognition listener
    LaunchedEffect(Unit) {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty() && matches[0].isNotBlank()) {
                    val spokenText = matches[0]
                    android.util.Log.d("VoiceAssistant", "Speech recognized: $spokenText")
                    coroutineScope.launch {
                        try {
                            val frame = currentImage
                            if (frame != null) {
                                chatResponse = ""
                                // Use image-based analysis when a frame is available
                                sendFrameToGeminiAI(
                                    frame,
                                    onPartialResult = { partial ->
                                        if (partial.isNotBlank()) {
                                            chatResponse = (chatResponse + partial).trim()
                                        }
                                    },
                                    onError = { err ->
                                        android.util.Log.e("VoiceAssistant", "AI Image Error: $err")
                                        if (chatResponse.isBlank()) {
                                            ttsState.value?.speak("Sorry, I couldn't see clearly. Please try again.", TextToSpeech.QUEUE_FLUSH, null, null)
                                        }
                                    },
                                    onComplete = {
                                        if (chatResponse.isNotBlank()) {
                                            ttsState.value?.speak(chatResponse, TextToSpeech.QUEUE_FLUSH, null, null)
                                        }
                                    }
                                )
                            } else {
                                // Fallback to text-only chat
                                chatResponse = sendMessageToGeminiAI(spokenText, "Camera view available")
                                if (chatResponse.isNotBlank()) {
                                    ttsState.value?.speak(chatResponse, TextToSpeech.QUEUE_FLUSH, null, null)
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("VoiceAssistant", "AI Error: ${e.message}")
                            ttsState.value?.speak("Sorry, I couldn't process that. Please try again.", TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                    }
                } else {
                    android.util.Log.w("VoiceAssistant", "No speech recognized")
                }
            }
            override fun onReadyForSpeech(params: Bundle?) {
                android.util.Log.d("VoiceAssistant", "Ready for speech")
            }
            override fun onBeginningOfSpeech() {
                android.util.Log.d("VoiceAssistant", "Speech started")
            }
            override fun onRmsChanged(rmsdB: Float) {
                if (rmsdB > 0) {
                    android.util.Log.d("VoiceAssistant", "Audio level: $rmsdB dB")
                }
            }
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                android.util.Log.d("VoiceAssistant", "Speech ended")
            }
            override fun onError(error: Int) {
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_AUDIO -> "Audio error"
                    else -> "Error: $error"
                }
                android.util.Log.e("VoiceAssistant", "Speech error: $errorMsg")
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    android.util.Log.d("VoiceAssistant", "Partial: ${matches[0]}")
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    // Start continuous listening
    LaunchedEffect(hasPermissions) {
        if (hasPermissions && SpeechRecognizer.isRecognitionAvailable(context)) {
            android.util.Log.d("VoiceAssistant", "Starting speech recognition...")
            ttsState.value?.speak("Voice assistant ready. Speak now.", TextToSpeech.QUEUE_FLUSH, null, null)
            
            while (true) {
                try {
                    android.util.Log.d("VoiceAssistant", "Starting listening session")
                    speechRecognizer.startListening(speechIntent)
                    delay(5000) // Wait 5 seconds between sessions
                } catch (e: Exception) {
                    android.util.Log.e("VoiceAssistant", "Error starting recognition: ${e.message}")
                    delay(5000) // Wait longer on error
                }
            }
        } else {
            android.util.Log.e("VoiceAssistant", "Speech recognition not available")
            ttsState.value?.speak("Speech recognition not available. Please check your device.", TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    // Request permissions
    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            ActivityCompat.requestPermissions(
                (context as Activity),
                arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA),
                100
            )
        }
    }

    // Camera preview
    if (hasPermissions) {
        VoiceAssistantCamera(
            onImageCaptured = { bitmap: Bitmap ->
                currentImage = bitmap
            },
            cameraExecutor = cameraExecutor
        )
    }
    
    // Simple UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Voice Assistant",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (chatResponse.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = chatResponse,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = if (hasPermissions) "Listening... Speak now!" else "Grant permissions to start",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        if (hasPermissions) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    try {
                        android.util.Log.d("VoiceAssistant", "Manual test: Starting speech recognition")
                        speechRecognizer.startListening(speechIntent)
                    } catch (e: Exception) {
                        android.util.Log.e("VoiceAssistant", "Manual test error: ${e.message}")
                    }
                }
            ) {
                Text("Test Speech Recognition")
            }
        }
    }
}

@Composable
fun VoiceAssistantCamera(
    onImageCaptured: (Bitmap) -> Unit,
    cameraExecutor: ExecutorService
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    LaunchedEffect(Unit) {
        try {
            val cameraProvider = ProcessCameraProvider.getInstance(context).get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            preview.setSurfaceProvider(previewView.surfaceProvider)
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)

            // Capture images for AI context
            while (true) {
                delay(3000) // Capture every 3 seconds
                val tempFile = File.createTempFile("voice_assistant", ".jpg", context.cacheDir)
                val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()
                imageCapture.takePicture(
                    outputOptions,
                    cameraExecutor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            val path = outputFileResults.savedUri?.path ?: tempFile.absolutePath
                            val bitmap = android.graphics.BitmapFactory.decodeFile(path)
                            onImageCaptured(bitmap)
                        }
                        override fun onError(exception: ImageCaptureException) {}
                    }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
}

