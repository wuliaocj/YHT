$token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0X29wZW5pZF8xMjM0NTYiLCJpYXQiOjE3NzIxNTU1NDksImV4cCI6MTc3Mjc2MDM0OX0.1zigiHm-rGxmXxHpd4UUJjxXRGHRzzkCAmjuFCT2urQjS7KLME15vE1WKbFCMGvSNcJXY35c57gErPXtzTtzTw"

$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

# 1. 查看商品列表
Write-Host "=== 1. 查看商品列表 ==="
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/product/list" -Method GET -Headers $headers -UseBasicParsing
    Write-Host "商品列表响应: $($response.Content)"
} catch {
    Write-Host "获取商品列表失败: $($_.Exception.Message)"
}

# 2. 查看商品详情（商品ID为6）
Write-Host "\n=== 2. 查看商品详情 ==="
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/product/detail/6" -Method GET -Headers $headers -UseBasicParsing
    Write-Host "商品详情响应: $($response.Content)"
} catch {
    Write-Host "获取商品详情失败: $($_.Exception.Message)"
}
