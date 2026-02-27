# 测试外卖功能完整流程

# 1. 测试登录获取JWT令牌
Write-Host "=== 1. 测试登录获取JWT令牌 ==="
try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/user/test/login" -Method POST -Headers @{"Content-Type"="application/json"} -Body '{"username":"test","password":"123456"}'
    $token = $loginResponse.data.token
    Write-Host "登录成功，获取到令牌: $token"
} catch {
    Write-Host "登录失败: $($_.Exception.Message)"
    exit 1
}

# 2. 查看用户地址列表
Write-Host "\n=== 2. 测试查看用户地址列表 ==="
try {
    $addressListResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/address/list/2" -Method GET -Headers @{"Authorization"="Bearer $token"}
    Write-Host "获取地址列表成功: $($addressListResponse | ConvertTo-Json -Depth 3)"
    $addressId = $addressListResponse.data[0].id
    Write-Host "使用地址ID: $addressId"
} catch {
    Write-Host "获取地址列表失败: $($_.Exception.Message)"
    # 如果没有地址，创建一个
    try {
        Write-Host "\n=== 创建新地址 ==="
        $newAddress = @{
            userId = 2
            consignee = "测试用户"
            phone = "13800138000"
            province = "广东省"
            city = "深圳市"
            district = "南山区"
            detailAddress = "科技园南区"
            isDefault = 1
            status = 1
        }
        $createAddressResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/address/save" -Method POST -Headers @{"Content-Type"="application/json"; "Authorization"="Bearer $token"} -Body ($newAddress | ConvertTo-Json)
        Write-Host "创建地址成功: $($createAddressResponse | ConvertTo-Json -Depth 3)"
        $addressId = $createAddressResponse.data.id
        Write-Host "新创建的地址ID: $addressId"
    } catch {
        Write-Host "创建地址失败: $($_.Exception.Message)"
        exit 1
    }
}

# 3. 创建外卖订单
Write-Host "\n=== 3. 测试创建外卖订单 ==="
try {
    $order = @{
        orderType = 2  # 外卖
        paymentMethod = 1  # 微信支付
        addressId = $addressId
        userRemark = "少冰，不要糖"
        couponId = 1  # 使用优惠券
        discountAmount = 5.00  # 优惠金额
    }
    $createOrderResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/order/create?userId=2" -Method POST -Headers @{"Content-Type"="application/json"; "Authorization"="Bearer $token"} -Body ($order | ConvertTo-Json)
    Write-Host "创建外卖订单成功: $($createOrderResponse | ConvertTo-Json -Depth 3)"
    $orderId = $createOrderResponse.data.id
    Write-Host "订单ID: $orderId"
} catch {
    Write-Host "创建外卖订单失败: $($_.Exception.Message)"
    exit 1
}

# 4. 查询订单详情
Write-Host "\n=== 4. 测试查询订单详情 ==="
try {
    $orderDetailResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/order/detail/$orderId" -Method GET -Headers @{"Authorization"="Bearer $token"}
    Write-Host "查询订单详情成功: $($orderDetailResponse | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "查询订单详情失败: $($_.Exception.Message)"
}

# 5. 管理员更新订单状态（模拟支付成功）
Write-Host "\n=== 5. 测试管理员更新订单状态 ==="
try {
    $updateRequest = @{
        orderId = $orderId
        status = 2  # 支付成功
        adminRemark = "订单已支付"
    }
    $updateResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/order/admin/order/update" -Method POST -Headers @{"Content-Type"="application/json"; "Authorization"="Bearer $token"} -Body ($updateRequest | ConvertTo-Json)
    Write-Host "更新订单状态成功: $($updateResponse | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "更新订单状态失败: $($_.Exception.Message)"
}

# 6. 再次查询订单详情（检查取餐码）
Write-Host "\n=== 6. 测试再次查询订单详情（检查取餐码） ==="
try {
    $orderDetailResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/order/detail/$orderId" -Method GET -Headers @{"Authorization"="Bearer $token"}
    Write-Host "查询订单详情成功: $($orderDetailResponse | ConvertTo-Json -Depth 3)"
    $takeCode = $orderDetailResponse.data.takeCode
    Write-Host "取餐码: $takeCode"
} catch {
    Write-Host "查询订单详情失败: $($_.Exception.Message)"
}

# 7. 验证取餐码
Write-Host "\n=== 7. 测试验证取餐码 ==="
try {
    $validateResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/order/validate-take-code?takeCode=$takeCode" -Method POST -Headers @{"Authorization"="Bearer $token"}
    Write-Host "验证取餐码成功: $($validateResponse | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "验证取餐码失败: $($_.Exception.Message)"
}

# 8. 管理员更新订单状态为已完成
Write-Host "\n=== 8. 测试管理员更新订单状态为已完成 ==="
try {
    $updateRequest = @{
        orderId = $orderId
        status = 3  # 已完成
        adminRemark = "订单已完成"
    }
    $updateResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/order/admin/order/update" -Method POST -Headers @{"Content-Type"="application/json"; "Authorization"="Bearer $token"} -Body ($updateRequest | ConvertTo-Json)
    Write-Host "更新订单状态为已完成成功: $($updateResponse | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "更新订单状态为已完成失败: $($_.Exception.Message)"
}

# 9. 验证取餐码（已完成状态）
Write-Host "\n=== 9. 测试验证取餐码（已完成状态） ==="
try {
    $validateResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/order/validate-take-code?takeCode=$takeCode" -Method POST -Headers @{"Authorization"="Bearer $token"}
    Write-Host "验证取餐码成功: $($validateResponse | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "验证取餐码失败: $($_.Exception.Message)"
}
