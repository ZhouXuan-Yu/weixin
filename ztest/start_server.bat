@echo off
title UDP/TCP网络测试服务器
echo ====== UDP/TCP网络测试服务器启动脚本 ======
echo.

REM 检查Python是否安装
where python >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 未检测到Python安装，请安装Python 3.6或更高版本
    pause
    exit /b 1
)

REM 默认参数
set MODE=both
set UDP_PORT=8888
set TCP_PORT=9999
set HOST=0.0.0.0

REM 显示本机IP地址
echo 本机IPv4地址:
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /r /c:"IPv4"') do (
    echo     %%a
)
echo.

REM 提示用户选择模式
echo 请选择服务器模式:
echo [1] UDP和TCP (默认)
echo [2] 仅UDP
echo [3] 仅TCP
choice /c 123 /n /m "选择(1-3): "
if %ERRORLEVEL% EQU 1 set MODE=both
if %ERRORLEVEL% EQU 2 set MODE=udp
if %ERRORLEVEL% EQU 3 set MODE=tcp
echo.

REM 提示用户自定义端口
if "%MODE%" == "both" (
    set /p UDP_PORT="UDP端口 [默认: 8888]: "
    set /p TCP_PORT="TCP端口 [默认: 9999]: "
) else if "%MODE%" == "udp" (
    set /p UDP_PORT="UDP端口 [默认: 8888]: "
) else if "%MODE%" == "tcp" (
    set /p TCP_PORT="TCP端口 [默认: 9999]: "
)
echo.

REM 启动Python服务器
echo 正在启动服务器...
echo 命令: python network_test.py --mode %MODE% --host %HOST% --udp-port %UDP_PORT% --tcp-port %TCP_PORT%
echo.
echo 按 Ctrl+C 停止服务器
echo ======================================
echo.

python network_test.py --mode %MODE% --host %HOST% --udp-port %UDP_PORT% --tcp-port %TCP_PORT%

pause 
 