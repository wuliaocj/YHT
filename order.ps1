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

# 2. 查看商品详情（假设商品ID为6）
Write-Host "\n=== 2. 查看商品详情 ==="
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/product/detail/6" -Method GET -Headers $headers -UseBasicParsing
    Write-Host "商品详情响应: $($response.Content)"
} catch {
    Write-Host "获取商品详情失败: $($_.Exception.Message)"
}

# 3. 添加商品到购物车
Write-Host "\n=== 3. 添加商品到购物车 ==="
$cartBody = @{
    "productId" = 6
    "quantity" = 1
    "selectedSpecs" = "{\"cup_type\":\"中杯\",\"taste\":\"标准糖\",\"temperature\":\"去冰\",\"topping\":[\"珍珠\",\"布丁\"]}"
    "unitPrice" = 12.00
    "productName" = "招牌奶茶"
    "productImage" = "http://localhost:8080/upload/images/d32159289a4d4daba3a1da760a322251.jpg"
    "specIds" = "[1,6,11,3,4]"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/cart/add" -Method POST -Headers $headers -Body $cartBody -UseBasicParsing
    Write-Host "添加购物车响应: $($response.Content)"
} catch {
    Write-Host "添加购物车失败: $($_.Exception.Message)"
}

# 4. 查看购物车
Write-Host "\n=== 4. 查看购物车 ==="
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/cart/list/2" -Method GET -Headers $headers -UseBasicParsing
    Write-Host "购物车响应: $($response.Content)"
} catch {
    Write-Host "获取购物车失败: $($_.Exception.Message)"
}
