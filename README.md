# 🎨 Edge Detector App - Real-Time OpenCV Processing

A comprehensive Android application demonstrating real-time edge detection using OpenCV (C++), OpenGL ES rendering, JNI integration, and a TypeScript web viewer for result visualization.

## ✅ Features Implemented

### 📱 Android Application

- **Camera Integration**: Real-time camera feed using Camera2 API with TextureView
- **Native Processing**: OpenCV Canny edge detection in C++ via JNI
- **OpenGL Rendering**: Hardware-accelerated frame display using OpenGL ES 2.0+
- **Performance**: Optimized for 10-15 FPS real-time processing

### 🌐 Web Viewer

- **TypeScript Implementation**: Clean, modular web-based frame viewer
- **Real-time Stats**: FPS, resolution, processing time, and frame count display
- **Interactive Controls**: Frame refresh and mode toggle functionality
- **Base64 Image Support**: Displays processed frames from the Android app

### 🔧 Technical Integration

- **JNI Bridge**: Seamless Java ↔ C++ communication for frame processing
- **Memory Management**: Efficient frame buffer handling and texture updates
- **Cross-platform**: Android NDK with web-based debugging interface

## 📷 Screenshots

_Demo of edge detection processing and web viewer interface_

![Edge Detection Demo](docs/edge_detection_demo.gif)

## ⚙️ Setup Instructions

### Prerequisites

- **Android Studio**: Latest version with NDK support
- **OpenCV for Android**: Version 4.8+
- **CMake**: Version 3.22.1+
- **Node.js**: For TypeScript compilation (web viewer)

### 🔨 Android Setup

1. **Install OpenCV Android SDK**:

   ```bash
   # Download OpenCV Android SDK from opencv.org
   # Extract to: ~/opencv-android-sdk/
   ```

2. **Configure NDK Path**:

   - Open `local.properties` (create if needed)
   - Add: `ndk.dir=/path/to/your/android-ndk`

3. **Build Native Library**:

   ```bash
   cd EdgeDetectorApp
   ./gradlew assembleDebug
   ```

4. **Run on Device**:
   - Connect Android device with USB debugging
   - Grant camera permissions when prompted

### 🌐 Web Viewer Setup

1. **Install Dependencies**:

   ```bash
   cd web
   npm install
   ```

2. **Compile TypeScript**:

   ```bash
   npm run build
   ```

3. **Serve Locally**:
   ```bash
   npm run serve
   # Access at http://localhost:8080
   ```

### 📦 Dependencies

#### Android

- `OpenCV 4.8+`: Computer vision processing
- `Camera2 API`: Camera stream management
- `OpenGL ES 2.0+`: Hardware-accelerated rendering
- `NDK r25+`: Native code compilation

#### Web

- `TypeScript 5.0+`: Type-safe web development
- `DOM APIs`: Browser interface management

## 🧠 Architecture Explanation

### 📊 Data Flow

```
Camera Feed → Image Processing → OpenGL Rendering
     ↓              ↓               ↓
TextureView → JNI/OpenCV → GLSurfaceView
     ↓              ↓               ↓
Android Java ← C++ Native ← GPU Textures
```

### 🔄 JNI Integration

1. **Frame Capture**: Camera2 API captures YUV frames via ImageReader
2. **JNI Bridge**: Java byte arrays passed to native `processFrame()` function
3. **OpenCV Processing**: C++ applies Canny edge detection algorithm
4. **Result Return**: Processed PNG data returned to Java as byte array
5. **GL Rendering**: Bitmap decoded and uploaded to OpenGL texture

### 🎨 OpenGL Rendering Pipeline

- **Vertex Shader**: Positions quad vertices for full-screen display
- **Fragment Shader**: Samples texture and outputs processed pixels
- **Texture Management**: Dynamic texture updates from processed frames
- **Buffer Objects**: Efficient vertex and texture coordinate handling

### 🌐 Web Viewer Architecture

- **TypeScript Classes**: Modular `EdgeDetectorViewer` for frame management
- **DOM Manipulation**: Real-time stats updates and image display
- **Base64 Support**: Displays images received from Android processing
- **Event Handling**: Interactive controls for refresh and mode toggle

## 🏗️ Project Structure

```
EdgeDetectorApp/
├── app/                          # Android application
│   ├── src/main/
│   │   ├── java/com/edgedetector/
│   │   │   └── CameraActivity.java    # Main camera & JNI integration
│   │   ├── cpp/
│   │   │   └── CMakeLists.txt         # Native build configuration
│   │   └── res/                       # Android resources
│   └── build.gradle                   # App-level build config
├── jni/
│   └── edge_detector.cpp              # OpenCV processing (C++)
├── gl/
│   └── GLRenderer.java                # OpenGL ES rendering
├── web/                               # TypeScript web viewer
│   ├── src/
│   │   ├── app.ts                     # Main viewer logic
│   │   └── index.html                 # Web interface
│   ├── package.json                   # Node.js dependencies
│   └── tsconfig.json                  # TypeScript configuration
├── build.gradle                       # Project-level build config
└── README.md                          # This documentation
```

## 🚀 Performance Optimizations

- **Frame Rate**: Target 15 FPS with adaptive processing
- **Memory Management**: Efficient buffer reuse and garbage collection
- **GPU Acceleration**: OpenGL ES hardware rendering
- **Thread Management**: Background processing to avoid UI blocking

## 🧪 Testing & Debugging

### 📱 Android Debugging

```bash
adb logcat | grep EdgeDetector
```

### 🌐 Web Development

```bash
# Watch mode for live TypeScript compilation
npm run watch
```

### 📊 Performance Monitoring

- Monitor FPS in web viewer stats panel
- Use Android Studio profiler for memory analysis
- OpenGL ES debugging with graphics tools

## 🔮 Future Enhancements

- **WebSocket Integration**: Real-time frame streaming to web viewer
- **Multiple Filters**: Sobel, Laplacian, and other edge detection algorithms
- **Camera Controls**: Exposure, focus, and zoom adjustments
- **Recording**: Save processed video streams
- **Cloud Processing**: Offload heavy computation to remote servers

## 📝 License

MIT License - Feel free to use this project for learning and development.

---

**⚠️ Note**: This project demonstrates core integration patterns for Android + OpenCV + OpenGL + Web technologies. Production usage would require additional error handling, security considerations, and performance optimizations.
