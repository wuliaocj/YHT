$headers = @{
    "Content-Type" = "application/json"
}

$body = '{"username":"test","password":"123456"}'

$response = Invoke-WebRequest -Uri "http://localhost:8080/api/user/test/login" -Method POST -Headers $headers -Body $body

Write-Host "Response Status Code: $($response.StatusCode)"
Write-Host "Response Content: $($response.Content)"
