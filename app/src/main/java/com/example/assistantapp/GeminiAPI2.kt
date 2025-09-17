package com.example.assistantapp

import android.graphics.Bitmap
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

val ReadModel = GenerativeModel(
    modelName = "gemini-2.0-flash",
    apiKey = "ADD_YOUR_API",
    generationConfig = generationConfig {
        temperature = 0.8f
        topK = 64
        topP = 0.95f
        maxOutputTokens = 8192
        responseMimeType = "text/plain"
        candidateCount = 1
    },
    safetySettings = listOf(
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE),
    ),
    systemInstruction = content { text("You are a specialized document analysis assistant for visually impaired users. Your task is to not only read text but also provide context about what the document is about.\n\nKey Responsibilities:\n1. DOCUMENT ANALYSIS:\n   - Identify the document type (letter, bill, receipt, book, sign, etc.)\n   - Determine the main topic or subject matter\n   - Identify key information like dates, amounts, names, addresses\n   - Recognize the document's purpose (informational, instructional, commercial, etc.)\n\n2. TEXT EXTRACTION:\n   - Read ALL visible text with maximum accuracy\n   - Include headers, titles, body text, captions, labels\n   - Preserve formatting and structure when possible\n   - Handle multiple languages appropriately\n\n3. CONTEXTUAL DESCRIPTION:\n   - Provide a brief summary of what the document is about\n   - Highlight important information (dates, amounts, names)\n   - Explain the document's relevance or purpose\n   - Identify any urgent or important details\n\nResponse Format:\nStart with: \"This appears to be a [document type] about [main topic]. Here's what it says:\"\nThen provide: \"[Complete text content]\"\nEnd with: \"Key information: [important details like dates, amounts, names, etc.]\"\n\nExamples:\n- \"This appears to be a utility bill for electricity. Here's what it says: [text]. Key information: Due date is March 15th, amount due is $85.50.\"\n- \"This appears to be a restaurant menu. Here's what it says: [text]. Key information: Prices range from $8 to $25, vegetarian options available.\"\n- \"This appears to be a street sign. Here's what it says: [text]. Key information: Speed limit 25 mph, school zone ahead.\"\n\nYour goal is to make documents fully accessible by providing both the text content and meaningful context about what the document is about.") },

    )

/// ... (keep existing imports and model configuration)

suspend fun sendFrameToGemini2AI(bitmap: Bitmap, onPartialResult: (String) -> Unit, onError: (String) -> Unit, onComplete: (() -> Unit)? = null) {
    try {
        withContext(Dispatchers.IO) {
            val inputContent = content {
                image(bitmap)
                text("Analyze this document/image and provide both the document context and complete text content. Identify what type of document this is, what it's about, extract all visible text, and highlight any important information like dates, amounts, or names.")
            }

            var fullResponse = ""
            var hasResponse = false
            
            try {
                ReadModel.generateContentStream(inputContent).collect { chunk ->
                    chunk.text?.let { text ->
                        if (text.isNotBlank()) {
                            fullResponse += text
                            onPartialResult(text)
                            hasResponse = true
                            Log.d("GeminiAI", "Received text chunk: $text")
                        }
                    }
                }
                
                Log.d("GeminiAI", "Stream completed. Full response length: ${fullResponse.length}")
                
                // After stream completion, check if we got any response
                if (!hasResponse || fullResponse.isBlank()) {
                    Log.w("GeminiAI", "No text detected in the image")
                    onError("No text detected in the image. Please try again with clearer text.")
                } else {
                    Log.d("GeminiAI", "Successfully processed image with text: ${fullResponse.take(100)}...")
                }
                
                // Call completion callback
                onComplete?.invoke()
            } catch (streamException: Exception) {
                Log.e("GeminiAI", "Stream error: ${streamException.message}")
                onError("Failed to process image. Please try again.")
            }
        }
    } catch (e: IOException) {
        Log.e("GeminiAI", "Network error: ${e.message}")
        onError("Network error: ${e.message}")
    } catch (e: Exception) {
        Log.e("GeminiAI", "Unexpected error: ${e.message}")
        onError("Unexpected error: ${e.message}")
    }
}
