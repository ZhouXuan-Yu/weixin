package com.example.weixin;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 消息服务类，用于处理TCP通信
 */
public class MessageService {
    private static final String TAG = "MessageService";
    private static final int TCP_SERVER_PORT = 9000;
    private static final int TCP_SOCKET_TIMEOUT = 5000;
    
    // 消息类型常量
    public static final int MSG_CONNECTED = 101;
    public static final int MSG_CONNECTION_FAILED = 102;
    public static final int MSG_RECEIVE = 103;
    public static final int MSG_SEND_SUCCESS = 104;
    public static final int MSG_SEND_FAILED = 105;
    public static final int MSG_SERVER_STARTED = 106;
    public static final int MSG_SERVER_ERROR = 107;
    
    private ExecutorService executorService;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private Handler handler;
    private boolean isServerRunning = false;
    private boolean isConnected = false;
    
    /**
     * 初始化消息服务
     * @param handler 用于接收消息事件的Handler
     */
    public MessageService(Handler handler) {
        this.handler = handler;
        this.executorService = Executors.newCachedThreadPool();
    }
    
    /**
     * 启动TCP服务器
     * @param port 服务器端口号
     */
    public void startServer(final int port) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket();
                    serverSocket.setReuseAddress(true);
                    serverSocket.bind(new InetSocketAddress(port));
                    isServerRunning = true;
                    
                    // 发送服务器启动消息
                    sendHandlerMessage(MSG_SERVER_STARTED, "TCP服务器已启动在端口: " + port);
                    
                    // 等待客户端连接
                    while (isServerRunning) {
                        try {
                            Socket socket = serverSocket.accept();
                            handleClientConnection(socket);
                        } catch (IOException e) {
                            if (isServerRunning) {
                                // 只有在服务器还在运行时才报告错误
                                Log.e(TAG, "接受客户端连接时出错", e);
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "启动TCP服务器出错", e);
                    sendHandlerMessage(MSG_SERVER_ERROR, "启动TCP服务器出错: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 处理客户端连接
     */
    private void handleClientConnection(final Socket socket) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    socket.setSoTimeout(TCP_SOCKET_TIMEOUT);
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    
                    // 发送连接成功消息
                    String clientAddress = socket.getInetAddress().getHostAddress();
                    sendHandlerMessage(MSG_CONNECTED, "与 " + clientAddress + " 建立连接");
                    
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // 处理接收到的消息
                        sendHandlerMessage(MSG_RECEIVE, line);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "客户端连接处理出错", e);
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "关闭Socket出错", e);
                    }
                }
            }
        });
    }
    
    /**
     * 作为客户端连接到服务器
     * @param host 服务器地址
     * @param port 服务器端口
     */
    public void connectToServer(final String host, final int port) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    clientSocket = new Socket();
                    clientSocket.connect(new InetSocketAddress(host, port), TCP_SOCKET_TIMEOUT);
                    clientSocket.setSoTimeout(TCP_SOCKET_TIMEOUT);
                    
                    reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                    isConnected = true;
                    
                    // 发送连接成功消息
                    sendHandlerMessage(MSG_CONNECTED, "已连接到服务器 " + host + ":" + port);
                    
                    // 开始接收消息
                    String line;
                    while (isConnected && (line = reader.readLine()) != null) {
                        sendHandlerMessage(MSG_RECEIVE, line);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "连接服务器出错", e);
                    sendHandlerMessage(MSG_CONNECTION_FAILED, "连接服务器出错: " + e.getMessage());
                } finally {
                    disconnect();
                }
            }
        });
    }
    
    /**
     * 发送消息
     * @param message 要发送的消息
     */
    public void sendMessage(final String message) {
        if (!isConnected || writer == null) {
            sendHandlerMessage(MSG_SEND_FAILED, "未连接到服务器");
            return;
        }
        
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    writer.write(message + "\n");
                    writer.flush();
                    sendHandlerMessage(MSG_SEND_SUCCESS, message);
                } catch (IOException e) {
                    Log.e(TAG, "发送消息出错", e);
                    sendHandlerMessage(MSG_SEND_FAILED, "发送消息出错: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 断开连接
     */
    public void disconnect() {
        isConnected = false;
        isServerRunning = false;
        
        try {
            if (reader != null) {
                reader.close();
                reader = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "关闭读取器出错", e);
        }
        
        try {
            if (writer != null) {
                writer.close();
                writer = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "关闭写入器出错", e);
        }
        
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                clientSocket = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "关闭客户端Socket出错", e);
        }
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                serverSocket = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "关闭服务器Socket出错", e);
        }
    }
    
    /**
     * 向Handler发送消息
     */
    private void sendHandlerMessage(int what, String data) {
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.what = what;
            msg.obj = data;
            handler.sendMessage(msg);
        }
    }
    
    /**
     * 检查是否已连接
     */
    public boolean isConnected() {
        return isConnected;
    }
} 