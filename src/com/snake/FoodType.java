package com.snake;

import java.awt.Color;

public enum FoodType {
    NORMAL(Color.RED, Color.PINK, 10, "普通"),
    SPEED_BOOST(Color.YELLOW, Color.ORANGE, 15, "加速"),
    SLOW_DOWN(Color.CYAN, Color.BLUE, 15, "减速"),
    DOUBLE_SCORE(Color.MAGENTA, new Color(255, 100, 255), 20, "双倍"),
    GHOST(Color.WHITE, Color.LIGHT_GRAY, 25, "穿墙"),
    SHRINK(new Color(0, 255, 0), Color.GREEN, 30, "缩短");

    private final Color primaryColor;
    private final Color secondaryColor;
    private final int score;
    private final String name;

    FoodType(Color primary, Color secondary, int score, String name) {
        this.primaryColor = primary;
        this.secondaryColor = secondary;
        this.score = score;
        this.name = name;
    }

    public Color getPrimaryColor() { return primaryColor; }
    public Color getSecondaryColor() { return secondaryColor; }
    public int getScore() { return score; }
    public String getName() { return name; }
}
