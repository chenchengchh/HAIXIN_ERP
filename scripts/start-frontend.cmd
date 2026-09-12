@echo off

REM Start frontend service script
REM Author: Trae AI
REM Date: 2026-01-23

echo ========================================
echo Starting frontend service...
echo ========================================

REM Change to frontend directory
cd /d "%~dp0frontend"

REM Check if dependencies are installed
if not exist "node_modules" (
    echo Installing dependencies...
    npm install
    if errorlevel 1 (
        echo Failed to install dependencies. Please check network or npm configuration.
        pause
        exit /b 1
    )
)

REM Start frontend development server
echo Starting frontend development server...
if "%VITE_PROXY_TARGET%"=="" set VITE_PROXY_TARGET=http://localhost:9000
start "Frontend Dev Server" cmd /k "npm run dev -- --host 0.0.0.0 --port 5173"

REM Wait for server to start
echo Waiting for server to start (3-5 seconds)...
ping 127.0.0.1 -n 4 >nul

REM Open browser to access frontend
echo Opening browser to access frontend...
start "" "http://localhost:5173/"

echo ========================================
echo Frontend service startup script completed!
echo Please visit http://localhost:5173/ to view the frontend page.
echo If the service fails to start normally, please check the error messages in the terminal.
echo ========================================

pause
