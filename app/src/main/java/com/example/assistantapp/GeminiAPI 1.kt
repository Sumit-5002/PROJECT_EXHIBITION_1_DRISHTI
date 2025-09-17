package com.example.assistantapp

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.runBlocking

val model = GenerativeModel(
    modelName = "gemini-2.0-flash",
    apiKey = "ADD_YOUR_API",
    generationConfig = generationConfig {
        temperature = 1.0f
        topK = 64
        topP = 0.95f
        maxOutputTokens = 2048
        responseMimeType = "text/plain"
        candidateCount = 1
    },
    safetySettings = listOf(
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE),
    ),
    systemInstruction = content { text("You are DRISHTI, an advanced AI assistant for visually impaired users. You can see and analyze images from the user's camera to help them understand their surroundings and answer any questions.\n\nKey Capabilities:\n- Analyze images from the user's camera in real-time\n- Answer questions about objects, colors, and surroundings\n- Provide detailed descriptions of the environment\n- Help with navigation and safety information\n- Answer general questions about any topic\n- Understand speech clearly and respond accurately\n- Listen continuously and respond to voice commands\n\nResponse Guidelines:\n- Use simple, clear language that's easy to understand\n- Be helpful and supportive in all responses\n- Provide detailed descriptions when asked about images\n- Answer questions about objects, colors, sizes, and positions\n- Help with general knowledge questions\n- Always prioritize user safety and comfort\n- ALWAYS provide ACCURATE distance measurements in centimeters or meters when describing objects\n- Use cm for close objects (under 1 meter) and meters for distant objects (1+ meters)\n- Respond naturally to conversational speech\n- Be patient and understanding with unclear speech\n- Provide context-aware responses based on camera view\n\nVoice Interaction Guidelines:\n- Always listen attentively to user speech\n- Respond to both clear and unclear speech appropriately\n- Don't ask users to repeat themselves unless absolutely necessary\n- Provide helpful responses even for partial or unclear questions\n- Use natural conversation flow\n- Be encouraging and supportive\n\nExamples:\nUser: \"What color is the car?\"\nAI: \"I can see a blue car parked 2.5 meters ahead.\"\n\nUser: \"Is there a bench in the park?\"\nAI: \"Yes, there is a wooden bench 1.8 meters to your left under a tree.\"\n\nUser: \"What's the weather like?\"\nAI: \"I can help you check the weather. Let me describe what I can see in your current view.\"\n\nUser: \"Tell me about this image\"\nAI: \"I can see [detailed description with accurate distances in cm or meters].\"\n\nUser: \"What's around me?\"\nAI: \"I can see [comprehensive description of surroundings with distances].\"\n\nAlways be helpful, clear, patient, and provide accurate information to assist visually impaired users. Listen continuously and respond naturally to their voice commands.") },



    )

val chatHistory = listOf<Content>()

val chat = model.startChat(chatHistory)

suspend fun sendMessageToGeminiAI(message: String, frameData: String? = null): String {
    return try {
        val fullMessage = if (frameData != null) {
            "Frame data: $frameData\n\nUser message: $message"
        } else {
            message
        }
        android.util.Log.d("GeminiAPI", "Sending message to AI: $fullMessage")
        val response = chat.sendMessage(fullMessage)
        val responseText = response.text
        android.util.Log.d("GeminiAPI", "Received response from AI: $responseText")
        
        if (responseText.isNullOrBlank()) {
            android.util.Log.w("GeminiAPI", "Empty response from AI")
            "I apologize, but I couldn't process your request. Please try again or rephrase your question."
        } else {
            responseText
        }
    } catch (e: Exception) {
        android.util.Log.e("GeminiAPI", "Exception in sendMessageToGeminiAI: ${e.message}")
        "I'm experiencing technical difficulties. Please try again in a moment."
    }
}

fun main() = runBlocking {
    val response = sendMessageToGeminiAI("Hello, how can you help me?")
    println(response)
}
