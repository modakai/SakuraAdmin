param(
    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string]$ProjectName,

    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$')]
    [string]$JavaPackage,

    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[a-zA-Z][a-zA-Z0-9_]*$')]
    [string]$DatabaseName,

    [ValidateRange(1, 65535)]
    [int]$BackendPort = 8101,

    [ValidateRange(1, 65535)]
    [int]$FrontendPort = 5173,

    [string]$AppTitle,

    [ValidatePattern('^[a-z0-9][a-z0-9-]*[a-z0-9]$')]
    [string]$DockerPrefix,

    [ValidateNotNullOrEmpty()]
    [string]$AdminUsername = 'sakura',

    [ValidateNotNullOrEmpty()]
    [string]$AdminPassword = '12345678',

    [switch]$DryRun,

    [switch]$Force,

    [switch]$SkipJavaPackageRename
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$TemplateProjectName = 'Sakura Admin'
$TemplateArtifactName = 'springboot3_init'
$TemplatePackage = 'com.sakura.boot_init'
$TemplatePackagePath = 'com\sakura\boot_init'
$TemplateDatabaseName = 'sakura_boot_init'
$TemplateDockerPrefix = 'sakura'
$TemplateAdminUsername = 'sakura'
$TemplateAdminPassword = '12345678'
$TemplatePasswordHash = '2a6dd3323691b39e8e9b1132b035ede5'
$PasswordSalt = 'sakura'

if ([string]::IsNullOrWhiteSpace($AppTitle)) {
    $AppTitle = $ProjectName
}

function Convert-ToKebabCase {
    param([Parameter(Mandatory = $true)][string]$Value)

    # 把项目名标准化为 Docker、前端包名和容器名前缀可用的 kebab-case。
    $normalized = $Value.Trim() -creplace '([a-z0-9])([A-Z])', '$1-$2'
    $normalized = $normalized -replace '[^A-Za-z0-9]+', '-'
    $normalized = $normalized.Trim('-').ToLowerInvariant()
    if ([string]::IsNullOrWhiteSpace($normalized)) {
        throw 'ProjectName 无法转换为有效的 kebab-case 名称。'
    }
    return $normalized
}

function Convert-ToArtifactName {
    param([Parameter(Mandatory = $true)][string]$Value)

    # Maven artifactId 使用小写、数字、连字符，和 Docker 前缀保持一致。
    return Convert-ToKebabCase -Value $Value
}

function Convert-ToPackagePath {
    param([Parameter(Mandatory = $true)][string]$PackageName)

    return ($PackageName -replace '\.', [IO.Path]::DirectorySeparatorChar)
}

function Convert-ToMd5 {
    param([Parameter(Mandatory = $true)][string]$Value)

    # 后端当前密码算法是 md5(PASSWORD_SALT + 明文密码)，初始化脚本必须保持一致。
    $md5 = [System.Security.Cryptography.MD5]::Create()
    try {
        $bytes = [System.Text.Encoding]::UTF8.GetBytes($Value)
        $hash = $md5.ComputeHash($bytes)
        return (($hash | ForEach-Object { $_.ToString('x2') }) -join '')
    }
    finally {
        $md5.Dispose()
    }
}

function Assert-ProjectRoot {
    param([Parameter(Mandatory = $true)][string]$Root)

    $requiredPaths = @(
        'README.md',
        'docker-compose.yml',
        'springboot3_init\pom.xml',
        'shadcn-vue-app\package.json'
    )

    foreach ($path in $requiredPaths) {
        $fullPath = Join-Path $Root $path
        if (-not (Test-Path -LiteralPath $fullPath)) {
            throw "当前目录不像 SakuraAdmin 根目录，缺少：$path"
        }
    }
}

function Assert-GitClean {
    param([Parameter(Mandatory = $true)][string]$Root)

    if (-not (Test-Path -LiteralPath (Join-Path $Root '.git'))) {
        return
    }

    $status = git -C $Root status --short
    if ($LASTEXITCODE -ne 0) {
        throw '无法读取 Git 状态，请先确认 Git 可用。'
    }

    if ($status -and -not $Force) {
        throw "Git 工作区不是干净状态。请先提交/暂存现有修改，或明确传入 -Force。`n$status"
    }
}

function Get-TargetFiles {
    param([Parameter(Mandatory = $true)][string]$Root)

    $fixedFiles = @(
        'README.md',
        'docker-compose.yml',
        'springboot3_init\AGENT.md',
        'springboot3_init\Dockerfile',
        'springboot3_init\pom.xml',
        'springboot3_init\src\main\resources\application.yml',
        'springboot3_init\src\main\resources\application-dev.yml',
        'springboot3_init\src\main\resources\application-prod.yml',
        'springboot3_init\src\main\resources\application-test.yml',
        'springboot3_init\sql\mysql\create_table.sql',
        'springboot3_init\sql\mysql\init_data.sql',
        'springboot3_init\sql\postgresql\create_table.sql',
        'springboot3_init\sql\postgresql\init_data.sql',
        'shadcn-vue-app\Dockerfile',
        'shadcn-vue-app\nginx\default.conf',
        'shadcn-vue-app\package.json',
        'shadcn-vue-app\vite.config.ts',
        'shadcn-vue-app\.env',
        'shadcn-vue-app\.env.development',
        'shadcn-vue-app\.env.production',
        'shadcn-vue-app\.env.test',
        'shadcn-vue-app\src\utils\document-title.ts',
        'shadcn-vue-app\src\plugins\i18n\zh.json',
        'shadcn-vue-app\src\plugins\i18n\en.json',
        'shadcn-vue-app\src\components\app-sidebar\sidebar-logo.vue',
        'shadcn-vue-app\src\pages\auth\components\auth-title.vue',
        'shadcn-vue-app\src\pages\auth\components\login-form.vue',
        'shadcn-vue-app\src\pages\users\components\user-protection.ts'
    )

    $files = New-Object System.Collections.Generic.List[string]
    foreach ($relativePath in $fixedFiles) {
        $fullPath = Join-Path $Root $relativePath
        if (Test-Path -LiteralPath $fullPath) {
            $files.Add((Resolve-Path -LiteralPath $fullPath).Path)
        }
    }

    if (-not $SkipJavaPackageRename) {
        $sourceRoots = @(
            'springboot3_init\src\main\java',
            'springboot3_init\src\test\java',
            'springboot3_init\src\main\resources\mapper'
        )

        foreach ($relativeRoot in $sourceRoots) {
            $fullRoot = Join-Path $Root $relativeRoot
            if (Test-Path -LiteralPath $fullRoot) {
                Get-ChildItem -LiteralPath $fullRoot -Recurse -File -Include *.java,*.xml |
                    ForEach-Object { $files.Add($_.FullName) }
            }
        }
    }

    return $files | Select-Object -Unique
}

function New-ReplacementRule {
    param(
        [Parameter(Mandatory = $true)][string]$Old,
        [Parameter(Mandatory = $true)][string]$New,
        [string[]]$PathPatterns = @()
    )

    [pscustomobject]@{
        Old = $Old
        New = $New
        PathPatterns = $PathPatterns
    }
}

function Test-PathMatchesRule {
    param(
        [Parameter(Mandatory = $true)][string]$RelativePath,
        [Parameter(Mandatory = $true)]$Rule
    )

    if (-not $Rule.PathPatterns -or $Rule.PathPatterns.Count -eq 0) {
        return $true
    }

    foreach ($pattern in $Rule.PathPatterns) {
        if ($RelativePath -like $pattern) {
            return $true
        }
    }

    return $false
}

function Update-FileContent {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$Root,
        [Parameter(Mandatory = $true)]$Rules,
        [switch]$PreviewOnly
    )

    $relativePath = [IO.Path]::GetRelativePath($Root, $Path)
    $original = Get-Content -LiteralPath $Path -Raw
    if ($null -eq $original) {
        $original = ''
    }
    $updated = $original
    $hitCount = 0
    $details = New-Object System.Collections.Generic.List[string]

    foreach ($rule in $Rules) {
        if (-not (Test-PathMatchesRule -RelativePath $relativePath -Rule $rule)) {
            continue
        }

        $count = ([regex]::Matches($updated, [regex]::Escape($rule.Old))).Count
        if ($count -le 0) {
            continue
        }

        $updated = $updated.Replace($rule.Old, $rule.New)
        $hitCount += $count
        $details.Add("$($rule.Old) -> $($rule.New) ($count)")
    }

    if ($hitCount -gt 0 -and -not $PreviewOnly) {
        # 直接写入替换后的完整字符串，避免 Set-Content 额外改动文件末尾换行。
        [IO.File]::WriteAllText($Path, $updated, [Text.UTF8Encoding]::new($false))
    }

    [pscustomobject]@{
        RelativePath = $relativePath
        HitCount = $hitCount
        Details = $details
    }
}

function Move-JavaPackageDirectory {
    param(
        [Parameter(Mandatory = $true)][string]$Root,
        [Parameter(Mandatory = $true)][string]$SourceRoot,
        [Parameter(Mandatory = $true)][string]$NewPackagePath,
        [switch]$PreviewOnly
    )

    $oldDirectory = Join-Path (Join-Path $Root $SourceRoot) $TemplatePackagePath
    $newDirectory = Join-Path (Join-Path $Root $SourceRoot) $NewPackagePath

    if (-not (Test-Path -LiteralPath $oldDirectory)) {
        return [pscustomobject]@{
            Source = [IO.Path]::GetRelativePath($Root, $oldDirectory)
            Target = [IO.Path]::GetRelativePath($Root, $newDirectory)
            Moved = $false
            Reason = '源目录不存在，跳过。'
        }
    }

    if (Test-Path -LiteralPath $newDirectory) {
        throw "目标 Java 包目录已存在，避免覆盖：$newDirectory"
    }

    if (-not $PreviewOnly) {
        New-Item -ItemType Directory -Path (Split-Path -Parent $newDirectory) -Force | Out-Null
        Move-Item -LiteralPath $oldDirectory -Destination $newDirectory

        # 清理移动后留下的空目录，避免旧包路径残留。
        $cursor = Split-Path -Parent $oldDirectory
        while ($cursor -and $cursor.StartsWith((Join-Path $Root $SourceRoot))) {
            if ((Get-ChildItem -LiteralPath $cursor -Force | Select-Object -First 1)) {
                break
            }
            Remove-Item -LiteralPath $cursor -Force
            $cursor = Split-Path -Parent $cursor
        }
    }

    [pscustomobject]@{
        Source = [IO.Path]::GetRelativePath($Root, $oldDirectory)
        Target = [IO.Path]::GetRelativePath($Root, $newDirectory)
        Moved = $true
        Reason = ''
    }
}

# 无论用户从哪个目录调用脚本，都以 scripts 目录的上一级作为项目根目录。
$Root = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
Assert-ProjectRoot -Root $Root
Assert-GitClean -Root $Root

$ProjectSlug = if ([string]::IsNullOrWhiteSpace($DockerPrefix)) {
    Convert-ToKebabCase -Value $ProjectName
}
else {
    $DockerPrefix
}
$ArtifactName = Convert-ToArtifactName -Value $ProjectSlug
$NewPackagePath = Convert-ToPackagePath -PackageName $JavaPackage
$AdminPasswordHash = Convert-ToMd5 -Value ($PasswordSalt + $AdminPassword)

$rules = @(
    New-ReplacementRule -Old $TemplateProjectName -New $AppTitle
    New-ReplacementRule -Old $TemplateDatabaseName -New $DatabaseName
    New-ReplacementRule -Old "target/$TemplateArtifactName-0.0.1-SNAPSHOT.jar" -New "target/$ArtifactName-0.0.1-SNAPSHOT.jar" -PathPatterns @('README.md')
    New-ReplacementRule -Old "/workspace/target/$TemplateArtifactName-0.0.1-SNAPSHOT.jar" -New "/workspace/target/$ArtifactName-0.0.1-SNAPSHOT.jar" -PathPatterns @('springboot3_init\Dockerfile')
    New-ReplacementRule -Old "@Modulithic(systemName = `"$TemplateArtifactName`")" -New "@Modulithic(systemName = `"$ArtifactName`")" -PathPatterns @('springboot3_init\src\main\java\*\SakuraApplication.java')
    New-ReplacementRule -Old 'sakura-boot-init-api' -New "$ProjectSlug-api"
    New-ReplacementRule -Old 'sakura-admin-api' -New "$ProjectSlug-api"
    New-ReplacementRule -Old 'sakura-admin' -New $ProjectSlug
    New-ReplacementRule -Old 'sakura-web' -New "$ProjectSlug-web"
    New-ReplacementRule -Old 'sakura-api' -New "$ProjectSlug-api"
    New-ReplacementRule -Old 'sakura-mysql' -New "$ProjectSlug-mysql"
    New-ReplacementRule -Old 'sakura-redis' -New "$ProjectSlug-redis"
    New-ReplacementRule -Old 'sakura-mysql-data' -New "$ProjectSlug-mysql-data"
    New-ReplacementRule -Old 'sakura-redis-data' -New "$ProjectSlug-redis-data"
    New-ReplacementRule -Old 'http://localhost:8101' -New "http://localhost:$BackendPort"
    New-ReplacementRule -Old 'SERVER_PORT=8101' -New "SERVER_PORT=$BackendPort"
    New-ReplacementRule -Old 'server.port=8101' -New "server.port=$BackendPort"
    New-ReplacementRule -Old 'port: 8101' -New "port: $BackendPort" -PathPatterns @('springboot3_init\src\main\resources\application-*.yml')
    New-ReplacementRule -Old '${API_PORT:-8101}:8101' -New "`${API_PORT:-$BackendPort}:$BackendPort" -PathPatterns @('docker-compose.yml')
    New-ReplacementRule -Old 'SERVER_PORT: 8101' -New "SERVER_PORT: $BackendPort" -PathPatterns @('docker-compose.yml')
    New-ReplacementRule -Old '-p 8101:8101' -New "-p ${BackendPort}:$BackendPort" -PathPatterns @('README.md')
    New-ReplacementRule -Old '| 后端 API | `sakura-api` | `8101`' -New "| 后端 API | ``$ProjectSlug-api`` | ``$BackendPort``" -PathPatterns @('README.md')
    New-ReplacementRule -Old "| 后端 API | ``$ProjectSlug-api`` | ``8101``" -New "| 后端 API | ``$ProjectSlug-api`` | ``$BackendPort``" -PathPatterns @('README.md')
    New-ReplacementRule -Old 'proxy_pass http://api:8101/api/;' -New "proxy_pass http://api:$BackendPort/api/;" -PathPatterns @('shadcn-vue-app\nginx\default.conf')
    New-ReplacementRule -Old 'VITE_SERVER_API_URL=http://localhost:8101' -New "VITE_SERVER_API_URL=http://localhost:$BackendPort" -PathPatterns @('shadcn-vue-app\.env*')
    New-ReplacementRule -Old 'port: 5173' -New "port: $FrontendPort" -PathPatterns @('shadcn-vue-app\vite.config.ts')
    New-ReplacementRule -Old '"name": "shadcn-vue-admin"' -New "`"name`": `"$ProjectSlug-web`"" -PathPatterns @('shadcn-vue-app\package.json')
    New-ReplacementRule -Old "<artifactId>$TemplateArtifactName</artifactId>" -New "<artifactId>$ArtifactName</artifactId>" -PathPatterns @('springboot3_init\pom.xml')
    New-ReplacementRule -Old "<name>$TemplateArtifactName</name>" -New "<name>$ArtifactName</name>" -PathPatterns @('springboot3_init\pom.xml')
    New-ReplacementRule -Old $TemplatePackage -New $JavaPackage
    New-ReplacementRule -Old $TemplatePackagePath -New $NewPackagePath
    New-ReplacementRule -Old "'$TemplateAdminUsername'" -New "'$AdminUsername'" -PathPatterns @(
        'README.md',
        'springboot3_init\sql\mysql\init_data.sql',
        'springboot3_init\sql\postgresql\init_data.sql'
    )
    New-ReplacementRule -Old '默认超级管理员账号：sakura，密码：' -New "默认超级管理员账号：$AdminUsername，密码：" -PathPatterns @(
        'springboot3_init\sql\postgresql\init_data.sql'
    )
    New-ReplacementRule -Old '账号：sakura，密码：' -New "账号：$AdminUsername，密码：" -PathPatterns @(
        'springboot3_init\sql\mysql\init_data.sql'
    )
    New-ReplacementRule -Old "账号：$TemplateAdminUsername" -New "账号：$AdminUsername" -PathPatterns @('README.md')
    New-ReplacementRule -Old "$TemplateAdminUsername / $TemplateAdminPassword" -New "$AdminUsername / $AdminPassword" -PathPatterns @('README.md')
    New-ReplacementRule -Old "PROTECTED_SUPER_ADMIN_ACCOUNT = `"$TemplateAdminUsername`";" -New "PROTECTED_SUPER_ADMIN_ACCOUNT = `"$AdminUsername`";" -PathPatterns @(
        'springboot3_init\src\main\java\*\shared\constant\UserConstant.java'
    )
    New-ReplacementRule -Old "PROTECTED_SUPER_ADMIN_ACCOUNT = '$TemplateAdminUsername'" -New "PROTECTED_SUPER_ADMIN_ACCOUNT = '$AdminUsername'" -PathPatterns @(
        'shadcn-vue-app\src\pages\users\components\user-protection.ts',
        'shadcn-vue-app\src\plugins\i18n\*.json'
    )
    New-ReplacementRule -Old "密码：$TemplateAdminPassword" -New "密码：$AdminPassword" -PathPatterns @('README.md')
    New-ReplacementRule -Old "DEFAULT_PASSWORD = `"$TemplateAdminPassword`";" -New "DEFAULT_PASSWORD = `"$AdminPassword`";" -PathPatterns @(
        'springboot3_init\src\main\java\*\shared\constant\UserConstant.java'
    )
    New-ReplacementRule -Old $TemplateAdminPassword -New $AdminPassword -PathPatterns @(
        'README.md',
        'springboot3_init\sql\mysql\init_data.sql',
        'springboot3_init\sql\postgresql\init_data.sql',
        'shadcn-vue-app\src\pages\auth\components\login-form.vue',
        'shadcn-vue-app\src\plugins\i18n\*.json'
    )
    New-ReplacementRule -Old $TemplatePasswordHash -New $AdminPasswordHash -PathPatterns @(
        'springboot3_init\sql\mysql\init_data.sql',
        'springboot3_init\sql\postgresql\init_data.sql'
    )
)

Write-Host "SakuraAdmin 初始化脚本"
Write-Host "Root: $Root"
Write-Host "ProjectName: $ProjectName"
Write-Host "AppTitle: $AppTitle"
Write-Host "ProjectSlug: $ProjectSlug"
Write-Host "JavaPackage: $JavaPackage"
Write-Host "DatabaseName: $DatabaseName"
Write-Host "BackendPort: $BackendPort"
Write-Host "FrontendPort: $FrontendPort"
Write-Host "AdminUsername: $AdminUsername"
Write-Host "Mode: $(if ($DryRun) { 'DryRun' } else { 'Write' })"

$targetFiles = Get-TargetFiles -Root $Root
$results = foreach ($file in $targetFiles) {
    Update-FileContent -Path $file -Root $Root -Rules $rules -PreviewOnly:$DryRun
}

$changedResults = $results | Where-Object { $_.HitCount -gt 0 }
if ($changedResults) {
    Write-Host ''
    Write-Host "$(if ($DryRun) { '[DryRun] 将修改文件：' } else { '已修改文件：' })"
    foreach ($result in $changedResults) {
        Write-Host "- $($result.RelativePath)：命中 $($result.HitCount) 处"
        foreach ($detail in $result.Details) {
            Write-Host "  - $detail"
        }
    }
}
else {
    Write-Host ''
    Write-Host '没有发现需要替换的模板文本。'
}

if (-not $SkipJavaPackageRename) {
    Write-Host ''
    Write-Host "$(if ($DryRun) { '[DryRun] 将移动 Java 包目录：' } else { 'Java 包目录处理：' })"
    $moveResults = @(
        Move-JavaPackageDirectory -Root $Root -SourceRoot 'springboot3_init\src\main\java' -NewPackagePath $NewPackagePath -PreviewOnly:$DryRun
        Move-JavaPackageDirectory -Root $Root -SourceRoot 'springboot3_init\src\test\java' -NewPackagePath $NewPackagePath -PreviewOnly:$DryRun
    )

    foreach ($move in $moveResults) {
        if ($move.Moved) {
            Write-Host "- from: $($move.Source)"
            Write-Host "  to:   $($move.Target)"
        }
        else {
            Write-Host "- $($move.Source)：$($move.Reason)"
        }
    }
}

Write-Host ''
if ($DryRun) {
    Write-Host 'DryRun 完成：未写入任何文件。去掉 -DryRun 后会执行上述修改。'
}
else {
    Write-Host '初始化完成。建议继续执行：'
    Write-Host '1. git diff --stat'
    Write-Host '2. cd springboot3_init; mvn -DskipTests compile'
    Write-Host '3. cd ../shadcn-vue-app; pnpm build'
    Write-Host "4. rg -n `"$TemplateProjectName|$TemplateDatabaseName|$TemplateArtifactName|$TemplatePackage|$TemplateDockerPrefix-api|$TemplateDockerPrefix-web`""
}
