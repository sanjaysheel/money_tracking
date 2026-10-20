@echo off
title Aura Finance Server
cd /d "%~dp0"
echo Starting Aura Finance Server...
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0server.ps1"
pause
