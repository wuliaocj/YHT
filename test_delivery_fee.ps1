# 测试配送费计算功能

# 基础URL
$baseUrl = "http://localhost:8080/api"

# 测试计算配送费
function Test-CalculateDeliveryFee {
    Write-Host "=== 测试配送费计算 ==="
    
    $testCases = @(
        @{ distance = 1; weight = 1; orderAmount = 20; expected = "3.00" },
        @{ distance = 3; weight = 1; orderAmount = 20; expected = "5.00" },
        @{ distance = 1; weight = 3; orderAmount = 20; expected = "4.00" },
        @{ distance = 5; weight = 5; orderAmount = 20; expected = "9.00" },
        @{ distance = 1; weight = 1; orderAmount = 30; expected = "0.00" },
        @{ distance = 10; weight = 10; orderAmount = 40; expected = "0.00" }
    )
    
    foreach ($testCase in $testCases) {
        $body = @{
            distance = $testCase.distance
            weight = $testCase.weight
            orderAmount = $testCase.orderAmount
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$baseUrl/delivery-fee/calculate" -Method POST -Body $body -ContentType "application/json"
        
        Write-Host "测试用例：距离=$($testCase.distance)km, 重量=$($testCase.weight)kg, 订单金额=$($testCase.orderAmount)元"
        Write-Host "期望结果：$($testCase.expected)元"
        Write-Host "实际结果：$($response.data)元"
        Write-Host "测试结果：$(if ($response.data -eq $testCase.expected) { '通过' } else { '失败' })"
        Write-Host ""
    }
}

# 测试获取免费配送阈值
function Test-GetFreeDeliveryThreshold {
    Write-Host "=== 测试获取免费配送阈值 ==="
    
    $response = Invoke-RestMethod -Uri "$baseUrl/delivery-fee/free-threshold" -Method GET
    
    Write-Host "免费配送阈值：$($response.data)元"
    Write-Host "测试结果：成功"
    Write-Host ""
}

# 测试更新配送费规则
function Test-UpdateDeliveryFeeRule {
    Write-Host "=== 测试更新配送费规则 ==="
    
    $body = @{
        baseFee = "5.00"
        distanceFee = "1.50"
        weightFee = "1.00"
        freeThreshold = "50.00"
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$baseUrl/delivery-fee/update-rule" -Method PUT -Body $body -ContentType "application/json"
    
    Write-Host "更新配送费规则："
    Write-Host "- 基础配送费：5.00元"
    Write-Host "- 每公里额外费用：1.50元"
    Write-Host "- 每公斤额外费用：1.00元"
    Write-Host "- 免费配送阈值：50.00元"
    Write-Host "测试结果：$($response.msg)"
    Write-Host ""
    
    # 验证更新后的规则
    Test-CalculateDeliveryFeeAfterUpdate
}

# 测试更新后的配送费计算
function Test-CalculateDeliveryFeeAfterUpdate {
    Write-Host "=== 测试更新后的配送费计算 ==="
    
    $testCases = @(
        @{ distance = 1; weight = 1; orderAmount = 20; expected = "5.00" },
        @{ distance = 3; weight = 1; orderAmount = 20; expected = "8.00" },
        @{ distance = 1; weight = 3; orderAmount = 20; expected = "7.00" },
        @{ distance = 5; weight = 5; orderAmount = 20; expected = "16.00" },
        @{ distance = 1; weight = 1; orderAmount = 50; expected = "0.00" }
    )
    
    foreach ($testCase in $testCases) {
        $body = @{
            distance = $testCase.distance
            weight = $testCase.weight
            orderAmount = $testCase.orderAmount
        } | ConvertTo-Json
        
        try {
            $response = Invoke-RestMethod -Uri "$baseUrl/delivery-fee/calculate" -Method POST -Body $body -ContentType "application/json"
            
            Write-Host "测试用例：距离=$($testCase.distance)km, 重量=$($testCase.weight)kg, 订单金额=$($testCase.orderAmount)元"
            Write-Host "期望结果：$($testCase.expected)元"
            Write-Host "实际结果：$($response.data)元"
            Write-Host "测试结果：$(if ($response.data -eq $testCase.expected) { '通过' } else { '失败' })"
        } catch {
            Write-Host "测试用例：距离=$($testCase.distance)km, 重量=$($testCase.weight)kg, 订单金额=$($testCase.orderAmount)元"
            Write-Host "测试结果：失败 - $($_.Exception.Message)"
        }
        Write-Host ""
    }
}

# 运行所有测试
Write-Host "开始测试配送费计算功能..."
Write-Host ""

Test-CalculateDeliveryFee
Test-GetFreeDeliveryThreshold
Test-UpdateDeliveryFeeRule

Write-Host "配送费计算功能测试完成！"
