#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import socket
import argparse
import threading
import time
import sys
from datetime import datetime

# 全局变量
running = True
tcp_socket = None

def get_current_time():
    """返回当前时间字符串"""
    return datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]

def print_message(message, message_type="INFO"):
    """打印带时间戳和类型的消息"""
    time_str = get_current_time()
    print(f"[{time_str}] [{message_type}] {message}")

def send_udp_message(target_ip, target_port, message):
    """发送UDP消息"""
    try:
        # 创建UDP socket
        udp_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        
        # 发送消息
        udp_socket.sendto(message.encode('utf-8'), (target_ip, target_port))
        print_message(f"UDP消息已发送至 {target_ip}:{target_port}", "UDP")
        
        # 设置接收超时
        udp_socket.settimeout(3)
        
        try:
            # 尝试接收回复
            data, addr = udp_socket.recvfrom(8192)
            reply = data.decode('utf-8', errors='replace')
            print_message(f"收到来自 {addr[0]}:{addr[1]} 的回复:", "UDP")
            print_message(f"回复内容: {reply}", "DATA")
        except socket.timeout:
            print_message("等待UDP回复超时", "WARNING")
        
    except Exception as e:
        print_message(f"发送UDP消息时出错: {str(e)}", "ERROR")
    finally:
        udp_socket.close()

def tcp_receive_thread(sock):
    """TCP接收线程"""
    global running
    
    try:
        while running:
            try:
                # 接收数据
                data = sock.recv(8192)
                if not data:
                    print_message("TCP连接已关闭", "TCP")
                    running = False
                    break
                
                message = data.decode('utf-8', errors='replace')
                print_message("收到TCP回复:", "TCP")
                print_message(f"回复内容: {message}", "DATA")
            except socket.timeout:
                continue
            except Exception as e:
                if running:
                    print_message(f"接收TCP数据时出错: {str(e)}", "ERROR")
                break
    except Exception as e:
        print_message(f"TCP接收线程出错: {str(e)}", "ERROR")

def connect_tcp(target_ip, target_port):
    """连接到TCP服务器"""
    global tcp_socket, running
    
    try:
        # 创建TCP socket
        tcp_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        tcp_socket.settimeout(5)
        
        # 连接服务器
        print_message(f"正在连接TCP服务器 {target_ip}:{target_port}...", "TCP")
        tcp_socket.connect((target_ip, target_port))
        print_message(f"已连接到TCP服务器 {target_ip}:{target_port}", "TCP")
        
        # 设置接收超时
        tcp_socket.settimeout(1)
        
        # 启动接收线程
        receiver = threading.Thread(target=tcp_receive_thread, args=(tcp_socket,))
        receiver.daemon = True
        receiver.start()
        
        return True
    except Exception as e:
        print_message(f"连接TCP服务器时出错: {str(e)}", "ERROR")
        if tcp_socket:
            tcp_socket.close()
            tcp_socket = None
        return False

def send_tcp_message(message):
    """发送TCP消息"""
    global tcp_socket
    
    if not tcp_socket:
        print_message("未连接到TCP服务器", "ERROR")
        return False
    
    try:
        # 发送消息
        tcp_socket.sendall(message.encode('utf-8'))
        print_message("TCP消息已发送", "TCP")
        return True
    except Exception as e:
        print_message(f"发送TCP消息时出错: {str(e)}", "ERROR")
        return False

def interactive_mode(mode, target_ip, target_port):
    """交互模式"""
    global running, tcp_socket
    
    print_message("====== 网络通信测试客户端 - 交互模式 ======", "SYSTEM")
    print_message(f"目标: {target_ip}:{target_port}", "SYSTEM")
    print_message(f"模式: {mode.upper()}", "SYSTEM")
    print_message("输入消息内容并按回车发送，输入'quit'或'exit'退出", "SYSTEM")
    print_message("============================================", "SYSTEM")
    
    if mode == 'tcp':
        # 连接TCP服务器
        if not connect_tcp(target_ip, target_port):
            return
    
    try:
        while running:
            # 获取用户输入
            user_input = input("> ")
            
            # 检查是否退出
            if user_input.lower() in ['quit', 'exit']:
                running = False
                break
            
            # 发送消息
            if mode == 'udp':
                send_udp_message(target_ip, target_port, user_input)
            elif mode == 'tcp':
                if not send_tcp_message(user_input):
                    break
    except KeyboardInterrupt:
        print_message("\n接收到退出信号", "SYSTEM")
    finally:
        running = False
        if tcp_socket:
            tcp_socket.close()

def main():
    """主函数"""
    # 解析命令行参数
    parser = argparse.ArgumentParser(description='网络通信测试客户端')
    parser.add_argument('--mode', choices=['udp', 'tcp'], required=True,
                      help='通信模式: udp 或 tcp')
    parser.add_argument('--ip', required=True,
                      help='目标IP地址')
    parser.add_argument('--port', type=int, required=True,
                      help='目标端口')
    parser.add_argument('--message', 
                      help='要发送的消息 (如不提供则进入交互模式)')
    
    args = parser.parse_args()
    
    # 根据模式执行操作
    if args.message:
        # 单次发送模式
        if args.mode == 'udp':
            send_udp_message(args.ip, args.port, args.message)
        else:  # TCP mode
            if connect_tcp(args.ip, args.port):
                send_tcp_message(args.message)
                # 等待接收回复
                time.sleep(2)
                if tcp_socket:
                    tcp_socket.close()
    else:
        # 交互模式
        interactive_mode(args.mode, args.ip, args.port)

if __name__ == "__main__":
    main() 