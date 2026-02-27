# 测试优惠券功能

# 1. 管理员创建优惠券
Write-Host "=== 1. 测试管理员创建优惠券 ==="
try {
    $createCouponResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/coupon/save" -Method POST -Headers @{"Content-Type"="application/json"} -Body '{"name":"测试满减券","type":0,"value":5.00,"min_amount":20.00,"total_count":100,"remaining_count":100,"status":1,"apply_scope":0}'
    Write-Host "创建优惠券成功: $($createCouponResponse | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "创建优惠券失败: $($_.Exception.Message)"
}

# 2. 管理员获取优惠券列表
Write-Host "\n=== 2. 测试管理员获取优惠券列表 ==="
try {
    $couponListResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/coupon/list" -Method GET
    Write-Host "获取优惠券列表成功: $($couponListResponse | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "获取优惠券列表失败: $($_.Exception.Message)"
}

# 3. 用户领取优惠券（使用测试用户ID 2）
Write-Host "\n=== 3. 测试用户领取优惠券 ==="
try {
    $receiveCouponResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/coupon/receive?userId=2&couponId=1" -Method POST
    Write-Host "领取优惠券成功: $($receiveCouponResponse | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "领取优惠券失败: $($_.Exception.Message)"
}

# 4. 用户获取可用优惠券
Write-Host "\n=== 4. 测试用户获取可用优惠券 ==="
try {
    $availableCouponsResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/coupon/user/available/2" -Method GET
    Write-Host "获取可用优惠券成功: $($availableCouponsResponse | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "获取可用优惠券失败: $($_.Exception.Message)"
}

# 5. 用户获取所有优惠券
Write-Host "\n=== 5. 测试用户获取所有优惠券 ==="
try {
    $allCouponsResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/coupon/user/2" -Method GET
    Write-Host "获取所有优惠券成功: $($allCouponsResponse | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "获取所有优惠券失败: $($_.Exception.Message)"
}
