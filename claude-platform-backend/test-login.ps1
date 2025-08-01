# Claude Platform Backend 登录测试脚本

Write-Host "开始测试 Claude Platform Backend..." -ForegroundColor Green

# 服务器地址
$baseUrl = "http://localhost:10088"

# 测试健康检查
Write-Host "`n1. 测试健康检查接口..." -ForegroundColor Yellow
try {
    $healthResponse = Invoke-RestMethod -Uri "$baseUrl/api/health" -Method GET
    Write-Host "健康检查成功:" -ForegroundColor Green
    Write-Host ($healthResponse | ConvertTo-Json -Depth 2)
} catch {
    Write-Host "健康检查失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 测试登录接口
Write-Host "`n2. 测试默认管理员登录..." -ForegroundColor Yellow

$loginData = @{
    username = "admin"
    password = "123456"
} | ConvertTo-Json

$headers = @{
    "Content-Type" = "application/json"
}

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $loginData -Headers $headers
    
    Write-Host "登录成功!" -ForegroundColor Green
    Write-Host "Token: $($loginResponse.data.token.Substring(0, 50))..." -ForegroundColor Cyan
    Write-Host "用户角色: $($loginResponse.data.userInfo.userRole)" -ForegroundColor Cyan
    Write-Host "Token限制: $($loginResponse.data.userInfo.tokenLimit)" -ForegroundColor Cyan
    
    # 保存token用于后续测试
    $global:authToken = $loginResponse.data.token
    
} catch {
    Write-Host "登录失败: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $responseStream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($responseStream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "错误详情: $responseBody" -ForegroundColor Red
    }
    exit 1
}

# 测试需要认证的接口
Write-Host "`n3. 测试用户信息接口..." -ForegroundColor Yellow

$authHeaders = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $global:authToken"
}

try {
    $userInfoResponse = Invoke-RestMethod -Uri "$baseUrl/api/user/info" -Method GET -Headers $authHeaders
    
    Write-Host "获取用户信息成功!" -ForegroundColor Green
    Write-Host "用户名: $($userInfoResponse.data.username)" -ForegroundColor Cyan
    Write-Host "真实姓名: $($userInfoResponse.data.realName)" -ForegroundColor Cyan
    Write-Host "邮箱: $($userInfoResponse.data.email)" -ForegroundColor Cyan
    
} catch {
    Write-Host "获取用户信息失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 测试系统配置接口
Write-Host "`n4. 测试系统配置接口..." -ForegroundColor Yellow

try {
    $configResponse = Invoke-RestMethod -Uri "$baseUrl/api/system/default-settings" -Method GET -Headers $authHeaders
    
    Write-Host "获取系统配置成功!" -ForegroundColor Green
    Write-Host ($configResponse.data | ConvertTo-Json -Depth 2)
    
} catch {
    Write-Host "获取系统配置失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n所有测试完成!" -ForegroundColor Green
Write-Host "`n默认管理员账户信息:" -ForegroundColor Yellow
Write-Host "用户名: admin" -ForegroundColor White
Write-Host "密码: 123456" -ForegroundColor White
Write-Host "请及时修改默认密码!" -ForegroundColor Red