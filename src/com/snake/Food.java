package com.snake;

import java.awt.Point;
import java.util.Random;

public class Food {
    private Point position;
    private FoodType type;
    private final Random random = new Random();
    private long spawnTime;
    private static final long LIFETIME = 10000; // 10秒消失

    public Food() {
        this.position = new Point(0, 0);
        this.type = FoodType.NORMAL;
        this.spawnTime = System.currentTimeMillis();
    }

    public void spawn(int maxX, int maxY, Snake snake) {
        spawn(maxX, maxY, snake, null);
    }

    public void spawn(int maxX, int maxY, Snake snake, java.util.List<Point> obstacles) {
        do {
            position.setLocation(random.nextInt(maxX), random.nextInt(maxY));
        } while (snake.occupies(position) || (obstacles != null && obstacles.contains(position)));

        // 随机食物类型权重
        int roll = random.nextInt(100);
        if (roll < 50) type = FoodType.NORMAL;
        else if (roll < 65) type = FoodType.SPEED_BOOST;
        else if (roll < 78) type = FoodType.SLOW_DOWN;
        else if (roll < 88) type = FoodType.DOUBLE_SCORE;
        else if (roll < 95) type = FoodType.GHOST;
        else type = FoodType.SHRINK;

        spawnTime = System.currentTimeMillis();
    }

    public Point getPosition() {
        return position;
    }

    public FoodType getType() {
        return type;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - spawnTime > LIFETIME;
    }

    public double getRemainingRatio() {
        return 1.0 - (double)(System.currentTimeMillis() - spawnTime) / LIFETIME;
    }
}
