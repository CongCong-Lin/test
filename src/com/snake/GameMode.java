package com.snake;

public enum GameMode {
    CLASSIC("经典模式", "传统玩法，碰壁即死"),
    NO_WALL("无尽模式", "穿墙而过，永不停歇"),
    CHALLENGE("挑战模式", "躲避障碍，挑战极限"),
    TIME_ATTACK("限时模式", "60秒内争取得分王");

    private final String name;
    private final String description;

    GameMode(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
}
