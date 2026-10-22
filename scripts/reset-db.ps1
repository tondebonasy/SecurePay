param(
  [string]$DbName = "moneyguard",
  [string]$User = "root",
  [string]$Password = "",
  [string]$DbHost = "localhost",
  [int]$Port = 3306,
  [string]$MysqlBin = "mysql"
)

Write-Host "Resetting MySQL database '$DbName' on ${DbHost}:$Port as $User"
if ([string]::IsNullOrWhiteSpace($Password)) {
  & $MysqlBin -h $DbHost -P $Port -u $User -e "DROP DATABASE IF EXISTS $DbName; CREATE DATABASE $DbName;"
} else {
  & $MysqlBin -h $DbHost -P $Port -u $User -p"$Password" -e "DROP DATABASE IF EXISTS $DbName; CREATE DATABASE $DbName;"
}

if ($LASTEXITCODE -ne 0) {
  Write-Error "MySQL command failed. Check credentials and mysql.exe path."
  exit 1
}

Write-Host "Database reset complete. Start backend to re-run Flyway migrations."
