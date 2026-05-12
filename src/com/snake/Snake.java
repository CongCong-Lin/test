package com.snake;

import java.awt.Point;
import java.util.LinkedList;

public class Snake {
    private final LinkedList<Point> body;
    private Direction direction;
    private boolean growing;
    private boolean ghostMode;
    private long ghostEndTime;
    private long speedBoostEndTime;
    private long slowDownEndTime;
    private long doubleScoreEndTime;
    private int baseSpeed;

    public Snake(int startX, int startY) {
        body = new LinkedList<>();
        body.add(new Point(startX, startY));
        body.add(new Point(startX - 1, startY));
        body.add(new Point(startX - 2, startY));
        direction = Direction.RIGHT;
        growing = false;
        ghostMode = false;
        baseSpeed = 120;
    }

    public void setDirection(Direction newDir) {
        if ((direction == Direction.UP && newDir == Direction.DOWN) ||
            (direction == Direction.DOWN && newDir == Direction.UP) ||
            (direction == Direction.LEFT && newDir == Direction.RIGHT) ||
            (direction == Direction.RIGHT && newDir == Direction.LEFT)) {
            return;
        }
        this.direction = newDir;
    }

    public Direction getDirection() {
        return direction;
    }

    public void move() {
        Point head = body.getFirst();
        Point newHead = switch (direction) {
            case UP -> new Point(head.x, head.y - 1);
            case DOWN -> new Point(head.x, head.y + 1);
            case LEFT -> new Point(head.x - 1, head.y);
            case RIGHT -> new Point(head.x + 1, head.y);
        };

        body.addFirst(newHead);
        if (!growing) {
            body.removeLast();
        }
        growing = false;

        updateEffects();
    }

    private void updateEffects() {
        long now = System.currentTimeMillis();
        if (ghostMode && now > ghostEndTime) ghostMode = false;
    }

    public void grow() {
        growing = true;
    }

    public void shrink() {
        if (body.size() > 3) {
            body.removeLast();
            body.removeLast();
        }
    }

    public void activateGhost(long duration) {
        ghostMode = true;
        ghostEndTime = System.currentTimeMillis() + duration;
    }

    public void activateSpeedBoost(long duration) {
        speedBoostEndTime = System.currentTimeMillis() + duration;
    }

    public void activateSlowDown(long duration) {
        slowDownEndTime = System.currentTimeMillis() + duration;
    }

    public void activateDoubleScore(long duration) {
        doubleScoreEndTime = System.currentTimeMillis() + duration;
    }

    public boolean isGhostMode() {
        return ghostMode;
    }

    public boolean isSpeedBoosted() {
        return System.currentTimeMillis() < speedBoostEndTime;
    }

    public boolean isSlowedDown() {
        return System.currentTimeMillis() < slowDownEndTime;
    }

    public boolean isDoubleScore() {
        return System.currentTimeMillis() < doubleScoreEndTime;
    }

    public int getCurrentDelay() {
        if (isSpeedBoosted()) return Math.max(40, baseSpeed - 40);
        if (isSlowedDown()) return baseSpeed + 40;
        return baseSpeed;
    }

    public void setBaseSpeed(int speed) {
        this.baseSpeed = speed;
    }

    public Point getHead() {
        return body.getFirst();
    }

    public LinkedList<Point> getBody() {
        return body;
    }

    public boolean occupies(Point p) {
        return body.contains(p);
    }

    public boolean collidesWithSelf() {
        Point head = getHead();
        return body.stream().skip(1).anyMatch(p -> p.equals(head));
    }

    public boolean collidesWithWall(int maxX, int maxY) {
        if (ghostMode) return false;
        Point head = getHead();
        return head.x < 0 || head.x >= maxX || head.y < 0 || head.y >= maxY;
    }

    public void wrapAround(int maxX, int maxY) {
        Point head = getHead();
        if (head.x < 0) head.x = maxX - 1;
        else if (head.x >= maxX) head.x = 0;
        if (head.y < 0) head.y = maxY - 1;
        else if (head.y >= maxY) head.y = 0;
    }

    public boolean collidesWithObstacle(java.util.List<Point> obstacles) {
        if (ghostMode) return false;
        return obstacles.contains(getHead());
    }

    public int getLength() {
        return body.size();
    }

    public void reset(int startX, int startY) {
        body.clear();
        body.add(new Point(startX, startY));
        body.add(new Point(startX - 1, startY));
        body.add(new Point(startX - 2, startY));
        direction = Direction.RIGHT;
        growing = false;
        ghostMode = false;
        baseSpeed = 120;
        speedBoostEndTime = 0;
        slowDownEndTime = 0;
        doubleScoreEndTime = 0;
    }
}
