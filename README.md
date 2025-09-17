# 🚀 DRISHTI: AI VISION FOR THE BLIND

## 🎓 VIT Bhopal College Project Exhibition - Group 36

![DRISHTI Logo](app/src/main/res/drawable/applogo.png)

### 📱 Project Overview

**DRISHTI** is an innovative Android application designed to empower visually impaired individuals by providing real-time environmental awareness and navigation assistance through advanced AI technology. This project represents the culmination of our academic journey at VIT Bhopal, showcasing cutting-edge mobile development and artificial intelligence integration.

---

## 🌟 Key Features

### 1️⃣ **Navigation Mode** 🧭
- **Real-time Camera Analysis**: Continuous environmental monitoring using device camera
- **AI-Powered Object Detection**: Identifies obstacles, people, vehicles, and environmental hazards
- **Voice Feedback**: Instant audio descriptions of surroundings for safe navigation
- **Smart Frame Processing**: Optimized 3-second intervals for responsive performance

### 2️⃣ **Assistant Mode** 🤖
- **Interactive Voice Commands**: Ask questions about your environment in natural language
- **Context-Aware Responses**: AI understands spatial context and provides relevant information
- **Environmental Queries**: "What color is the car?", "How is the weather?", "Is there a person nearby?"
- **General Knowledge**: Access information about people, objects, and concepts

### 3️⃣ **Document Reader Mode** 📖
- **Smart Document Analysis**: Not only reads text but also identifies what the document is about
- **Context-Aware Reading**: Provides document type, main topic, and key information
- **Optical Character Recognition (OCR)**: Converts visual text to speech with context
- **High-Accuracy Reading**: Powered by Google's Gemini AI for reliable text interpretation
- **Instant Audio Output**: Real-time text-to-speech conversion with document context
- **Key Information Extraction**: Highlights important details like dates, amounts, names, and addresses

---

## 🛠️ Technical Specifications

### **Platform & Architecture**
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + simple repository pattern
- **Package name**: `com.example.assistantapp`
- **Minimum SDK**: 32
- **Target/Compile SDK**: 35
- **Java/Kotlin**: Java 17, Kotlin JVM target 17

### **AI Integration**
- **Models**: Google Gemini (Vision + Text)
- **Library**: `com.google.ai.client.generativeai:generativeai:0.9.0`
- **Use cases**: Navigation guidance, voice assistant, document reading (OCR + context)

### **Imaging & Media**
- **Camera**: CameraX (`camera-core/camera2/camera-lifecycle/camera-view` 1.3.1)
- **Default Image Quality**: 720p (1280×720)
- **Audio**: Android TTS and microphone input

### **Build & Packaging**
- **Compose Compiler Ext**: 1.5.8
- **NDK version**: 26.1.10909125 (ABI filters: arm64-v8a, armeabi-v7a, x86, x86_64)
- **Proguard**: Disabled in debug and release by default

### **Manifest Permissions**
- Camera, Record Audio, Internet, Vibrate, Access Network State
- Media button query for TTS service

---

## 🚀 Getting Started

### **Prerequisites**
- Android Studio (latest stable)
- Android SDK 35, min device SDK 32+
- Google Gemini API key (`https://ai.google.dev`)
- Android device with camera and microphone

### **Clone**
```bash
git clone https://github.com/Sumit-5002/PROJECT_EXHIBITION_1_DRISHTI.git
cd PROJECT_EXHIBITION_1_DRISHTI
```

### **API Key Setup**
Open these files and replace the placeholder with your key:
- `app/src/main/java/com/example/assistantapp/GeminiAPI.kt`
- `app/src/main/java/com/example/assistantapp/GeminiAPI 1.kt`
- `app/src/main/java/com/example/assistantapp/GeminiAPI2.kt`

Replace:
```
apiKey = "ADD_YOUR_API"
```
with your actual key string from `https://ai.google.dev`.

### Notes
- Do not commit your actual key to GitHub. Keep your copy local only.
- If you accidentally commit a key, rotate it in Google AI Studio and push a fix.

### **Build & Run**
- Open the project in Android Studio
- Sync Gradle
- Connect an Android device
- Run the `app` configuration

---

## 🎯 Usage Instructions

### **Navigation Mode**
- **Single Tap**: Activate camera and start navigation
- **Voice Feedback**: Automatic environmental descriptions every 3 seconds
- **Safety Alerts**: Immediate warnings for obstacles and hazards

### **Assistant Mode**
- **Double Tap**: Switch to interactive assistant mode
- **Voice Commands**: Ask questions about surroundings or general topics
- **Natural Language**: Use conversational queries
- **Continuous Listening**: Designed to minimize interruptions

### **Document Reader Mode**
- **Long Press**: Activate document reading mode
- **Point Camera**: Aim at document or text you want to read
- **Smart Analysis**: Automatically identifies document type and context
- **Contextual Reading**: Provides document summary, full text, and key information
- **Automatic Reading**: Instant text-to-speech conversion with document context

---

## 🛡️ Security and Key Rotation
- If GitHub flags a committed key, rotate it immediately in Google AI Studio and update the placeholder locally.

## 👥 Team Members - Group 36

### **Project Lead & Development**
| Name             | Contribution                                     |
|------------------|--------------------------------------------------|
| **SUMIT PRASAD** | Core Architecture, AI Integration, UI/UX Design  |
| **SUJEET GUPTA** | Icon & Presentation designer, Resource Manager   |
| **ADVAY BHAGAT** | Report and presentation designer                 |
| **KUMAR AMAN**   | Offline capability architecture (in DRISHTI)     |
| **KRISHANU DAS** | IoT researcher, Report manager                   |

### **Academic Details**
- **Institution**: VIT Bhopal (Vellore Institute of Technology, Bhopal Campus)
- **Course**: Bachelor of Technology in Computer Science
- **Semester**: 2nd Year, 3rd Semester Project
- **Academic Year**: 2024-2025
- **Project Type**: College Project Exhibition

---

## 🔧 Technology Stack

### **Frontend Technologies**
- **Jetpack Compose**: Modern Android UI toolkit
- **Material Design 3**: Latest Material Design components
- **Kotlin Coroutines**: Asynchronous programming
- **Android Navigation**: Screen navigation and routing

### **Backend & AI**
- **Google Gemini AI**: Vision + language tasks
- **OkHttp**: Networking
- **JSON Processing**: Data serialization and parsing

### **Development Tools**
- **Android Studio**: Primary development environment
- **Git**: Version control and collaboration
- **Gradle**: Build automation and dependency management
- **Android Debug Bridge (ADB)**: Device testing and debugging

---

## 📊 Performance Metrics

### **Optimization Results**
- **Frame Processing**: Guidance every ~3 seconds for responsive navigation
- **Camera Resolution**: 720p (1280×720) default capture/analysis
- **Speech Rate**: ~1.2x for clarity and speed
- **Memory Usage**: Efficient image lifecycle management

### **User Experience Improvements**
- **Reduced Latency**: Faster response times for better user experience
- **Improved Accuracy**: Better AI recognition and processing
- **Enhanced Accessibility**: Optimized for visually impaired users
- **Battery Efficiency**: Better power management
- **Smart Document Analysis**: Documents now provide context about what they're about
- **Continuous Voice Listening**: AI assistant listens continuously without constant error messages
- **Improved Voice Recognition**: Better handling of unclear speech and natural conversation flow

---

## 🎓 Academic Impact

### **Learning Outcomes**
- **Advanced Android Development**: Mastery of modern Android development practices
- **AI Integration**: Practical experience with cutting-edge AI technologies
- **Accessibility Design**: Understanding of inclusive design principles
- **Project Management**: Real-world project planning and execution

### **Innovation Contributions**
- **Assistive Technology**: Development of tools for differently-abled individuals
- **AI for Social Good**: Application of AI to solve real-world accessibility challenges
- **Mobile Innovation**: Pushing boundaries of mobile app capabilities
- **User-Centric Design**: Focus on user experience and accessibility

---

## 🔮 Future Enhancements

### **Planned Features**
- Offline mode via on-device models
- Multi-language support
- Gesture-based controls
- Cloud backup/sync

### **Technical Improvements**
- Edge inference for lower latency
- Continued prompt/UX tuning
- IoT and wearable integrations

---

## 📚 References & Resources

### **Technical Documentation**
- [Android Developer Documentation](https://developer.android.com/)
- [Jetpack Compose Guide](https://developer.android.com/jetpack/compose)
- [Google Gemini AI Documentation](https://ai.google.dev/docs)
- [Material Design Guidelines](https://material.io/design)

### **Research Papers**
- "Computer Vision for the Visually Impaired" - IEEE Access
- "AI-Powered Assistive Technologies" - ACM Digital Library
- "Mobile Accessibility in Modern Applications" - Mobile HCI Conference

---

## 📄 License

This project is licensed under the MIT License - see the `LICENSE` file for details.

### **Academic Use**
This project is developed for academic purposes at VIT Bhopal. Please provide appropriate attribution when referencing or building upon this code.

---

## 🤝 Acknowledgments

- **VIT Bhopal Faculty**: For guidance and academic support
- **Google AI Team**: For providing the Gemini AI platform
- **Android Developer Community**: For open-source contributions
- **Accessibility Advocates**: For insights into user needs and requirements

---

## 📞 Contact

**Group 36 - VIT Bhopal**
- **Email**: sumit.24bce11520@vitbhopal.ac.in
- **GitHub**: [Project Repository](https://github.com/Sumit-5002/PROJECT_EXHIBITION_1_DRISHTI)
- **Institution**: Vellore Institute of Technology, Bhopal Campus
- **Location**: Bhopal, Madhya Pradesh, India

---

*"Empowering the visually impaired through technology and innovation"* 🚀

**© 2024 Group 36, VIT Bhopal. All rights reserved.**

