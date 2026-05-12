#!/bin/bash
echo "================================"
echo "       贪吃蛇 - Snake Game"
echo "================================"
echo ""
echo "编译中..."
javac -encoding UTF-8 -d out src/com/snake/*.java
if [ $? -ne 0 ]; then
    echo "编译失败!"
    exit 1
fi
echo "编译成功!"
echo ""
echo "启动游戏..."
java -cp out com.snake.Main
