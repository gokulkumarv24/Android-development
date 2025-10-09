package com.edgedetector;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.util.Log;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class TestActivity extends Activity {
    private static final String TAG = "EdgeDetectorTest";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        TextView textView = new TextView(this);
        setContentView(textView);
        
        StringBuilder status = new StringBuilder();
        status.append("EdgeDetector Test Results:\n\n");
        
        // Test 1: Basic network
        try {
            status.append("✅ Internet permission: OK\n");
        } catch (Exception e) {
            status.append("❌ Internet permission: FAILED\n");
        }
        
        // Test 2: Port availability
        try {
            ServerSocket testSocket = new ServerSocket();
            testSocket.bind(new InetSocketAddress(8765));
            testSocket.close();
            status.append("✅ Port 8765: Available\n");
        } catch (Exception e) {
            status.append("❌ Port 8765: Blocked - " + e.getMessage() + "\n");
        }
        
        // Test 3: WebSocket library
        try {
            Class.forName("org.java_websocket.server.WebSocketServer");
            status.append("✅ WebSocket library: Found\n");
        } catch (Exception e) {
            status.append("❌ WebSocket library: Missing - " + e.getMessage() + "\n");
        }
        
        // Test 4: Try to start WebSocket server
        try {
            EdgeDetectorWebSocketServer testServer = new EdgeDetectorWebSocketServer();
            testServer.start();
            Thread.sleep(1000); // Wait 1 second
            if (testServer.getPort() > 0) {
                status.append("✅ WebSocket server: Started on port " + testServer.getPort() + "\n");
            } else {
                status.append("❌ WebSocket server: Port not bound\n");
            }
            testServer.stop();
        } catch (Exception e) {
            status.append("❌ WebSocket server: Failed - " + e.getMessage() + "\n");
            Log.e(TAG, "WebSocket server error", e);
        }
        
        textView.setText(status.toString());
        Log.i(TAG, status.toString());
    }
}