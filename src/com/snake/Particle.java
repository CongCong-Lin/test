package com.snake;

import java.awt.*;

public class Particle {
    private double x, y;
    private double vx, vy;
    private int life;
    private int maxLife;
    private Color color;
    private int size;

    public Particle(double x, double y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.maxLife = 20 + (int)(Math.random() * 20);
        this.life = maxLife;
        this.size = 3 + (int)(Math.random() * 5);
        double angle = Math.random() * Math.PI * 2;
        double speed = 1 + Math.random() * 3;
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
    }

    public void update() {
        x += vx;
        y += vy;
        vx *= 0.95;
        vy *= 0.95;
        life--;
    }

    public void draw(Graphics2D g2d) {
        float alpha = (float) life / maxLife;
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(),
                (int)(alpha * 255)));
        int s = (int)(size * alpha);
        g2d.fillOval((int)x - s/2, (int)y - s/2, s, s);
    }

    public boolean isDead() {
        return life <= 0;
    }
}
