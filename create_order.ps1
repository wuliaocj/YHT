$token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0X29wZW5pZF8xMjM0NTYiLCJpYXQiOjE3NzIxNTU1NDksImV4cCI6MTc3Mjc2MDM0OX0.1zigiHm-rGxmXxHpd4UUJjxXRGHRzzkCAmjuFCT2urQjS7KLME15vE1WKbFCMGvSNcJXY35c57gErPXtzTtzTw"

$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

# 5. 创建订单
Write-Host "=== 5. 创建订单 ==="
$orderBody = '{"orderType": 1, "paymentMethod": 1, "userRemark": "少冰"}'

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/order/create?userId=2" -Method POST -Headers $headers -Body $orderBody -UseBasicParsing
    Write-Host "创建订单响应: $($response.Content)"
} catch {
    Write-Host "创建订单失败: $($_.Exception.Message)"
}

# 6. 支付订单
Write-Host "\n=== 6. 支付订单 ==="
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/payment/create?orderNo=ORD417856680248741888" -Method POST -Headers $headers -UseBasicParsing
    Write-Host "支付订单响应: $($response.Content)"
} catch {
    Write-Host "支付订单失败: $($_.Exception.Message)"
}

# 7. 查看订单列表
Write-Host "\n=== 7. 查看订单列表 ==="
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/order/list/2" -Method GET -Headers $headers -UseBasicParsing
    Write-Host "订单列表响应: $($response.Content)"
} catch {
    Write-Host "获取订单列表失败: $($_.Exception.Message)"
}
