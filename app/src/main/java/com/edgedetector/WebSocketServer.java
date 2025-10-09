package com.edgedetector;

import android.util.Base64;
import android.util.Log;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EdgeDetectorWebSocketServer extends WebSocketServer {
    private static final String TAG = "WebSocketServer";
    private Set<WebSocket> connections;
    private static final int PORT = 8765;

    public EdgeDetectorWebSocketServer() {
        super(new InetSocketAddress(PORT));
        connections = Collections.synchronizedSet(new HashSet<>());
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn);
        Log.d(TAG, "New connection established");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
        Log.d(TAG, "Connection closed");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // Handle incoming messages if needed
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.e(TAG, "WebSocket error: " + ex.getMessage());
        if (conn != null) {
            connections.remove(conn);
        }
    }

    @Override
    public void onStart() {
        Log.d(TAG, "WebSocket server started on port " + PORT);
    }

    public void broadcastFrame(byte[] frameData) {
        if (connections.isEmpty()) return;
        
        String base64Frame = Base64.encodeToString(frameData, Base64.NO_WRAP);
        String frameMessage = "{\"type\":\"frame\",\"data\":\"" + base64Frame + "\"}";
        
        synchronized (connections) {
            for (WebSocket conn : connections) {
                conn.send(frameMessage);
            }
        }
    }
}