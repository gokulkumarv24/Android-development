package com.edgedetector;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class TestWebSocketActivity extends Activity {
    private static final String TAG = "WebSocketTest";
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    
    private TextView statusText;
    private TextView statusLabel;
    private TextView ipAddressText;
    private View statusIndicator;
    private Button startButton;
    private Button stopButton;
    
    private EdgeDetectorWebSocketServer webSocketServer;
    private boolean serverRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_websocket);
        
        // Initialize views
        statusText = findViewById(R.id.status_text);
        statusLabel = findViewById(R.id.status_label);
        ipAddressText = findViewById(R.id.ip_address);
        statusIndicator = findViewById(R.id.status_indicator);
        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);
        
        // Set up button listeners
        startButton.setOnClickListener(v -> testWebSocketServer());
        stopButton.setOnClickListener(v -> stopWebSocketServer());
        
        // Initialize UI state
        updateConnectionStatus(false);
        updateStatus("WebSocket Server Test\n\nReady to start...\n");
        
        // Auto-start test
        testWebSocketServer();
    }
    
    private void updateConnectionStatus(boolean isOnline) {
        runOnUiThread(() -> {
            // Animate status change
            Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in);
            
            if (isOnline) {
                statusIndicator.setBackgroundResource(R.drawable.status_online);
                statusLabel.setText("Server Online");
                ipAddressText.setText(getLocalIpAddress() + ":8765");
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                
                // Start pulse animation for online indicator
                Animation pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_animation);
                statusIndicator.startAnimation(pulseAnimation);
            } else {
                statusIndicator.setBackgroundResource(R.drawable.status_offline);
                statusLabel.setText("Server Offline");
                ipAddressText.setText("");
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                
                // Stop any animations
                statusIndicator.clearAnimation();
            }
            
            // Animate the status section
            findViewById(R.id.status_label).startAnimation(slideIn);
        });
    }
    
    private void testWebSocketServer() {
        updateStatus("Testing WebSocket Server...\n");
        
        try {
            if (webSocketServer != null) {
                webSocketServer.stop();
            }
            
            updateStatus("Creating WebSocket server...\n");
            webSocketServer = new EdgeDetectorWebSocketServer();
            
            updateStatus("Starting WebSocket server on port 8765...\n");
            webSocketServer.start();
            
            // Wait a moment for server to start
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    runOnUiThread(() -> {
                        if (webSocketServer.getAddress() != null) {
                            updateStatus("✅ SUCCESS!\n");
                            updateStatus("WebSocket server running on: " + webSocketServer.getAddress() + "\n");
                            updateStatus("Port: " + webSocketServer.getPort() + "\n");
                            updateStatus("\nYou can now connect from web browser!\n");
                            updateStatus("Use IP: " + getLocalIpAddress() + ":8765\n\n");
                            updateStatus("🌐 Web Interface: http://" + getLocalIpAddress() + ":8082\n");
                            updateStatus("📱 Manual Connection: http://" + getLocalIpAddress() + ":8082/manual.html\n");
                            updateConnectionStatus(true);
                            serverRunning = true;
                        } else {
                            updateStatus("❌ FAILED!\n");
                            updateStatus("Server address is null\n");
                            updateConnectionStatus(false);
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        updateStatus("❌ ERROR: " + e.getMessage() + "\n");
                        updateConnectionStatus(false);
                        Log.e(TAG, "WebSocket server error", e);
                    });
                }
            }).start();
            
        } catch (Exception e) {
            updateStatus("❌ EXCEPTION: " + e.getMessage() + "\n");
            updateConnectionStatus(false);
            Log.e(TAG, "Failed to start WebSocket server", e);
        }
    }
    
    private void stopWebSocketServer() {
        try {
            if (webSocketServer != null) {
                webSocketServer.stop();
                updateStatus("✋ WebSocket server stopped.\n");
                updateConnectionStatus(false);
                serverRunning = false;
            }
        } catch (Exception e) {
            updateStatus("Error stopping server: " + e.getMessage() + "\n");
        }
    }
    
    private void updateStatus(String message) {
        statusText.append(message);
        Log.i(TAG, message.trim());
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
            Log.e(TAG, "Error getting IP address", e);
        }
        return "unknown";
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketServer != null) {
            try {
                webSocketServer.stop();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping server on destroy", e);
            }
        }
    }
}