@echo off
chcp 65001 >nul
echo ================================
echo       贪吃蛇 - Snake Game
echo ================================
echo.

rem 尝试查找Java
set JAVA_CMD=java
set JAVAC_CMD=javac

if defined JAVA_HOME (
    set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
) else (
    where javac >nul 2>&1 || (
        echo 未找到Java编译器，请设置JAVA_HOME环境变量
        pause
        exit /b 1
    )
)

echo 编译中...
"%JAVAC_CMD%" -encoding UTF-8 -d out src\com\snake\*.java
if %errorlevel% neq 0 (
    echo 编译失败!
    pause
    exit /b 1
)
echo 编译成功!
echo.
echo 启动游戏...
"%JAVA_CMD%" -cp out com.snake.Main
