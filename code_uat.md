### **📝 DETAILED FILE-BY-FILE CODE EXPLANATION**

#### **[`app/src/main/java/com/edgedetector/CameraActivity.java`](app/src/main/java/com/edgedetector/CameraActivity.java)**

**Purpose**: Main Android activity orchestrating the entire camera → processing → rendering pipeline

**Key Components & Functions**:

```java
public class CameraActivity extends Activity {
    // Core components
    private TextureView textureView;           // Camera preview surface
    private GLSurfaceView glSurfaceView;       // OpenGL rendering surface
    private GLRenderer glRenderer;             // Custom OpenGL renderer
    private CameraDevice cameraDevice;         // Camera2 API device handle
    private ImageReader imageReader;           // Frame capture for processing
    private EdgeDetectorWebSocket webSocketServer; // Streaming server

    // Native library loading
    static {
        System.loadLibrary("edge_detector");   // Load C++ library
    }

    // JNI bridge method
    public native byte[] processFrame(byte[] input, int width, int height);
```

**Critical Methods**:

1. **`onCreate()`**: Initializes UI components and starts camera
2. **`openCamera()`**: Sets up Camera2 API with preview and capture sessions
3. **`startPreview()`**: Configures ImageReader for YUV frame capture
4. **`processImageFrame(Image image)`**: Core processing pipeline:

   ```java
   private void processImageFrame(Image image) {
       // Convert YUV_420_888 to byte array
       byte[] nv21 = imageToNV21(image);

       // Process via JNI (calls C++ code)
       byte[] processedFrame = processFrame(nv21, width, height);

       // Update OpenGL renderer
       glRenderer.updateFrame(processedFrame);

       // Stream to web clients
       webSocketServer.broadcastFrame(processedFrame);
   }
   ```

#### **[`jni/edge_detector.cpp`](jni/edge_detector.cpp)**

**Purpose**: Native C++ implementation for high-performance OpenCV processing

**Core Algorithm Implementation**:

```cpp
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_edgedetector_CameraActivity_processFrame(JNIEnv* env, jobject,
                                                   jbyteArray input, jint width, jint height) {
    // 1. Get input data from Java
    jbyte* inputBytes = env->GetByteArrayElements(input, nullptr);

    // 2. Create OpenCV Mat from YUV data
    cv::Mat yuv(height + height / 2, width, CV_8UC1, (unsigned char*)inputBytes);

    // 3. Convert color space: YUV → BGR
    cv::Mat bgr;
    cv::cvtColor(yuv, bgr, cv::COLOR_YUV2BGR_NV21);

    // 4. Apply Canny edge detection
    cv::Mat edges;
    cv::Canny(bgr, edges,           // Input/output matrices
              100, 200,             // Low/high thresholds (optimized values)
              3,                    // Sobel kernel size
              false);               // L2 gradient magnitude

    // 5. Convert back to BGR for display
    cv::Mat out;
    cv::cvtColor(edges, out, cv::COLOR_GRAY2BGR);

    // 6. Encode as PNG for efficient transmission
    std::vector<uchar> buf;
    cv::imencode(".png", out, buf);

    // 7. Return to Java as byte array
    jbyteArray result = env->NewByteArray(buf.size());
    env->SetByteArrayRegion(result, 0, buf.size(), reinterpret_cast<jbyte*>(buf.data()));

    // 8. Clean up native memory
    env->ReleaseByteArrayElements(input, inputBytes, JNI_ABORT);

    return result;
}
```

**Performance Optimizations**:

- **Direct Memory Access**: Uses JNI `GetByteArrayElements` for zero-copy operations
- **Efficient Color Conversion**: Optimized YUV→BGR→GRAY→BGR pipeline
- **Tuned Parameters**: Canny thresholds (100, 200) optimized for real-time performance
- **PNG Compression**: Reduces network transmission size

#### **[`gl/GLRenderer.java`](gl/GLRenderer.java)**

**Purpose**: Hardware-accelerated OpenGL ES rendering for smooth 60 FPS display

**Shader Programs**:

```java
public class GLRenderer implements GLSurfaceView.Renderer {
    // Vertex shader: Positions quad vertices for full-screen display
    private final String VERTEX_SHADER_CODE =
        "attribute vec4 vPosition;" +
        "attribute vec2 vTexCoord;" +
        "varying vec2 texCoord;" +
        "void main() {" +
        "  gl_Position = vPosition;" +
        "  texCoord = vTexCoord;" +
        "}";

    // Fragment shader: Samples texture and outputs processed pixels
    private final String FRAGMENT_SHADER_CODE =
        "precision mediump float;" +
        "uniform sampler2D uTexture;" +
        "varying vec2 texCoord;" +
        "void main() {" +
        "  gl_FragColor = texture2D(uTexture, texCoord);" +
        "}";
```

#### **[`app/src/main/java/com/edgedetector/WebSocketServer.java`](app/src/main/java/com/edgedetector/WebSocketServer.java)**

**Purpose**: Network streaming server for real-time frame transmission to web clients

**WebSocket Implementation**:

```java
public class EdgeDetectorWebSocket extends WebSocketServer {
    private Set<WebSocket> connections;

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn);
        Log.d("WebSocket", "Client connected");
    }

    // Broadcast processed frame to all connected clients
    public void broadcastFrame(byte[] frameData) {
        String base64Frame = Base64.encodeToString(frameData, Base64.NO_WRAP);
        String frameMessage = "{\"type\":\"frame\",\"data\":\"" + base64Frame + "\"}";

        synchronized (connections) {
            for (WebSocket conn : connections) {
                conn.send(frameMessage);
            }
        }
    }
}
```

#### **[`web/src/app.ts`](web/src/app.ts)**

**Purpose**: TypeScript implementation of real-time web viewer with WebSocket client

**Core Classes & Interfaces**:

```typescript
// Frame statistics interface
interface FrameStats {
  fps: number;
  resolution: string;
  mode: string;
  frameCount: number;
  processingTime: number;
  lastUpdated: string;
}

// Main viewer class
class EdgeDetectorViewer {
  private webSocket: WebSocket | null = null;
  private frameImage!: HTMLImageElement;
  private stats!: FrameStats;
  private currentMode: "edge" | "raw" = "edge";

  constructor() {
    this.initializeDOM();
    this.connectWebSocket();
  }

  private connectWebSocket(): void {
    const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
    const host = window.location.hostname || "localhost";
    this.webSocket = new WebSocket(`${protocol}//${host}:8765`);

    this.webSocket.onmessage = (event) => {
      const message = JSON.parse(event.data);
      if (message.type === "frame") {
        this.updateFrameFromSource(`data:image/png;base64,${message.data}`);
      }
    };
  }
}
```

---

## 🔧 **BUILD SYSTEM CONFIGURATION**

### **Android Build Configuration**

#### **[`app/build.gradle`](app/build.gradle)** (App-level):

```gradle
android {
    compileSdk 34
    ndkVersion "25.2.9519653"

    defaultConfig {
        applicationId "com.edgedetector"
        minSdk 21
        targetSdk 34

        // NDK configuration
        ndk {
            abiFilters 'arm64-v8a', 'armeabi-v7a'
        }

        // CMake configuration
        externalNativeBuild {
            cmake {
                cppFlags "-std=c++17"
                arguments "-DOpenCV_DIR=/path/to/opencv-android-sdk/sdk/native/jni"
            }
        }
    }

    // Link CMake build
    externalNativeBuild {
        cmake {
            path file('src/main/cpp/CMakeLists.txt')
            version '3.22.1'
        }
    }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'org.java-websocket:Java-WebSocket:1.5.3' // WebSocket server
    // OpenGL ES and Camera2 are part of Android SDK
}
```

#### **[`app/src/main/cpp/CMakeLists.txt`](app/src/main/cpp/CMakeLists.txt)**:

```cmake
cmake_minimum_required(VERSION 3.22.1)
project("edge_detector")

# OpenCV configuration
set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/../../../../../opencv-android-sdk/sdk/native/jni)
find_package(OpenCV REQUIRED)

# Create shared library
add_library(
    edge_detector
    SHARED
    ${CMAKE_SOURCE_DIR}/../../../../../jni/edge_detector.cpp
)

# Include directories
target_include_directories(edge_detector PRIVATE ${OpenCV_INCLUDE_DIRS})

# Link libraries
target_link_libraries(
    edge_detector
    ${OpenCV_LIBS}
    jnigraphics  # For efficient bitmap operations
    log         # For Android logging
)
```

### **Web Build Configuration**

#### **[`web/package.json`](web/package.json)**:

```json
{
  "name": "edge-detector-web-viewer",
  "version": "1.0.0",
  "scripts": {
    "build": "npm run clean && tsc && npm run copy-assets",
    "clean": "rimraf dist",
    "copy-assets": "copyfiles src/*.html src/*.ico dist --up 1",
    "watch": "tsc --watch",
    "serve": "http-server dist --cors --cache=-1"
  },
  "devDependencies": {
    "typescript": "^5.0.4",
    "http-server": "^14.1.1",
    "copyfiles": "^2.4.1",
    "rimraf": "^5.0.1"
  }
}
```

## z
