# Test delivery functionality complete flow

# 1. Test login to get JWT token
Write-Host "=== 1. Test login to get JWT token ==="
try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/user/test/login" -Method POST -Headers @{"Content-Type"="application/json"} -Body '{"username":"test","password":"123456"}'
    $token = $loginResponse.data.token
    Write-Host "Login successful, got token: $token"
} catch {
    Write-Host "Login failed: $($_.Exception.Message)"
    exit 1
}

# 2. Check user address list and create address if needed
Write-Host "\n=== 2. Test get user address list ==="
try {
    $addressListResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/address/list/2" -Method GET -Headers @{"Authorization"="Bearer $token"}
    Write-Host "Get address list successful: $($addressListResponse | ConvertTo-Json -Depth 3)"
    
    # Check if address list is empty
    if ($addressListResponse.data.Count -eq 0) {
        Write-Host "\n=== Create new address ==="
        $newAddress = @{
            userId = 2
            consignee = "Test User"
            phone = "13800138000"
            province = "Guangdong"
            city = "Shenzhen"
            district = "Nanshan"
            detailAddress = "Tech Park South"
            isDefault = 1
            status = 1
        }
        $createAddressResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/address/save" -Method POST -Headers @{"Content-Type"="application/json"; "Authorization"="Bearer $token"} -Body ($newAddress | ConvertTo-Json)
        Write-Host "Create address successful: $($createAddressResponse | ConvertTo-Json -Depth 3)"
        $addressId = $createAddressResponse.data.id
        Write-Host "New address ID: $addressId"
    } else {
        $addressId = $addressListResponse.data[0].id
        Write-Host "Using address ID: $addressId"
    }
} catch {
    Write-Host "Get address list failed: $($_.Exception.Message)"
    # Create address anyway
    try {
        Write-Host "\n=== Create new address ==="
        $newAddress = @{
            userId = 2
            consignee = "Test User"
            phone = "13800138000"
            province = "Guangdong"
            city = "Shenzhen"
            district = "Nanshan"
            detailAddress = "Tech Park South"
            isDefault = 1
            status = 1
        }
        $createAddressResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/address/save" -Method POST -Headers @{"Content-Type"="application/json"; "Authorization"="Bearer $token"} -Body ($newAddress | ConvertTo-Json)
        Write-Host "Create address successful: $($createAddressResponse | ConvertTo-Json -Depth 3)"
        $addressId = $createAddressResponse.data.id
        Write-Host "New address ID: $addressId"
    } catch {
        Write-Host "Create address failed: $($_.Exception.Message)"
        exit 1
    }
}

# 3. Add product to cart
Write-Host "\n=== 3. Test add product to cart ==="
try {
    $cartItem = @{
        userId = 2
        productId = 1
        quantity = 1
        selectedSpecs = '{"cup_type":"中杯","taste":"标准糖","temperature":"正常冰"}'
        unitPrice = 12.00
        productName = "珍珠奶茶"
        productImage = "pearl_milk_tea.jpg"
        isSelected = 1
    }
    $addCartResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/cart/add" -Method POST -Headers @{"Content-Type"="application/json"; "Authorization"="Bearer $token"} -Body ($cartItem | ConvertTo-Json)
    Write-Host "Add product to cart successful: $($addCartResponse | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "Add product to cart failed: $($_.Exception.Message)"
    # Continue even if cart addition fails
}

# 4. Create delivery order
Write-Host "\n=== 4. Test create delivery order ==="
try {
    $order = @{
        orderType = 2  # Delivery
        paymentMethod = 1  # WeChat Pay
        addressId = $addressId
        userRemark = "Less ice, no sugar"
        couponId = 1  # Use coupon
        discountAmount = 5.00  # Discount amount
    }
    $createOrderResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/order/create?userId=2" -Method POST -Headers @{"Content-Type"="application/json"; "Authorization"="Bearer $token"} -Body ($order | ConvertTo-Json)
    Write-Host "Create delivery order successful: $($createOrderResponse | ConvertTo-Json -Depth 3)"
    $orderId = $createOrderResponse.data.id
    Write-Host "Order ID: $orderId"
} catch {
    Write-Host "Create delivery order failed: $($_.Exception.Message)"
    exit 1
}

# 5. Get order detail
Write-Host "\n=== 5. Test get order detail ==="
try {
    $orderDetailResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/order/detail/$orderId" -Method GET -Headers @{"Authorization"="Bearer $token"}
    Write-Host "Get order detail successful: $($orderDetailResponse | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "Get order detail failed: $($_.Exception.Message)"
}

# 6. Admin update order status (simulate payment success)
Write-Host "\n=== 6. Test admin update order status ==="
try {
    $updateRequest = @{
        orderId = $orderId
        status = 2  # Payment success
        adminRemark = "Order paid"
    }
    $updateResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/order/admin/order/update" -Method POST -Headers @{"Content-Type"="application/json"; "Authorization"="Bearer $token"} -Body ($updateRequest | ConvertTo-Json)
    Write-Host "Update order status successful: $($updateResponse | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "Update order status failed: $($_.Exception.Message)"
}

# 7. Get order detail again (check take code)
Write-Host "\n=== 7. Test get order detail again (check take code) ==="
try {
    $orderDetailResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/order/detail/$orderId" -Method GET -Headers @{"Authorization"="Bearer $token"}
    Write-Host "Get order detail successful: $($orderDetailResponse | ConvertTo-Json -Depth 3)"
    $takeCode = $orderDetailResponse.data.takeCode
    Write-Host "Take code: $takeCode"
} catch {
    Write-Host "Get order detail failed: $($_.Exception.Message)"
}

# 8. Validate take code
Write-Host "\n=== 8. Test validate take code ==="
try {
    $validateResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/order/validate-take-code?takeCode=$takeCode" -Method POST -Headers @{"Authorization"="Bearer $token"}
    Write-Host "Validate take code successful: $($validateResponse | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "Validate take code failed: $($_.Exception.Message)"
}

# 9. Admin update order status to completed
Write-Host "\n=== 9. Test admin update order status to completed ==="
try {
    $updateRequest = @{
        orderId = $orderId
        status = 3  # Completed
        adminRemark = "Order completed"
    }
    $updateResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/order/admin/order/update" -Method POST -Headers @{"Content-Type"="application/json"; "Authorization"="Bearer $token"} -Body ($updateRequest | ConvertTo-Json)
    Write-Host "Update order status to completed successful: $($updateResponse | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "Update order status to completed failed: $($_.Exception.Message)"
}

# 10. Validate take code (completed status)
Write-Host "\n=== 10. Test validate take code (completed status) ==="
try {
    $validateResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/order/validate-take-code?takeCode=$takeCode" -Method POST -Headers @{"Authorization"="Bearer $token"}
    Write-Host "Validate take code successful: $($validateResponse | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "Validate take code failed: $($_.Exception.Message)"
}
