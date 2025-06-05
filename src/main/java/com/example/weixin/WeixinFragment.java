package com.example.weixin;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WeixinFragment extends Fragment {
    EditText etip, etreceiveport, etzhenceport, etmess;
    Button btnok, btnsend;
    TextView tvshow;
    ImageView btnSearch, btnAdd;
    ListView chatListView;
    RadioButton rbUdp, rbTcp, rbTcpServer;
    RadioGroup connectionType;
    
    InetAddress inetaddress = null;
    DatagramPacket pack;
    DatagramSocket sendsocket = null, receivesocket = null;
    Message m;
    String ip;
    int receiveport, zhenceport;

    // 通信类型
    private static final int COMM_UDP = 0;
    private static final int COMM_TCP_CLIENT = 1;
    private static final int COMM_TCP_SERVER = 2;
    private int currentCommType = COMM_UDP;
    
    // TCP通信服务
    private MessageService messageService;

    // 添加聊天列表相关数据
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private ChatAdapter chatAdapter;
    
    // 示例聊天数据
    private static final String[][] DEMO_CHATS = {
        {"王家兴", "嗨，最近在忙什么？", "9:30"},
        {"李明", "明天的会议准备好了吗？", "10:15"},
        {"张三", "周末一起去爬山吧！", "昨天"},
        {"李四", "项目报告已发送至您的邮箱", "昨天"},
        {"王五", "请查收文件，谢谢", "星期日"},
        {"赵六", "下午三点会议室见", "星期六"},
        {"钱七", "生日快乐！", "7-22"},
        {"孙八", "感谢您的帮助", "7-20"}
    };

    static final int RECEIVE_WHAT = 1, SEND_WHAT = 2;

    // 消息重发和确认机制
    private static final int MAX_RETRIES = 3; // 最大重试次数
    private static final int ACK_TIMEOUT = 2000; // 确认超时时间(ms)
    private ConcurrentHashMap<String, SentMessage> pendingMessages = new ConcurrentHashMap<>();
    private AtomicInteger messageIdCounter = new AtomicInteger(0);
    private boolean isConnected = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        initView(view);
        initChatData();
        
        // 初始化TCP消息服务
        messageService = new MessageService(tcpMessageHandler);

        // 设置聊天列表适配器
        chatAdapter = new ChatAdapter();
        chatListView.setAdapter(chatAdapter);

        btnsend.setEnabled(false); // 初始状态下禁用发送按钮
        btnsend.setBackgroundColor(Color.LTGRAY);
        
        // 设置连接类型选择
        connectionType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_udp) {
                    currentCommType = COMM_UDP;
                    etzhenceport.setVisibility(View.VISIBLE);
                    etzhenceport.setHint("侦测端口");
                } else if (checkedId == R.id.rb_tcp) {
                    currentCommType = COMM_TCP_CLIENT;
                    etzhenceport.setVisibility(View.GONE);
                } else if (checkedId == R.id.rb_tcp_server) {
                    currentCommType = COMM_TCP_SERVER;
                    etzhenceport.setVisibility(View.VISIBLE);
                    etzhenceport.setHint("服务器端口");
                }
            }
        });

        btnok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 断开现有连接
                disconnectCurrent();
                
                ip = etip.getText().toString();
                String receivePortString = etreceiveport.getText().toString();

                // 添加输入验证
                if (currentCommType != COMM_TCP_SERVER && (ip.isEmpty() || receivePortString.isEmpty())) {
                    Toast.makeText(getActivity(), "请确保填写完整的IP地址和端口号", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    // 解析接收端口，如果为空则设为0
                    receiveport = receivePortString.isEmpty() ? 0 : Integer.parseInt(receivePortString);
                    
                    // 根据不同连接类型进行连接
                    switch (currentCommType) {
                        case COMM_UDP:
                            String zhencePortString = etzhenceport.getText().toString();
                            // 侦测端口是必填项
                            if (zhencePortString.isEmpty()) {
                                Toast.makeText(getActivity(), "请填写侦测端口", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            zhenceport = Integer.parseInt(zhencePortString);
                            connectUDP();
                            break;
                            
                        case COMM_TCP_CLIENT:
                            connectTCPClient();
                            break;
                            
                        case COMM_TCP_SERVER:
                            String serverPortString = etzhenceport.getText().toString();
                            if (serverPortString.isEmpty()) {
                                Toast.makeText(getActivity(), "请填写服务器端口", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            int serverPort = Integer.parseInt(serverPortString);
                            startTCPServer(serverPort);
                            break;
                    }
                    
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "端口号格式错误", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        btnsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etmess.getText().toString();
                if (message.isEmpty()) {
                    Toast.makeText(getActivity(), "请输入消息内容", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (!isConnected) {
                    Toast.makeText(getActivity(), "请先建立连接", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                sendMessage(message);
            }
        });
        
        // 添加搜索和添加按钮点击事件
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "搜索功能", Toast.LENGTH_SHORT).show();
            }
        });
        
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "添加联系人", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
    
    /**
     * 连接UDP
     */
    private void connectUDP() {
        // 先关闭之前的连接
        if (receivesocket != null && !receivesocket.isClosed()) {
            receivesocket.close();
        }

        // 确保所有字段都有值，如果为空则使用默认值
        if (ip == null || ip.isEmpty()) {
            ip = "0";
        }
        
        if (receiveport <= 0) {
            receiveport = 0;
        }
        
        if (zhenceport <= 0) {
            // 必须指定有效的侦测端口
            Toast.makeText(getActivity(), "侦测端口必须设置", Toast.LENGTH_SHORT).show();
            return;
        }

        // 开始接收消息
        isConnected = true;
        new Thread(new ReceiveMessage()).start();

        // 连接成功后启用发送按钮
        btnsend.setEnabled(true);
        btnsend.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));

        // 显示连接成功消息
        Toast.makeText(getActivity(), "UDP连接成功", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 连接TCP客户端
     */
    private void connectTCPClient() {
        // 确保IP不为空
        if (ip == null || ip.isEmpty()) {
            ip = "0";
        }
        
        if (receiveport <= 0) {
            Toast.makeText(getActivity(), "接收端口必须设置", Toast.LENGTH_SHORT).show();
            return;
        }

        messageService.connectToServer(ip, receiveport);
        // 连接结果会通过Handler返回
    }
    
    /**
     * 启动TCP服务器
     */
    private void startTCPServer(int port) {
        if (port <= 0) {
            Toast.makeText(getActivity(), "服务器端口必须设置", Toast.LENGTH_SHORT).show();
            return;
        }
        
        messageService.startServer(port);
        // 启动结果会通过Handler返回
    }
    
    /**
     * 断开当前连接
     */
    private void disconnectCurrent() {
        isConnected = false;
        
        // 关闭UDP连接
        if (receivesocket != null && !receivesocket.isClosed()) {
            receivesocket.close();
            receivesocket = null;
        }
        
        if (sendsocket != null && !sendsocket.isClosed()) {
            sendsocket.close();
            sendsocket = null;
        }
        
        // 关闭TCP连接
        if (messageService != null) {
            messageService.disconnect();
        }
        
        btnsend.setEnabled(false);
        btnsend.setBackgroundColor(Color.LTGRAY);
    }
    
    /**
     * 发送消息
     */
    private void sendMessage(final String content) {
        if (!isConnected) return;
        
        switch (currentCommType) {
            case COMM_UDP:
                new Thread(new SendMessage(content)).start();
                break;
                
            case COMM_TCP_CLIENT:
            case COMM_TCP_SERVER:
                messageService.sendMessage(content);
                break;
        }
    }

    public void initView(View view) {
        etip = view.findViewById(R.id.et_ip);
        etreceiveport = view.findViewById(R.id.et_port);
        etzhenceport = view.findViewById(R.id.et_zhenceport);
        etmess = view.findViewById(R.id.et_mess);
        btnok = view.findViewById(R.id.btn_ok);
        btnsend = view.findViewById(R.id.btn_send);
        tvshow = view.findViewById(R.id.tv_show);
        chatListView = view.findViewById(R.id.chat_list);
        btnSearch = view.findViewById(R.id.btn_search);
        btnAdd = view.findViewById(R.id.btn_add);
        
        connectionType = view.findViewById(R.id.connection_type);
        rbUdp = view.findViewById(R.id.rb_udp);
        rbTcp = view.findViewById(R.id.rb_tcp);
        rbTcpServer = view.findViewById(R.id.rb_tcp_server);
    }
    
    // 初始化聊天列表数据
    private void initChatData() {
        for (String[] chatData : DEMO_CHATS) {
            ChatMessage message = new ChatMessage();
            message.name = chatData[0];
            message.content = chatData[1];
            message.time = chatData[2];
            chatMessages.add(message);
        }
    }

    // 聊天消息数据类
    private static class ChatMessage {
        String name;
        String content;
        String time;
    }
    
    // TCP消息处理Handler
    private Handler tcpMessageHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String content = msg.obj != null ? msg.obj.toString() : "";
            
            switch (msg.what) {
                case MessageService.MSG_CONNECTED:
                    isConnected = true;
                    btnsend.setEnabled(true);
                    btnsend.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                    Toast.makeText(getActivity(), "TCP连接成功", Toast.LENGTH_SHORT).show();
                    tvshow.append("系统消息: " + content + "\n");
                    break;
                    
                case MessageService.MSG_CONNECTION_FAILED:
                    isConnected = false;
                    btnsend.setEnabled(false);
                    btnsend.setBackgroundColor(Color.LTGRAY);
                    Toast.makeText(getActivity(), "TCP连接失败", Toast.LENGTH_SHORT).show();
                    tvshow.append("系统错误: " + content + "\n");
                    break;
                    
                case MessageService.MSG_RECEIVE:
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String currentTime = sdf.format(new Date());
                    tvshow.append("[" + currentTime + "] 接收: " + content + "\n");
                    break;
                    
                case MessageService.MSG_SEND_SUCCESS:
                    sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    currentTime = sdf.format(new Date());
                    tvshow.append("[" + currentTime + "] 我: " + content + "\n");
                    etmess.setText(""); // 清除输入框
                    break;
                    
                case MessageService.MSG_SEND_FAILED:
                    Toast.makeText(getActivity(), "发送失败: " + content, Toast.LENGTH_SHORT).show();
                    break;
                    
                case MessageService.MSG_SERVER_STARTED:
                    isConnected = true;
                    btnsend.setEnabled(true);
                    btnsend.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                    Toast.makeText(getActivity(), "TCP服务器已启动", Toast.LENGTH_SHORT).show();
                    tvshow.append("系统消息: " + content + "\n");
                    break;
                    
                case MessageService.MSG_SERVER_ERROR:
                    isConnected = false;
                    btnsend.setEnabled(false);
                    btnsend.setBackgroundColor(Color.LTGRAY);
                    Toast.makeText(getActivity(), "TCP服务器错误", Toast.LENGTH_SHORT).show();
                    tvshow.append("系统错误: " + content + "\n");
                    break;
            }
        }
    };
    
    // 聊天列表适配器
    private class ChatAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return chatMessages.size();
        }

        @Override
        public Object getItem(int position) {
            return chatMessages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.chat_list_item, parent, false);
                holder = new ViewHolder();
                holder.avatarView = convertView.findViewById(R.id.chat_avatar);
                holder.nameView = convertView.findViewById(R.id.chat_name);
                holder.contentView = convertView.findViewById(R.id.chat_content);
                holder.timeView = convertView.findViewById(R.id.chat_time);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            
            ChatMessage message = chatMessages.get(position);
            holder.nameView.setText(message.name);
            holder.contentView.setText(message.content);
            holder.timeView.setText(message.time);
            
            // 设置点击效果
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 创建点击动画
                    AlphaAnimation alphaAnimation = new AlphaAnimation(0.7f, 1.0f);
                    alphaAnimation.setDuration(300);
                    v.startAnimation(alphaAnimation);
                    
                    Toast.makeText(getActivity(), "打开与" + message.name + "的聊天", Toast.LENGTH_SHORT).show();
                }
            });
            
            return convertView;
        }
        
        class ViewHolder {
            ImageView avatarView;
            TextView nameView;
            TextView contentView;
            TextView timeView;
        }
    }

    // 消息数据结构
    private class SentMessage {
        String id;
        String content;
        int retries = 0;
        boolean acknowledged = false;
        long sentTime;

        SentMessage(String id, String content) {
            this.id = id;
            this.content = content;
            this.sentTime = System.currentTimeMillis();
        }
    }

    // 消息格式: TYPE|MESSAGE_ID|CONTENT
    // TYPE: MSG(消息), ACK(确认)
    public class SendMessage implements Runnable {
        private String messageContent;
        
        public SendMessage(String messageContent) {
            this.messageContent = messageContent;
        }
        
        @Override
        public void run() {
            if (messageContent == null || messageContent.isEmpty()) {
                return;
            }
            
            // 清除输入框
            etmess.post(() -> etmess.getText().clear());
            
            // 生成消息ID和完整消息
            final String messageId = UUID.randomUUID().toString().substring(0, 8);
            final String fullMessage = "MSG|" + messageId + "|" + messageContent;
            
            // 记录待确认消息
            SentMessage sentMessage = new SentMessage(messageId, messageContent);
            pendingMessages.put(messageId, sentMessage);
            
            // 发送消息
            boolean sent = sendUdpMessage(fullMessage);
            
            if (sent) {
                // 开始消息确认和重发机制
                new Thread(() -> {
                    try {
                        int attempts = 0;
                        while (attempts < MAX_RETRIES) {
                            // 等待确认
                            Thread.sleep(ACK_TIMEOUT);
                            
                            // 检查消息是否已确认
                            SentMessage msg = pendingMessages.get(messageId);
                            if (msg != null && !msg.acknowledged) {
                                // 未确认，重发消息
                                msg.retries++;
                                attempts++;
                                
                                if (attempts < MAX_RETRIES) {
                                    sendUdpMessage(fullMessage);
                                    int finalAttempts = attempts;
                                    getActivity().runOnUiThread(() -> {
                                        Toast.makeText(getActivity(), "重发消息: " + finalAttempts, Toast.LENGTH_SHORT).show();
                                    });
                                } else {
                                    // 超过最大重试次数
                                    getActivity().runOnUiThread(() -> {
                                        Toast.makeText(getActivity(), "消息发送失败", Toast.LENGTH_SHORT).show();
                                    });
                                    pendingMessages.remove(messageId);
                                }
                            } else {
                                // 已确认，退出循环
                                break;
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
                
                // 获取当前时间并显示消息
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String currentTime = sdf.format(new Date());
                
                Bundle bundle = new Bundle();
                bundle.putString("send", messageContent);
                bundle.putString("time", currentTime);
                bundle.putString("message_id", messageId);
                Message m = new Message();
                m.what = SEND_WHAT;
                m.setData(bundle);
                
                handler.sendMessage(m);
            }
        }
        
        private boolean sendUdpMessage(String message) {
            try {
                byte[] buffer = message.getBytes(StandardCharsets.UTF_8);
                inetaddress = InetAddress.getByName(ip);
                pack = new DatagramPacket(buffer, buffer.length, inetaddress, receiveport);
                
                if (sendsocket == null || sendsocket.isClosed()) {
                sendsocket = new DatagramSocket();
                    sendsocket.setSoTimeout(1000); // 设置超时时间
                }
                
                sendsocket.send(pack);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), "发送失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
                return false;
            }
        }
    }

    public class ReceiveMessage implements Runnable {
        @Override
        public void run() {
            try {
                // 初始化接收socket
                if (receivesocket == null || receivesocket.isClosed()) {
                receivesocket = new DatagramSocket(null);
                receivesocket.setReuseAddress(true);
                    receivesocket.setSoTimeout(5000); // 设置超时，避免阻塞
                receivesocket.bind(new InetSocketAddress(zhenceport));
                }
                
                byte[] buffer = new byte[8192];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                
                while (isConnected && currentCommType == COMM_UDP) {
                    try {
                        // 接收数据包
                        receivesocket.receive(packet);
                        String receivedData = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                        String senderIp = packet.getAddress().getHostAddress();
                        
                        // 解析消息
                        String[] parts = receivedData.split("\\|", 3);
                        if (parts.length >= 2) {
                            String messageType = parts[0];
                            String messageId = parts[1];
                            
                            if ("MSG".equals(messageType) && parts.length == 3) {
                                // 接收到消息，发送确认
                                String messageContent = parts[2];
                                sendAcknowledgment(messageId, packet.getAddress(), packet.getPort());
                                
                                // 处理接收到的消息
                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                String currentTime = sdf.format(new Date());
                                
                                Bundle bundle = new Bundle();
                                bundle.putString("receiveip", senderIp);
                                bundle.putString("receivedata", messageContent);
                                bundle.putString("time", currentTime);
                                
                                Message m = new Message();
                                m.what = RECEIVE_WHAT;
                                m.setData(bundle);
                                
                                handler.sendMessage(m);
                            }
                            else if ("ACK".equals(messageType)) {
                                // 处理确认消息
                                SentMessage sentMessage = pendingMessages.get(messageId);
                                if (sentMessage != null) {
                                    sentMessage.acknowledged = true;
                                    pendingMessages.remove(messageId);
                                }
                            }
                        }
                    } catch (SocketTimeoutException e) {
                        // 接收超时，继续循环
                        continue;
                    } catch (IOException e) {
                        if (!isConnected || currentCommType != COMM_UDP) {
                            break; // 如果连接已关闭，退出循环
                        }
                        e.printStackTrace();
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            } finally {
                if (receivesocket != null && !receivesocket.isClosed()) {
                    receivesocket.close();
                }
            }
        }
        
        private void sendAcknowledgment(String messageId, InetAddress address, int port) {
            try {
                String ackMessage = "ACK|" + messageId;
                byte[] ackData = ackMessage.getBytes(StandardCharsets.UTF_8);
                DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, address, port);
                
                // 使用临时socket发送确认
                DatagramSocket ackSocket = new DatagramSocket();
                ackSocket.send(ackPacket);
                ackSocket.close();
            } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (getActivity() == null) return;
            
            String timeStamp = msg.getData().getString("time", "");
            
            switch (msg.what) {
                case RECEIVE_WHAT:
                    tvshow.append("[" + timeStamp + "] " + msg.getData().getString("receiveip") + ": " +
                            msg.getData().getString("receivedata") + "\n");
                    break;
                case SEND_WHAT:
                    String messageId = msg.getData().getString("message_id", "");
                    String statusIndicator = "";
                    SentMessage sentMessage = pendingMessages.get(messageId);
                    if (sentMessage != null && !sentMessage.acknowledged) {
                        statusIndicator = " ⏳"; // 消息发送中，等待确认
                    }
                    tvshow.append("[" + timeStamp + "] 我: " + msg.getData().getString("send") + statusIndicator + "\n");
                    break;
            }
        }
    };
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectCurrent();
    }
}
