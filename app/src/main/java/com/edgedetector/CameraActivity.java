package com.edgedetector;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.nio.ByteBuffer;
import com.edgedetector.gl.GLRenderer;

public class CameraActivity extends Activity {
    private TextureView textureView;
    private GLSurfaceView glSurfaceView;
    private GLRenderer glRenderer;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder previewRequestBuilder;
    private Size previewSize;
    private ImageReader imageReader;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private EdgeDetectorWebSocketServer webSocketServer;
    private static final int WEBSOCKET_PORT = 8765;
    
    // UI Elements
    private TextView fpsValue;
    private TextView frameCount;
    private TextView connectionCount;
    private TextView serverIp;
    private TextView processingStatus;
    private View recordingIndicator;
    
    // Stats tracking
    private int totalFrames = 0;
    private long startTime = System.currentTimeMillis();

    // Load native library
    static {
        System.loadLibrary("edge_detector");
    }

    // Native method declaration
    public native byte[] processFrame(byte[] input, int width, int height);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        
        // Initialize UI elements
        textureView = findViewById(R.id.texture_view);
        glSurfaceView = findViewById(R.id.gl_surface_view);
        fpsValue = findViewById(R.id.fps_value);
        frameCount = findViewById(R.id.frame_count);
        connectionCount = findViewById(R.id.connection_count);
        serverIp = findViewById(R.id.server_ip);
        processingStatus = findViewById(R.id.processing_status);
        recordingIndicator = findViewById(R.id.recording_indicator);
        
        // Start WebSocket server
        webSocketServer = new EdgeDetectorWebSocketServer();
        try {
            webSocketServer.start();
            android.util.Log.i("EdgeDetector", "WebSocket server started on port " + WEBSOCKET_PORT);
            updateServerStatus(true);
        } catch (Exception e) {
            android.util.Log.e("EdgeDetector", "Failed to start WebSocket server: " + e.getMessage());
            updateServerStatus(false);
        }
        
        // Set up OpenGL surface view
        glSurfaceView.setEGLContextClientVersion(2);
        glRenderer = new GLRenderer();
        glSurfaceView.setRenderer(glRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        
        // Set up camera texture view (for preview)
        textureView.setSurfaceTextureListener(surfaceTextureListener);
        
        // Initialize stats
        updateStats();
    }
    
    private void updateServerStatus(boolean isOnline) {
        runOnUiThread(() -> {
            if (isOnline) {
                serverIp.setText(getLocalIpAddress() + ":8765");
                processingStatus.setText("Processing: Active");
                // Make recording indicator visible and animate
                recordingIndicator.setVisibility(View.VISIBLE);
            } else {
                serverIp.setText("Server Offline");
                processingStatus.setText("Processing: Inactive");
                recordingIndicator.setVisibility(View.INVISIBLE);
            }
        });
    }
    
    private void updateStats() {
        runOnUiThread(() -> {
            // Calculate FPS
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - startTime;
            if (elapsed > 0) {
                float fps = (totalFrames * 1000.0f) / elapsed;
                fpsValue.setText(String.format("%.1f", fps));
            }
            
            // Update frame count
            frameCount.setText(String.format("%,d", totalFrames));
            
            // Update connection count (simulate for now)
            connectionCount.setText("1");
        });
    }
    
    private String getLocalIpAddress() {
        try {
            java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface networkInterface = interfaces.nextElement();
                java.util.Enumeration<java.net.InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    java.net.InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && address instanceof java.net.Inet4Address) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("EdgeDetector", "Error getting IP address", e);
        }
        return "unknown";
    }

    private final TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            // Process frame when texture is updated
            processCurrentFrame();
        }
    };

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA },
                    REQUEST_CAMERA_PERMISSION);
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
            manager.openCamera(cameraId, stateCallback, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
        }
    };

    private void startPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(640, 480);
            Surface surface = new Surface(texture);
            
            // Set up ImageReader for frame processing
            imageReader = ImageReader.newInstance(640, 480, ImageFormat.YUV_420_888, 2);
            imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = reader.acquireLatestImage();
                    if (image != null) {
                        processImageFrame(image);
                        image.close();
                    }
                }
            }, null);
            
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);
            previewRequestBuilder.addTarget(imageReader.getSurface());
            
            cameraDevice.createCaptureSession(
                java.util.Arrays.asList(surface, imageReader.getSurface()),
                new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        captureSession = session;
                        try {
                            captureSession.setRepeatingRequest(previewRequestBuilder.build(), null, null);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    }
                }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        }
    }
    
    private void processImageFrame(Image image) {
        // Convert Image to byte array for JNI processing
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();
        
        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();
        
        byte[] nv21 = new byte[ySize + uSize + vSize];
        yBuffer.get(nv21, 0, ySize);
        uBuffer.get(nv21, ySize, uSize);
        vBuffer.get(nv21, ySize + uSize, vSize);
        
        // Process frame using JNI
        byte[] processedFrame = processFrame(nv21, image.getWidth(), image.getHeight());
        
        // Update frame counter
        totalFrames++;
        
        // Send processed frame to OpenGL renderer and WebSocket
        if (processedFrame != null) {
            if (glRenderer != null) {
                glRenderer.updateFrame(processedFrame);
                glSurfaceView.requestRender();
            }
            // Send frame to connected web viewers
            if (webSocketServer != null) {
                webSocketServer.broadcastFrame(processedFrame);
                android.util.Log.d("EdgeDetector", "Frame sent to web viewers");
            }
        }
        
        // Update UI stats every 30 frames to avoid too frequent updates
        if (totalFrames % 30 == 0) {
            updateStats();
        }
    }
    
    private void processCurrentFrame() {
        // This method is called when texture is updated
        // Frame processing is handled by ImageReader callback
    }
}
