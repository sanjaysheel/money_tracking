@echo off
title Aura Finance - iOS Local Server
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0Start_iOS_Server.ps1"
pause
