package com.example.assistantapp

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.ui.graphics.vector.ImageVector
import android.speech.tts.TextToSpeech
import java.util.Locale
import androidx.compose.material3.Button

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(navController: NavHostController) {
    var showDeveloperInfo by remember { mutableStateOf(false) }
    var isFirstTime by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }

    // Initialize TTS
    LaunchedEffect(Unit) {
        tts.value = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.value?.language = Locale.US
                tts.value?.setSpeechRate(0.8f) // Optimized for clarity and speed
                tts.value?.setPitch(1.1f) // Clear pitch for better comprehension
            }
        }
    }

    // Welcome message for first-time users
    LaunchedEffect(isFirstTime) {
        if (isFirstTime && tts.value != null) {
            tts.value?.speak(
                "Welcome to DRISHTI, your AI vision assistant. " +
                "This app has three main modes: Navigation Mode for real-time guidance, " +
                "Voice Assistant for asking questions, and Document Reader to read text from documents and signs. " +
                "Tap anywhere on the screen to hear options again.",
                TextToSpeech.QUEUE_FLUSH, null, null
            )
            isFirstTime = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable {
                // Audio instructions on tap
                tts.value?.speak(
                    "DRISHTI has three modes. Navigation Mode: Tap the top button. " +
                    "Voice Assistant: Tap the middle button. Document Reader: Tap the bottom button. " +
                    "Double tap any button to activate it.",
                    TextToSpeech.QUEUE_FLUSH, null, null
                )
            }
    ) {
        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Title with Audio Description
            Text(
                text = "DRISHTI",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "AI VISION FOR THE BLIND",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Your AI-powered vision assistant for the blind",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Accessible Feature Buttons
            AccessibleFeatureButton(
                icon = Icons.Default.CameraAlt,
                title = "Navigation Mode",
                description = "Real-time camera navigation with AI assistance",
                audioDescription = "Navigation Mode. Double tap to activate. Provides real-time guidance using your camera.",
                onClick = { 
                    tts.value?.speak("Opening Navigation Mode", TextToSpeech.QUEUE_FLUSH, null, null)
                    navController.navigate("blindMode") 
                },
                onDoubleClick = {
                    tts.value?.speak("Navigation Mode activated", TextToSpeech.QUEUE_FLUSH, null, null)
                    navController.navigate("blindMode")
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AccessibleFeatureButton(
                icon = Icons.Default.Mic,
                title = "Voice Assistant",
                description = "Speak to get information and guidance",
                audioDescription = "Voice Assistant. Double tap to activate. Ask questions about your environment.",
                onClick = { 
                    tts.value?.speak("Opening Voice Assistant", TextToSpeech.QUEUE_FLUSH, null, null)
                    navController.navigate("voiceAssistant") 
                },
                onDoubleClick = {
                    tts.value?.speak("Voice Assistant activated", TextToSpeech.QUEUE_FLUSH, null, null)
                    navController.navigate("voiceAssistant")
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AccessibleFeatureButton(
                icon = Icons.Default.DocumentScanner,
                title = "Document Reader",
                description = "Read documents, signs, and text using OCR",
                audioDescription = "Document Reader. Double tap to activate. Read text from documents and signs.",
                onClick = { 
                    tts.value?.speak("Opening Document Reader", TextToSpeech.QUEUE_FLUSH, null, null)
                    navController.navigate("documentReader") 
                },
                onDoubleClick = {
                    tts.value?.speak("Document Reader activated", TextToSpeech.QUEUE_FLUSH, null, null)
                    navController.navigate("documentReader")
                }
            )
        }

        // Developer Info Button (Top Right)
        IconButton(
            onClick = { 
                showDeveloperInfo = !showDeveloperInfo
                tts.value?.speak(
                    if (showDeveloperInfo) "Developer information shown" else "Developer information hidden",
                    TextToSpeech.QUEUE_FLUSH, null, null
                )
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Developer Info",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Developer Info Card
        AnimatedVisibility(
            visible = showDeveloperInfo,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 16.dp)
        ) {
            Card(
                modifier = Modifier.width(280.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Developer Info",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Team 36 - VIT Bhopal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Version 1.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Team members:",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "SUMIT PRASAD",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "SUJEET GUPTA",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "ADVAY BHAGAT",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "KUMAR AMAN",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "KRISHANU DAS",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

    }
}

@Composable
fun AccessibleFeatureButton(
    icon: ImageVector,
    title: String,
    description: String,
    audioDescription: String,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit
) {
    var clickCount by remember { mutableStateOf(0) }
    var clickTimer by remember { mutableStateOf<Long?>(null) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                clickCount++
                val currentTime = System.currentTimeMillis()
                
                if (clickCount == 1) {
                    clickTimer = currentTime
                    // Single click - provide audio description
                    // This will be handled by the parent's clickable modifier
                } else if (clickCount == 2) {
                    val timeDiff = currentTime - (clickTimer ?: 0)
                    if (timeDiff < 500) { // Double click within 500ms
                        onDoubleClick()
                        clickCount = 0
                        clickTimer = null
                    } else {
                        clickCount = 1
                        clickTimer = currentTime
                    }
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Double tap to activate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

