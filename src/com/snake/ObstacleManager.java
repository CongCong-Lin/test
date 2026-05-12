package com.snake;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ObstacleManager {
    private List<Point> obstacles;
    private final Random random = new Random();
    private final int gridSize;
    private final int gridCount;

    public ObstacleManager(int gridSize, int gridCount) {
        this.gridSize = gridSize;
        this.gridCount = gridCount;
        this.obstacles = new ArrayList<>();
    }

    public void generateObstacles(int count, Snake snake) {
        obstacles.clear();
        for (int i = 0; i < count; i++) {
            Point p;
            do {
                p = new Point(random.nextInt(gridCount), random.nextInt(gridCount));
            } while (snake.occupies(p) || isNearStart(p, snake));
            obstacles.add(p);
        }
    }

    private boolean isNearStart(Point p, Snake snake) {
        Point head = snake.getHead();
        return Math.abs(p.x - head.x) < 5 && Math.abs(p.y - head.y) < 5;
    }

    public void addLevelObstacles(int level, Snake snake) {
        int count = Math.min(level * 3, 30);
        generateObstacles(count, snake);
    }

    public List<Point> getObstacles() {
        return obstacles;
    }

    public boolean contains(Point p) {
        return obstacles.contains(p);
    }

    public void clear() {
        obstacles.clear();
    }
}
