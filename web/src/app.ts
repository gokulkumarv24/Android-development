interface FrameStats {
    fps: number;
    resolution: string;
    mode: string;
    frameCount: number;
    processingTime: number;
    lastUpdated: string;
    connectionQuality: string;
}

interface ConnectionConfig {
    url: string;
    autoReconnect: boolean;
    reconnectInterval: number;
    maxReconnectAttempts: number;
}

class EdgeDetectorViewer {
    private frameImage!: HTMLImageElement;
    private loadingMessage!: HTMLDivElement;
    private refreshBtn!: HTMLButtonElement;
    private toggleModeBtn!: HTMLButtonElement;
    private fpsValue!: HTMLDivElement;
    private resolutionValue!: HTMLDivElement;
    private modeValue!: HTMLDivElement;
    private frameCountValue!: HTMLDivElement;
    private processingTimeValue!: HTMLDivElement;
    private lastUpdatedValue!: HTMLDivElement;
    private qualityValue!: HTMLDivElement;
    private connectionStatus!: HTMLDivElement;
    private stats!: FrameStats;
    private currentMode: 'edge' | 'raw' = 'edge';
    private webSocket: WebSocket | null = null;
    private connectionConfig: ConnectionConfig;
    private reconnectAttempts: number = 0;
    private isConnected: boolean = false;
    private lastFrameTime: number = 0;
    private frameTimeouts: number[] = [];

    // Sample base64 edge-detected image (small demo image)
    private sampleEdgeFrame = 'data:image/png;base64,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';
    
    constructor() {
        // Initialize connection configuration
        this.connectionConfig = {
            url: this.getDefaultWebSocketUrl(),
            autoReconnect: true,
            reconnectInterval: 5000,
            maxReconnectAttempts: 10
        };
        
        // Initialize DOM elements
        this.frameImage = document.getElementById('frameImage') as HTMLImageElement;
        this.loadingMessage = document.getElementById('loadingMessage') as HTMLDivElement;
        this.refreshBtn = document.getElementById('refreshBtn') as HTMLButtonElement;
        this.toggleModeBtn = document.getElementById('toggleModeBtn') as HTMLButtonElement;
        this.fpsValue = document.getElementById('fpsValue') as HTMLDivElement;
        this.resolutionValue = document.getElementById('resolutionValue') as HTMLDivElement;
        this.modeValue = document.getElementById('modeValue') as HTMLDivElement;
        this.frameCountValue = document.getElementById('frameCountValue') as HTMLDivElement;
        this.processingTimeValue = document.getElementById('processingTimeValue') as HTMLDivElement;
        this.lastUpdatedValue = document.getElementById('lastUpdatedValue') as HTMLDivElement;
        this.qualityValue = document.getElementById('qualityValue') as HTMLDivElement;
        this.connectionStatus = document.getElementById('connectionStatus') as HTMLDivElement;
        
        // Initialize stats
        this.stats = {
            fps: 0,
            resolution: '--',
            mode: 'Edge Detection',
            frameCount: 0,
            processingTime: 0,
            lastUpdated: '--',
            connectionQuality: 'Disconnected'
        };
        
        this.setupEventListeners();
        this.initializeConnectionUI();
        this.connectWebSocket();
        this.loadSampleFrame();
    }
    
    private setupEventListeners(): void {
        this.refreshBtn.addEventListener('click', () => {
            this.refreshFrame();
        });
        
        this.toggleModeBtn.addEventListener('click', () => {
            this.toggleMode();
        });
        
        // Add manual connection button if it doesn't exist
        this.createManualConnectionButton();
    }
    
    private getDefaultWebSocketUrl(): string {
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const host = window.location.hostname || 'localhost';
        return `${protocol}//${host}:8765`;
    }
    
    private initializeConnectionUI(): void {
        // Create connection status UI elements if they don't exist
        if (!this.connectionStatus) {
            this.createConnectionStatusElements();
        }
        this.updateConnectionStatus('Connecting...', 'offline');
    }
    
    private createConnectionStatusElements(): void {
        // This method creates connection status elements dynamically if needed
        const statusBar = document.querySelector('.status-bar');
        if (statusBar && !document.getElementById('connectionStatus')) {
            const statusElement = document.createElement('div');
            statusElement.id = 'connectionStatus';
            statusElement.className = 'status-item status-offline';
            statusElement.innerHTML = '<i class="fas fa-wifi"></i><span>Connecting...</span>';
            statusBar.appendChild(statusElement);
            this.connectionStatus = statusElement;
        }
    }
    
    private createManualConnectionButton(): void {
        const controls = document.querySelector('.controls');
        if (controls && !document.getElementById('manualConnectBtn')) {
            const manualBtn = document.createElement('button');
            manualBtn.id = 'manualConnectBtn';
            manualBtn.className = 'btn';
            manualBtn.style.background = 'linear-gradient(45deg, #8b5cf6, #a855f7)';
            manualBtn.innerHTML = '<i class="fas fa-plug"></i> Manual Connect';
            manualBtn.addEventListener('click', () => this.showManualConnectionDialog());
            controls.appendChild(manualBtn);
        }
    }
    
    private showManualConnectionDialog(): void {
        const url = prompt('Enter WebSocket URL (e.g., ws://192.168.1.100:8765):', this.connectionConfig.url);
        if (url && url.trim()) {
            this.connectionConfig.url = url.trim();
            this.reconnectAttempts = 0;
            this.disconnectWebSocket();
            this.connectWebSocket();
        }
    }
    
    private initializeStats(): void {
        this.stats = {
            fps: 15.2,
            resolution: '640 x 480',
            mode: 'Edge Detection',
            frameCount: 1247,
            processingTime: 8.3,
            lastUpdated: new Date().toLocaleTimeString(),
            connectionQuality: 'Demo Mode'
        };
        this.updateStatsDisplay();
    }
    
    private updateConnectionStatus(message: string, status: 'online' | 'offline'): void {
        if (this.connectionStatus) {
            const icon = status === 'online' ? 'fas fa-wifi' : 'fas fa-wifi-slash';
            const className = status === 'online' ? 'status-item status-online' : 'status-item status-offline';
            this.connectionStatus.className = className;
            this.connectionStatus.innerHTML = `<i class="${icon}"></i><span>${message}</span>`;
        }
        
        this.stats.connectionQuality = status === 'online' ? 'Excellent' : 'Disconnected';
        this.isConnected = status === 'online';
        this.updateStatsDisplay();
    }
    
    private disconnectWebSocket(): void {
        if (this.webSocket) {
            this.webSocket.close();
            this.webSocket = null;
        }
        this.updateConnectionStatus('Disconnected', 'offline');
    }
    
    private loadSampleFrame(): void {
        this.frameImage.onload = () => {
            this.loadingMessage.style.display = 'none';
            this.frameImage.style.display = 'block';
            this.stats.lastUpdated = new Date().toLocaleTimeString();
            this.updateStatsDisplay();
        };
        
        this.frameImage.src = this.sampleEdgeFrame;
    }
    
    private refreshFrame(): void {
        this.loadingMessage.style.display = 'block';
        this.frameImage.style.display = 'none';
        
        // Simulate frame refresh with updated stats
        setTimeout(() => {
            this.stats.frameCount += Math.floor(Math.random() * 10) + 1;
            this.stats.fps = Math.round((Math.random() * 5 + 12) * 10) / 10;
            this.stats.processingTime = Math.round((Math.random() * 5 + 5) * 10) / 10;
            
            this.loadSampleFrame();
        }, 500);
    }
    
    private toggleMode(): void {
        this.currentMode = this.currentMode === 'edge' ? 'raw' : 'edge';
        this.stats.mode = this.currentMode === 'edge' ? 'Edge Detection' : 'Raw Camera Feed';
        
        // In a real implementation, this would switch between different image sources
        this.refreshFrame();
    }
    
    private updateStatsDisplay(): void {
        this.fpsValue.textContent = this.stats.fps.toString();
        this.resolutionValue.textContent = this.stats.resolution;
        this.modeValue.textContent = this.stats.mode;
        this.frameCountValue.textContent = this.stats.frameCount.toLocaleString();
        this.processingTimeValue.textContent = `${this.stats.processingTime} ms`;
        this.lastUpdatedValue.textContent = this.stats.lastUpdated;
        if (this.qualityValue) {
            this.qualityValue.textContent = this.stats.connectionQuality;
        }
    }

    private connectWebSocket(): void {
        if (this.webSocket && this.webSocket.readyState === WebSocket.OPEN) {
            return; // Already connected
        }
        
        this.updateConnectionStatus('Connecting...', 'offline');
        
        try {
            this.webSocket = new WebSocket(this.connectionConfig.url);
            
            this.webSocket.onopen = () => {
                console.log('🔌 Connected to Edge Detector server at', this.connectionConfig.url);
                this.updateConnectionStatus('Connected', 'online');
                this.reconnectAttempts = 0;
                
                // Request initial frame
                if (this.webSocket) {
                    this.webSocket.send(JSON.stringify({ type: 'request_frame' }));
                }
            };

            this.webSocket.onmessage = (event) => {
                try {
                    const message = JSON.parse(event.data);
                    this.handleWebSocketMessage(message);
                } catch (error) {
                    console.error('Failed to parse WebSocket message:', error);
                }
            };

            this.webSocket.onerror = (error) => {
                console.error('WebSocket error:', error);
                this.updateConnectionStatus('Connection Error', 'offline');
            };

            this.webSocket.onclose = (event) => {
                console.log('WebSocket connection closed:', event.code, event.reason);
                this.updateConnectionStatus('Disconnected', 'offline');
                
                if (this.connectionConfig.autoReconnect && 
                    this.reconnectAttempts < this.connectionConfig.maxReconnectAttempts) {
                    this.scheduleReconnect();
                }
            };
            
        } catch (error) {
            console.error('Failed to create WebSocket connection:', error);
            this.updateConnectionStatus('Connection Failed', 'offline');
            if (this.connectionConfig.autoReconnect) {
                this.scheduleReconnect();
            }
        }
    }
    
    private handleWebSocketMessage(message: any): void {
        const currentTime = Date.now();
        
        switch (message.type) {
            case 'frame':
                this.updateFrameFromSource(`data:image/jpeg;base64,${message.data}`);
                this.updateFrameStats(message.stats || {});
                break;
                
            case 'stats':
                this.updateFrameStats(message.data);
                break;
                
            case 'status':
                console.log('Server status:', message.data);
                break;
                
            default:
                console.log('Unknown message type:', message.type);
        }
        
        // Calculate FPS based on frame reception rate
        if (this.lastFrameTime > 0) {
            const timeDiff = currentTime - this.lastFrameTime;
            if (timeDiff > 0) {
                this.stats.fps = Math.round((1000 / timeDiff) * 10) / 10;
            }
        }
        this.lastFrameTime = currentTime;
    }
    
    private updateFrameStats(newStats: Partial<FrameStats>): void {
        this.stats = { ...this.stats, ...newStats };
        this.stats.lastUpdated = new Date().toLocaleTimeString();
        this.updateStatsDisplay();
    }
    
    private scheduleReconnect(): void {
        this.reconnectAttempts++;
        const delay = Math.min(this.connectionConfig.reconnectInterval * this.reconnectAttempts, 30000);
        
        this.updateConnectionStatus(`Reconnecting in ${Math.ceil(delay/1000)}s... (${this.reconnectAttempts}/${this.connectionConfig.maxReconnectAttempts})`, 'offline');
        
        setTimeout(() => {
            if (!this.isConnected) {
                this.connectWebSocket();
            }
        }, delay);
    }
    
    // Method to update frame from external source (e.g., WebSocket, HTTP endpoint)
    public updateFrameFromSource(base64Data: string, newStats?: Partial<FrameStats>): void {
        if (newStats) {
            this.stats = { ...this.stats, ...newStats };
        }
        
        this.frameImage.onload = () => {
            this.loadingMessage.style.display = 'none';
            this.frameImage.style.display = 'block';
            this.stats.lastUpdated = new Date().toLocaleTimeString();
            this.stats.frameCount++;
            this.updateStatsDisplay();
        };
        
        this.frameImage.onerror = () => {
            console.error('Failed to load frame image');
            this.loadingMessage.style.display = 'block';
            this.frameImage.style.display = 'none';
        };
        
        this.frameImage.src = base64Data;
    }
    
    // Public methods for external control
    public connect(url?: string): void {
        if (url) {
            this.connectionConfig.url = url;
        }
        this.reconnectAttempts = 0;
        this.disconnectWebSocket();
        this.connectWebSocket();
    }
    
    public disconnect(): void {
        this.connectionConfig.autoReconnect = false;
        this.disconnectWebSocket();
    }
    
    public getConnectionStatus(): { connected: boolean; url: string; attempts: number } {
        return {
            connected: this.isConnected,
            url: this.connectionConfig.url,
            attempts: this.reconnectAttempts
        };
    }
    
    public setAutoReconnect(enabled: boolean): void {
        this.connectionConfig.autoReconnect = enabled;
    }
    
    // Method to handle QR code connection data
    public handleQRConnection(connectionData: any): void {
        try {
            const data = typeof connectionData === 'string' ? JSON.parse(connectionData) : connectionData;
            if (data.type === 'edgedetector_connection' && data.expectedServer) {
                console.log('🔗 QR Code connection detected');
                this.connect(data.expectedServer);
            }
        } catch (error) {
            console.error('Failed to parse QR connection data:', error);
        }
    }
}

// Initialize the viewer when the DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    const viewer = new EdgeDetectorViewer();
    
    // Expose viewer to global scope for external access
    (window as any).edgeDetectorViewer = viewer;
    
    // Handle URL parameters for automatic connection
    const urlParams = new URLSearchParams(window.location.search);
    const wsUrl = urlParams.get('ws');
    if (wsUrl) {
        console.log('🔗 Auto-connecting to WebSocket from URL parameter:', wsUrl);
        viewer.connect(wsUrl);
    }
    
    // Listen for QR code scan events (if QR scanner is implemented)
    window.addEventListener('qr-code-scanned', (event: any) => {
        viewer.handleQRConnection(event.detail);
    });
    
    // Listen for manual connection events
    window.addEventListener('manual-connection', (event: any) => {
        viewer.connect(event.detail.url);
    });
    
    // Expose connection methods globally for easier debugging and external control
    (window as any).connectToEdgeDetector = (url: string) => viewer.connect(url);
    (window as any).disconnectFromEdgeDetector = () => viewer.disconnect();
    (window as any).getEdgeDetectorStatus = () => viewer.getConnectionStatus();
    
    console.log('🎨 Edge Detector Web Viewer initialized');
    console.log('� Manual connection: Use connectToEdgeDetector("ws://IP:8765")');
    console.log('🔗 QR code connection: Point camera at QR code from connection page');
    console.log('📊 Connection status: Use getEdgeDetectorStatus()');
});