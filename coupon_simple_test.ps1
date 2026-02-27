$token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0X29wZW5pZF8xMjM0NTYiLCJpYXQiOjE3NzIxNTU1NDksImV4cCI6MTc3Mjc2MDM0OX0.1zigiHm-rGxmXxHpd4UUJjxXRGHRzzkCAmjuFCT2urQjS7KLME15vE1WKbFCMGvSNcJXY35c57gErPXtzTtzTw"

$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

# 测试优惠券接口
Write-Host "=== 测试优惠券接口 ==="

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/coupon/available/2" -Method GET -Headers $headers -UseBasicParsing
    Write-Host "可用优惠券响应: $($response.Content)"
} catch {
    Write-Host "获取可用优惠券失败: $($_.Exception.Message)"
}

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/admin/coupon/list" -Method GET -Headers $headers -UseBasicParsing
    Write-Host "管理员优惠券列表响应: $($response.Content)"
} catch {
    Write-Host "获取管理员优惠券列表失败: $($_.Exception.Message)"
}
