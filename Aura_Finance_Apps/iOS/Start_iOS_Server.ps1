# Aura Finance - iOS Local Server & Quick Connect Launcher
$port = 8080
$bundlePath = Join-Path $PSScriptRoot "iOS_PWA_Bundle"

# Determine local IP address on Wi-Fi/Ethernet
$ipAddresses = Get-NetIPAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue | Where-Object { 
    $_.InterfaceAlias -notmatch 'Loopback' -and $_.IPAddress -notmatch '^169\.' -and $_.IPAddress -notmatch '^127\.' 
}
$localIp = if ($ipAddresses) { $ipAddresses[0].IPAddress } else { "localhost" }

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "       AURA FINANCE - iOS LOCAL CONNECT SERVER              " -ForegroundColor Yellow
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Connect your iPhone to the SAME Wi-Fi network as this PC." -ForegroundColor Green
Write-Host "2. Open Safari on your iPhone and visit:" -ForegroundColor Green
Write-Host ""
Write-Host "   👉  http://${localIp}:${port}" -ForegroundColor Yellow -BackgroundColor Black
Write-Host ""
Write-Host "3. Tap the Share button in Safari -> 'Add to Home Screen'." -ForegroundColor Green
Write-Host "   Aura Finance will install as a standalone native app!" -ForegroundColor Green
Write-Host ""
Write-Host "Serving from: $bundlePath" -ForegroundColor Gray
Write-Host "Press Ctrl+C to stop the server anytime." -ForegroundColor Gray
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

# Start lightweight HTTP Listener
$listener = New-Object System.Net.HttpListener
$listener.Prefixes.Add("http://*:${port}/")
try {
    $listener.Start()
} catch {
    $listener = New-Object System.Net.HttpListener
    $listener.Prefixes.Add("http://localhost:${port}/")
    $listener.Start()
}

while ($listener.IsListening) {
    $context = $listener.GetContext()
    $request = $context.Request
    $response = $context.Response
    
    $localPath = $request.Url.LocalPath.TrimStart('/')
    if ([string]::IsNullOrWhiteSpace($localPath)) {
        $localPath = "index.html"
    }
    
    $filePath = Join-Path $bundlePath $localPath
    if (Test-Path $filePath -PathType Leaf) {
        $ext = [System.IO.Path]::GetExtension($filePath).ToLower()
        $contentType = switch ($ext) {
            ".html" { "text/html; charset=utf-8" }
            ".css"  { "text/css; charset=utf-8" }
            ".js"   { "application/javascript; charset=utf-8" }
            ".json" { "application/json; charset=utf-8" }
            ".png"  { "image/png" }
            ".jpg"  { "image/jpeg" }
            ".svg"  { "image/svg+xml" }
            default { "application/octet-stream" }
        }
        $response.ContentType = $contentType
        $bytes = [System.IO.File]::ReadAllBytes($filePath)
        $response.ContentLength64 = $bytes.Length
        $response.OutputStream.Write($bytes, 0, $bytes.Length)
    } else {
        $response.StatusCode = 404
        $notFoundMsg = [System.Text.Encoding]::UTF8.GetBytes("File Not Found")
        $response.OutputStream.Write($notFoundMsg, 0, $notFoundMsg.Length)
    }
    $response.OutputStream.Close()
}
