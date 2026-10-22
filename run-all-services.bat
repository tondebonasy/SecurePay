@echo off
setlocal

set "ROOT=%~dp0"

call :start_service "Backend" "%ROOT%backend" "mvn spring-boot:run"
call :start_service "Admin Web" "%ROOT%admin-web" "npm run dev"
call :start_service "Stellar Worker" "%ROOT%stellar-worker" "npm run start"
call :start_service "Mobile" "%ROOT%mobile" "npm run start"

echo.
echo All services have been launched in separate windows.
echo Close this window when you are done.
pause
exit /b 0

:start_service
set "NAME=%~1"
set "DIR=%~2"
set "CMD=%~3"

if not exist "%DIR%" (
  echo [WARN] %NAME% folder not found: %DIR%
  goto :eof
)

start "%NAME%" cmd /k "cd /d ""%DIR%"" && %CMD%"
goto :eof
