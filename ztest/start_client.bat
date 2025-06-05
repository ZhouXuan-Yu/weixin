@echo off
title UDP/TCP网络测试客户端
echo ====== UDP/TCP网络测试客户端启动脚本 ======
echo.

REM 检查Python是否安装
where python >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 未检测到Python安装，请安装Python 3.6或更高版本
    pause
    exit /b 1
)

REM 默认参数
set MODE=udp
set DEFAULT_PORT=8888
set TARGET_IP=
set TARGET_PORT=
set MESSAGE=

REM 提示用户选择模式
echo 请选择通信模式:
echo [1] UDP (默认)
echo [2] TCP
choice /c 12 /n /m "选择(1-2): "
if %ERRORLEVEL% EQU 1 (
    set MODE=udp
    set DEFAULT_PORT=8888
)
if %ERRORLEVEL% EQU 2 (
    set MODE=tcp
    set DEFAULT_PORT=9999
)
echo.

REM 输入目标IP
set /p TARGET_IP="目标IP地址: "
if "%TARGET_IP%"=="" (
    echo [错误] 必须输入目标IP地址
    pause
    exit /b 1
)

REM 输入目标端口
set /p TARGET_PORT="目标端口 [默认: %DEFAULT_PORT%]: "
if "%TARGET_PORT%"=="" set TARGET_PORT=%DEFAULT_PORT%
echo.

REM 选择发送模式
echo 请选择发送模式:
echo [1] 交互模式 (默认)
echo [2] 单条消息模式
choice /c 12 /n /m "选择(1-2): "
if %ERRORLEVEL% EQU 2 (
    set /p MESSAGE="要发送的消息: "
    if "%MESSAGE%"=="" (
        echo [错误] 必须输入消息内容
        pause
        exit /b 1
    )
)
echo.

REM 启动Python客户端
echo 正在启动客户端...
if "%MESSAGE%"=="" (
    echo 命令: python network_client.py --mode %MODE% --ip %TARGET_IP% --port %TARGET_PORT%
    echo.
    echo 交互模式启动，输入消息内容并按回车发送，输入'quit'或'exit'退出
) else (
    echo 命令: python network_client.py --mode %MODE% --ip %TARGET_IP% --port %TARGET_PORT% --message "%MESSAGE%"
)
echo.
echo ======================================
echo.

if "%MESSAGE%"=="" (
    python network_client.py --mode %MODE% --ip %TARGET_IP% --port %TARGET_PORT%
) else (
    python network_client.py --mode %MODE% --ip %TARGET_IP% --port %TARGET_PORT% --message "%MESSAGE%"
)

pause 