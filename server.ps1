# Aura Finance - Zero-Dependency Local Web Server
param([int]$Port = 8080)

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
if (-not $Root) { $Root = (Get-Location).Path }

# Determine local WiFi IPv4
$localIP = "192.168.68.51"
try {
    $ipLines = ipconfig | Select-String "IPv4"
    foreach ($line in $ipLines) {
        if ($line -match "(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})") {
            $candidate = $matches[1]
            if ($candidate -ne "127.0.0.1" -and -not $candidate.StartsWith("169.254")) {
                $localIP = $candidate
                break
            }
        }
    }
} catch {}

$pcUrl = "http://localhost:" + $Port
$phoneUrl = "http://" + $localIP + ":" + $Port

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   AURA FINANCE - LOCAL WEB SERVER STARTED                " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host ("  [PC Browser]    " + $pcUrl) -ForegroundColor Yellow
Write-Host ("  [Phone Browser] " + $phoneUrl) -ForegroundColor Yellow
Write-Host ""
Write-Host "  (Ensure your phone is connected to the same Wi-Fi)" -ForegroundColor Gray
Write-Host "  Press Ctrl+C in this window to stop the server." -ForegroundColor Gray
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host ""

# Automatically launch browser on PC
try {
    Start-Process $pcUrl
} catch {}

# Start TCP Listener on all interfaces
$endpoint = [System.Net.IPEndPoint]::new([System.Net.IPAddress]::Any, $Port)
$server = [System.Net.Sockets.TcpListener]::new($endpoint)

try {
    $server.Start()
} catch {
    Write-Host ("Port " + $Port + " is busy. Trying port 8081...") -ForegroundColor Yellow
    $Port = 8081
    $pcUrl = "http://localhost:" + $Port
    $phoneUrl = "http://" + $localIP + ":" + $Port
    $endpoint = [System.Net.IPEndPoint]::new([System.Net.IPAddress]::Any, $Port)
    $server = [System.Net.Sockets.TcpListener]::new($endpoint)
    $server.Start()
    Write-Host ("Running on " + $pcUrl + " and " + $phoneUrl) -ForegroundColor Green
}

function Get-ContentType($ext) {
    switch ($ext.ToLower()) {
        ".html" { return "text/html; charset=utf-8" }
        ".js"   { return "application/javascript; charset=utf-8" }
        ".json" { return "application/json; charset=utf-8" }
        ".css"  { return "text/css; charset=utf-8" }
        ".png"  { return "image/png" }
        ".svg"  { return "image/svg+xml" }
        ".ico"  { return "image/x-icon" }
        Default { return "application/octet-stream" }
    }
}

$crlf = [char]13 + [char]10

while ($true) {
    try {
        $client = $server.AcceptTcpClient()
        $stream = $client.GetStream()
        $reader = [System.IO.StreamReader]::new($stream)
        $requestLine = $reader.ReadLine()
        
        if ($requestLine) {
            $parts = $requestLine -split " "
            $reqPath = $parts[1]
            if ($reqPath -eq "/" -or $reqPath -eq "") { $reqPath = "/index.html" }
            $reqPath = $reqPath.Split("?")[0]
            
            $cleanRelPath = $reqPath.TrimStart("/").Replace("/", [System.IO.Path]::DirectorySeparatorChar)
            $filePath = [System.IO.Path]::Combine($Root, $cleanRelPath)
            
            if ([System.IO.File]::Exists($filePath)) {
                $ext = [System.IO.Path]::GetExtension($filePath)
                $contentType = Get-ContentType $ext
                $fileBytes = [System.IO.File]::ReadAllBytes($filePath)
                
                $header = "HTTP/1.1 200 OK" + $crlf +
                          "Content-Type: " + $contentType + $crlf +
                          "Content-Length: " + $fileBytes.Length + $crlf +
                          "Access-Control-Allow-Origin: *" + $crlf +
                          "Connection: close" + $crlf + $crlf
                
                $headerBytes = [System.Text.Encoding]::UTF8.GetBytes($header)
                $stream.Write($headerBytes, 0, $headerBytes.Length)
                $stream.Write($fileBytes, 0, $fileBytes.Length)
            } else {
                $notFound = "HTTP/1.1 404 Not Found" + $crlf +
                            "Content-Length: 9" + $crlf +
                            "Connection: close" + $crlf + $crlf +
                            "Not Found"
                
                $notFoundBytes = [System.Text.Encoding]::UTF8.GetBytes($notFound)
                $stream.Write($notFoundBytes, 0, $notFoundBytes.Length)
            }
        }
        
        $stream.Flush()
        $client.Close()
    } catch {
        # ignore transient client disconnects
    }
}
