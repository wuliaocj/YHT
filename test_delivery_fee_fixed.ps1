# 测试固定配送费功能

$body = '{"distance": 5, "weight": 3, "orderAmount": 50}'
try {
    $response = Invoke-WebRequest -Uri 'http://localhost:8080/api/delivery-fee/calculate' -Method POST -Body $body -ContentType 'application/json'
    Write-Host "测试结果：成功"
    Write-Host "响应内容：" $response.Content
} catch {
    Write-Host "测试结果：失败"
    Write-Host "错误信息：" $_.Exception.Message
}
