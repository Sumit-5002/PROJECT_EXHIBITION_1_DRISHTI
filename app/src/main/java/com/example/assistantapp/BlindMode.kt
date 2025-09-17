package com.example.assistantapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun BlindModeScreen() {
    val context = LocalContext.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    // Navigation response state
    var navigationResponse by remember { mutableStateOf("") }
    
    // Permission state
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // TTS setup
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }
    
    // Initialize TTS with optimized settings for blind users
    LaunchedEffect(context) {
        tts.value = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.value?.language = Locale.US
                tts.value?.setSpeechRate(0.8f) // Optimized for clarity and responsiveness
                tts.value?.setPitch(1.1f) // Clear pitch for better comprehension
                // Try to use the best available voice
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
    
    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            tts.value?.stop()
            tts.value?.shutdown()
            cameraExecutor.shutdown()
        }
    }
    
    
    // Welcome message
    LaunchedEffect(tts.value) {
        if (tts.value != null) {
            tts.value?.speak(
                "DRISHTI Navigation Mode activated. I will provide real-time guidance about your surroundings.",
                TextToSpeech.QUEUE_FLUSH, null, null
            )
            // Start navigation immediately
            delay(2000)
            tts.value?.speak("Starting navigation guidance. I will describe your surroundings every 2 seconds.", TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }
    
    // Request permissions
        LaunchedEffect(Unit) {
        if (!hasPermission) {
            ActivityCompat.requestPermissions(
                (context as Activity),
                arrayOf(Manifest.permission.CAMERA),
                1
            )
        }
        }
        
        // Update permission status
        LaunchedEffect(Unit) {
            while (true) {
                hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                delay(1000)
        }
    }
    
    // Main UI - Pure Navigation Mode
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
        // Camera preview for navigation
        if (hasPermission) {
            NavigationModeCamera(
                onNavigationUpdate = { description ->
                    navigationResponse = description
                    tts.value?.speak(description, TextToSpeech.QUEUE_ADD, null, null)
                }
            )
        }
        
        // Navigation status indicator
        if (hasPermission) {
            Card(
                    modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pulsing dot to show active navigation
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                MaterialTheme.colorScheme.onPrimary,
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = "Navigation Active",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // AI Response Text Output Overlay
        if (navigationResponse.isNotBlank()) {
            Card(
                    modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
                    .fillMaxWidth(0.9f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Navigation Guidance:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = navigationResponse,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }
            }
            
            // Permission status overlay
            if (!hasPermission) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                        text = "Camera Permission Required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                        text = "Please grant camera permission to use Navigation Mode",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                }
            }
        }
    }
}

private fun CoroutineScope.onDispose(function: () -> Unit) {}

// NAVIGATION MODE - Continuous real-time guidance
@Composable
fun NavigationModeCamera(
    onNavigationUpdate: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val preview = Preview.Builder()
        .setTargetResolution(android.util.Size(1920, 1080)) // 1080p for maximum quality
        .build()
    val previewView = remember { PreviewView(context) }
    
    var lastProcessedTimestamp by remember { mutableStateOf(0L) }
    val frameInterval = 1500L // Process every 1.5 seconds for continuous responsiveness
    var isProcessing by remember { mutableStateOf(false) }
    
    val imageAnalysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setTargetResolution(android.util.Size(1920, 1080)) // 1080p for maximum quality
        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888) // Best quality format
        .build()
        .also {
            it.setAnalyzer(Executors.newSingleThreadExecutor(), ImageAnalysis.Analyzer { imageProxy ->
                val currentTimestamp = System.currentTimeMillis()
                
                // Only process if not already processing and enough time has passed
                if (!isProcessing && currentTimestamp - lastProcessedTimestamp >= frameInterval) {
                    isProcessing = true
                    lastProcessedTimestamp = currentTimestamp
                    
                    val bitmap = imageProxy.toBitmap()
                    bitmap?.let { bmp ->
                        GlobalScope.launch {
                            try {
                                var fullDescription = ""
                                var isComplete = false
                                
                                // Use the navigation-specific AI function
                                android.util.Log.d("BlindMode", "Starting AI processing for navigation")
                                sendFrameToGeminiAI(bmp, 
                                    onPartialResult = { partialResult ->
                                        android.util.Log.d("BlindMode", "Received partial result: $partialResult")
                                        if (partialResult.isNotBlank()) {
                                            fullDescription += partialResult
                                        }
                                    }, 
                                    onError = { error ->
                                        android.util.Log.e("BlindMode", "AI Error: $error")
                                        fullDescription = "Navigation guidance temporarily unavailable. Please try again."
                                        isComplete = true
                                    },
                                    onComplete = {
                                        android.util.Log.d("BlindMode", "AI processing completed")
                                        isComplete = true
                                    }
                                )
                                
                                // Wait for complete response with better timeout handling
                                var waitTime = 0
                                while (!isComplete && waitTime < 8000) {
                                    delay(100)
                                    waitTime += 100
                                }
                                
                                // Only speak if we have a complete response
                                if (fullDescription.isNotBlank() && fullDescription.length > 5) {
                                    onNavigationUpdate(fullDescription)
                                } else {
                                    onNavigationUpdate("Clear path ahead. Continue forward with caution.")
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("BlindMode", "Navigation processing error: ${e.message}")
                                val errorMessage = when {
                                    e.message?.contains("network", ignoreCase = true) == true -> 
                                        "Network error. Please check your internet connection."
                                    e.message?.contains("timeout", ignoreCase = true) == true -> 
                                        "Navigation request timed out. Please try again."
                                    e.message?.contains("api", ignoreCase = true) == true -> 
                                        "Navigation service temporarily unavailable. Please try again."
                                    else -> "Navigation guidance temporarily unavailable. Please try again."
                                }
                                onNavigationUpdate(errorMessage)
                            } finally {
                                isProcessing = false
                            }
                        }
                    }
                }
                imageProxy.close()
            })
        }
    
    LaunchedEffect(cameraProviderFuture) {
        try {
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            preview.setSurfaceProvider(previewView.surfaceProvider)
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    
    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}
