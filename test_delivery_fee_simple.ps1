# 简单测试配送费计算功能

# 基础URL
$baseUrl = "http://localhost:8080/api"

Write-Host "开始测试配送费计算功能..."
Write-Host ""

# 测试1：计算配送费
Write-Host "=== 测试1：计算配送费 ==="
try {
    $body = @{
        distance = 3
        weight = 2
        orderAmount = 25
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$baseUrl/delivery-fee/calculate" -Method POST -Body $body -ContentType "application/json"
    Write-Host "测试结果：成功"
    Write-Host "配送费：$($response.data)元"
} catch {
    Write-Host "测试结果：失败"
    Write-Host "错误信息：$($_.Exception.Message)"
}
Write-Host ""

# 测试2：获取免费配送阈值
Write-Host "=== 测试2：获取免费配送阈值 ==="
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/delivery-fee/free-threshold" -Method GET
    Write-Host "测试结果：成功"
    Write-Host "免费配送阈值：$($response.data)元"
} catch {
    Write-Host "测试结果：失败"
    Write-Host "错误信息：$($_.Exception.Message)"
}
Write-Host ""

# 测试3：更新配送费规则
Write-Host "=== 测试3：更新配送费规则 ==="
try {
    $body = @{
        baseFee = "5.00"
        distanceFee = "1.50"
        weightFee = "1.00"
        freeThreshold = "50.00"
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$baseUrl/delivery-fee/update-rule" -Method PUT -Body $body -ContentType "application/json"
    Write-Host "测试结果：成功"
    Write-Host "更新结果：$($response.msg)"
} catch {
    Write-Host "测试结果：失败"
    Write-Host "错误信息：$($_.Exception.Message)"
}
Write-Host ""

# 测试4：再次计算配送费（验证规则更新）
Write-Host "=== 测试4：再次计算配送费 ==="
try {
    $body = @{
        distance = 3
        weight = 2
        orderAmount = 25
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$baseUrl/delivery-fee/calculate" -Method POST -Body $body -ContentType "application/json"
    Write-Host "测试结果：成功"
    Write-Host "配送费：$($response.data)元"
} catch {
    Write-Host "测试结果：失败"
    Write-Host "错误信息：$($_.Exception.Message)"
}
Write-Host ""

Write-Host "配送费计算功能测试完成！"
