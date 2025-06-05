
```bash
# 默认模式（同时启动UDP和TCP服务器）
python network_test.py

# 仅启动UDP服务器
python network_test.py --mode udp

# 仅启动TCP服务器
python network_test.py --mode tcp

# 指定UDP端口（默认是8888）
python network_test.py --udp-port 8888

# 指定TCP端口（默认是9999）
python network_test.py --tcp-port 9999

# 指定监听地址（默认是0.0.0.0，监听所有网卡）
python network_test.py --host 192.168.31.14
```

### 参数说明

- `--mode`: 通信模式，可选值为 `udp`、`tcp` 或 `both`（默认为`both`）
- `--host`: 监听地址，默认为`0.0.0.0`（所有网络接口）
- `--udp-port`: UDP服务器端口，默认为`8888`
- `--tcp-port`: TCP服务器端口，默认为`9999`

## 客户端使用方法 (network_client.py)

客户端工具可以模拟Android应用，向服务器发送UDP或TCP消息，用于测试通信是否正常。

### 命令行参数

- `--mode`: 必选，通信模式，可选值为 `udp` 或 `tcp`
- `--ip`: 必选，目标服务器IP地址
- `--port`: 必选，目标服务器端口
- `--message`: 可选，要发送的消息（如不提供则进入交互模式）

### 使用示例

#### 单次发送模式

```bash
# 发送单条UDP消息
python network_client.py --mode udp --ip 192.168.31.26 --port 8888 --message "这是一条UDP测试消息"

# 发送单条TCP消息
python network_client.py --mode tcp --ip 192.168.31.26 --port 9999 --message "这是一条TCP测试消息"
```

#### 交互模式

```bash
# UDP交互模式
python network_client.py --mode udp --ip 192.168.31.26 --port 8888

# TCP交互模式
python network_client.py --mode tcp --ip 192.168.31.26 --port 9999
```

在交互模式下，可以输入多条消息连续发送，输入'quit'或'exit'退出。

## 测试场景示例

### 场景1：测试Android应用向电脑发送消息

1. 在电脑上运行服务器:
   ```bash
   python network_test.py
   ```
   
2. 在Android应用中配置目标IP为电脑IP，端口为8888(UDP)或9999(TCP)

3. 从Android应用发送消息，观察电脑上的服务器是否收到

### 场景2：测试电脑向Android应用发送消息

1. 在Android应用中启动服务监听模式

2. 在电脑上运行客户端:
   ```bash
   python network_client.py --mode udp --ip <Android设备IP> --port <Android监听端口>
   ```
   
3. 输入消息发送，观察Android应用是否收到

## 与Android设备通信配置

### UDP通信测试

1. 在电脑上运行UDP服务器：
   ```bash
   python network_test.py --mode udp --udp-port 8888
   ```

2. 在Android应用中配置：
   - 选择"UDP"通信模式
   - 接收方IP填写：电脑的IP地址（脚本启动时会显示）
   - 接收端口填写：`8888`
   - 点击"连接"按钮
   - 发送消息测试

### TCP通信测试

1. 在电脑上运行TCP服务器：
   ```bash
   python network_test.py --mode tcp --tcp-port 9999
   ```

2. 在Android应用中配置：
   - 选择"TCP"（客户端）模式
   - 接收方IP填写：电脑的IP地址（脚本启动时会显示）
   - 接收端口填写：`9999`
   - 点击"连接"按钮
   - 连接成功后发送消息测试

### 模拟器特殊配置

如果使用Android模拟器，需要执行端口转发：

```bash
# UDP端口转发
adb forward tcp:8888 udp:8888

# TCP端口转发
adb forward tcp:9999 tcp:9999
```

## 输出说明

脚本输出格式为：`[时间戳] [消息类型] 消息内容`

消息类型包括：
- `SYSTEM`: 系统消息
- `UDP`: UDP相关消息
- `TCP`: TCP相关消息
- `DATA`: 接收到的数据内容
- `ERROR`: 错误信息
- `WARNING`: 警告信息

## 注意事项

- 确保防火墙未阻止相应端口
- 确保Android应用有网络权限
- 在局域网环境中，确保设备在同一网络下
- 模拟器需要额外的端口转发设置 