<#
启动前端服务脚本
作者: Trae AI
日期: 2026-01-23
#>

param(
    [string]$Port = '5173',
    [string]$ProxyTarget = 'http://localhost:9000'
)

Write-Host "========================================" -ForegroundColor Green
Write-Host "启动前端服务..." -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# 设置脚本所在目录
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# 切换到前端目录
Set-Location -Path "$ScriptDir\frontend"

$env:VITE_PROXY_TARGET = $ProxyTarget

# 检查是否已安装依赖
if (-not (Test-Path -Path "node_modules" -PathType Container)) {
    Write-Host "正在安装依赖..." -ForegroundColor Yellow
    npm install
    if ($LASTEXITCODE -ne 0) {
        Write-Host "依赖安装失败，请检查网络或npm配置" -ForegroundColor Red
        Read-Host "按 Enter 键退出..."
        exit 1
    }
}

# 启动前端开发服务器
Write-Host "正在启动前端开发服务器..." -ForegroundColor Yellow
Start-Process -FilePath "cmd.exe" -ArgumentList "/k npm run dev -- --host 0.0.0.0 --port $Port" -WindowStyle Normal -WorkingDirectory "$ScriptDir\frontend"

# 等待服务器启动
Write-Host "等待服务器启动，预计需要3-5秒..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

# 打开浏览器访问前端页面
Write-Host "正在打开浏览器访问前端页面..." -ForegroundColor Yellow
Start-Process -FilePath "http://localhost:$Port/"

Write-Host "========================================" -ForegroundColor Green
Write-Host "前端服务启动脚本执行完成！" -ForegroundColor Green
Write-Host "请访问 http://localhost:$Port/ 查看前端页面" -ForegroundColor Green
Write-Host "如果服务未正常启动，请检查终端中的错误信息" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

Read-Host "按 Enter 键退出..."
