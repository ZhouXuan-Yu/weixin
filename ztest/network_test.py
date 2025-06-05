#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import socket
import threading
import argparse
import time
import sys
import signal
from datetime import datetime

# 全局变量
running = True
connections = []

def get_current_time():
    """返回当前时间字符串"""
    return datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]

def print_message(message, message_type="INFO"):
    """打印带时间戳和类型的消息"""
    time_str = get_current_time()
    print(f"[{time_str}] [{message_type}] {message}")

def signal_handler(sig, frame):
    """处理Ctrl+C信号"""
    global running
    print_message("正在关闭服务器...", "SYSTEM")
    running = False
    # 关闭所有连接
    for conn in connections:
        try:
            conn.close()
        except:
            pass
    sys.exit(0)

def start_udp_server(host, port):
    """启动UDP服务器"""
    try:
        # 创建UDP socket
        udp_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        udp_socket.bind((host, port))
        connections.append(udp_socket)
        
        print_message(f"UDP服务器已启动 - 监听 {host}:{port}", "SYSTEM")
        print_message("等待接收数据...", "SYSTEM")
        
        # 设置超时，以便能够定期检查运行状态
        udp_socket.settimeout(1)
        
        while running:
            try:
                # 接收数据
                data, addr = udp_socket.recvfrom(8192)
                message = data.decode('utf-8', errors='replace')
                
                print_message(f"收到来自 {addr[0]}:{addr[1]} 的消息:", "UDP")
                print_message(f"消息内容: {message}", "DATA")
                
                # 发送确认回复（可选）
                reply = f"已收到消息: {message}"
                udp_socket.sendto(reply.encode('utf-8'), addr)
                print_message(f"已回复确认消息", "UDP")
                
            except socket.timeout:
                # 超时继续循环
                continue
            except Exception as e:
                print_message(f"接收UDP数据时出错: {str(e)}", "ERROR")
        
    except Exception as e:
        print_message(f"启动UDP服务器时出错: {str(e)}", "ERROR")
    finally:
        if udp_socket:
            udp_socket.close()
            connections.remove(udp_socket)
            print_message("UDP服务器已关闭", "SYSTEM")

def handle_tcp_client(client_socket, addr):
    """处理TCP客户端连接"""
    print_message(f"接受来自 {addr[0]}:{addr[1]} 的TCP连接", "TCP")
    
    try:
        connections.append(client_socket)
        client_socket.settimeout(1)
        
        while running:
            try:
                # 接收数据
                data = client_socket.recv(8192)
                if not data:
                    break
                
                message = data.decode('utf-8', errors='replace')
                print_message(f"收到来自 {addr[0]}:{addr[1]} 的消息:", "TCP")
                print_message(f"消息内容: {message}", "DATA")
                
                # 发送确认回复
                reply = f"已收到消息: {message}"
                client_socket.send(reply.encode('utf-8'))
                print_message(f"已回复确认消息", "TCP")
                
            except socket.timeout:
                # 超时继续循环
                continue
            except Exception as e:
                if running:  # 仅当服务器仍在运行时打印错误
                    print_message(f"处理TCP客户端数据时出错: {str(e)}", "ERROR")
                break
    
    finally:
        client_socket.close()
        if client_socket in connections:
            connections.remove(client_socket)
        print_message(f"与 {addr[0]}:{addr[1]} 的TCP连接已关闭", "TCP")

def start_tcp_server(host, port):
    """启动TCP服务器"""
    try:
        # 创建TCP socket
        tcp_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        tcp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        tcp_socket.bind((host, port))
        tcp_socket.listen(5)
        connections.append(tcp_socket)
        
        print_message(f"TCP服务器已启动 - 监听 {host}:{port}", "SYSTEM")
        print_message("等待客户端连接...", "SYSTEM")
        
        # 设置超时，以便能够定期检查运行状态
        tcp_socket.settimeout(1)
        
        while running:
            try:
                # 接受客户端连接
                client_socket, addr = tcp_socket.accept()
                
                # 创建新线程处理客户端连接
                client_thread = threading.Thread(target=handle_tcp_client, args=(client_socket, addr))
                client_thread.daemon = True
                client_thread.start()
                
            except socket.timeout:
                # 超时继续循环
                continue
            except Exception as e:
                if running:  # 仅当服务器仍在运行时打印错误
                    print_message(f"接受TCP连接时出错: {str(e)}", "ERROR")
        
    except Exception as e:
        print_message(f"启动TCP服务器时出错: {str(e)}", "ERROR")
    finally:
        if tcp_socket:
            tcp_socket.close()
            if tcp_socket in connections:
                connections.remove(tcp_socket)
            print_message("TCP服务器已关闭", "SYSTEM")

def main():
    """主函数"""
    # 解析命令行参数
    parser = argparse.ArgumentParser(description='网络通信测试工具')
    parser.add_argument('--mode', choices=['udp', 'tcp', 'both'], default='both',
                      help='通信模式: udp, tcp 或 both (默认: both)')
    parser.add_argument('--host', default='0.0.0.0',
                      help='监听地址 (默认: 0.0.0.0)')
    parser.add_argument('--udp-port', type=int, default=8888,
                      help='UDP端口 (默认: 8888)')
    parser.add_argument('--tcp-port', type=int, default=9999,
                      help='TCP端口 (默认: 9999)')
    
    args = parser.parse_args()
    
    # 注册信号处理
    signal.signal(signal.SIGINT, signal_handler)
    
    print_message("====== Android网络通信测试工具 ======", "SYSTEM")
    print_message(f"本机IP地址信息:", "SYSTEM")
    
    # 获取本机IP地址
    try:
        hostname = socket.gethostname()
        ip_list = socket.gethostbyname_ex(hostname)[2]
        for ip in ip_list:
            print_message(f"    {ip}", "SYSTEM")
    except Exception as e:
        print_message(f"获取本机IP地址时出错: {str(e)}", "ERROR")
    
    print_message("============================", "SYSTEM")
    
    # 启动服务器
    threads = []
    
    if args.mode in ['udp', 'both']:
        udp_thread = threading.Thread(target=start_udp_server, args=(args.host, args.udp_port))
        udp_thread.daemon = True
        udp_thread.start()
        threads.append(udp_thread)
    
    if args.mode in ['tcp', 'both']:
        tcp_thread = threading.Thread(target=start_tcp_server, args=(args.host, args.tcp_port))
        tcp_thread.daemon = True
        tcp_thread.start()
        threads.append(tcp_thread)
    
    print_message("服务器已启动，按 Ctrl+C 退出", "SYSTEM")
    print_message("============================", "SYSTEM")
    
    try:
        # 保持主线程运行
        while running:
            time.sleep(1)
    except KeyboardInterrupt:
        signal_handler(signal.SIGINT, None)

if __name__ == "__main__":
    main() 