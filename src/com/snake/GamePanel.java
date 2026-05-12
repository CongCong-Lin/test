package com.snake;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GamePanel extends JPanel implements ActionListener {
    private static final int GRID_SIZE = 20;
    private static final int GRID_COUNT = 30;
    private static final int PANEL_SIZE = GRID_SIZE * GRID_COUNT;

    private Snake snake;
    private Food food;
    private Timer timer;
    private boolean running;
    private boolean paused;
    private int score;
    private int highScore;
    private int level;
    private GameMode gameMode;
    private GameState gameState;
    private ObstacleManager obstacleManager;
    private List<Particle> particles;
    private int timeRemaining; // 限时模式用
    private Timer gameTimer;

    // 菜单相关
    private int menuSelection;
    private static final GameMode[] MODES = GameMode.values();

    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_SIZE, PANEL_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(new KeyHandler());
        highScore = 0;
        particles = new ArrayList<>();
        obstacleManager = new ObstacleManager(GRID_SIZE, GRID_COUNT);
        gameState = GameState.MENU;
        menuSelection = 0;
    }

    private void initGame(GameMode mode) {
        this.gameMode = mode;
        snake = new Snake(GRID_COUNT / 2, GRID_COUNT / 2);
        food = new Food();
        running = true;
        paused = false;
        score = 0;
        level = 1;
        particles.clear();
        obstacleManager.clear();

        switch (mode) {
            case CHALLENGE -> obstacleManager.addLevelObstacles(1, snake);
            case TIME_ATTACK -> timeRemaining = 60;
        }

        food.spawn(GRID_COUNT, GRID_COUNT, snake, obstacleManager.getObstacles());

        timer = new Timer(snake.getCurrentDelay(), this);
        timer.start();

        if (mode == GameMode.TIME_ATTACK) {
            gameTimer = new Timer(1000, e -> {
                timeRemaining--;
                if (timeRemaining <= 0) {
                    running = false;
                    timer.stop();
                    gameTimer.stop();
                    if (score > highScore) highScore = score;
                    gameState = GameState.GAME_OVER;
                }
            });
            gameTimer.start();
        }

        gameState = GameState.PLAYING;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (gameState) {
            case MENU -> drawMenu(g2d);
            case PLAYING, PAUSED -> {
                drawGrid(g2d);
                drawObstacles(g2d);
                drawFood(g2d);
                drawSnake(g2d);
                drawParticles(g2d);
                drawHUD(g2d);
                if (gameState == GameState.PAUSED) drawPauseOverlay(g2d);
            }
            case GAME_OVER -> drawGameOver(g2d);
        }
    }

    private void drawMenu(Graphics2D g2d) {
        // 背景渐变
        GradientPaint gp = new GradientPaint(0, 0, new Color(0, 50, 0),
                PANEL_SIZE, PANEL_SIZE, new Color(0, 0, 50));
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, PANEL_SIZE, PANEL_SIZE);

        // 标题
        g2d.setColor(new Color(0, 255, 0));
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 48));
        FontMetrics fm = g2d.getFontMetrics();
        String title = "贪吃蛇";
        g2d.drawString(title, (PANEL_SIZE - fm.stringWidth(title)) / 2, 150);

        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        fm = g2d.getFontMetrics();
        g2d.drawString("Snake Game", (PANEL_SIZE - fm.stringWidth("Snake Game")) / 2, 190);

        // 模式选择
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 24));
        fm = g2d.getFontMetrics();
        int startY = 280;
        for (int i = 0; i < MODES.length; i++) {
            int y = startY + i * 70;
            boolean selected = i == menuSelection;

            // 选中框
            if (selected) {
                g2d.setColor(new Color(0, 255, 0, 50));
                g2d.fillRoundRect(100, y - 30, PANEL_SIZE - 200, 60, 15, 15);
                g2d.setColor(Color.GREEN);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(100, y - 30, PANEL_SIZE - 200, 60, 15, 15);
            }

            g2d.setColor(selected ? Color.WHITE : Color.GRAY);
            String modeName = MODES[i].getName();
            g2d.drawString(modeName, (PANEL_SIZE - fm.stringWidth(modeName)) / 2, y);

            g2d.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            fm = g2d.getFontMetrics();
            g2d.setColor(selected ? Color.LIGHT_GRAY : Color.DARK_GRAY);
            String desc = MODES[i].getDescription();
            g2d.drawString(desc, (PANEL_SIZE - fm.stringWidth(desc)) / 2, y + 25);

            g2d.setFont(new Font("微软雅黑", Font.BOLD, 24));
            fm = g2d.getFontMetrics();
        }

        // 提示
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        fm = g2d.getFontMetrics();
        g2d.drawString("↑↓ 选择模式  |  ENTER 开始游戏", (PANEL_SIZE - fm.stringWidth("↑↓ 选择模式  |  ENTER 开始游戏")) / 2, PANEL_SIZE - 80);
        g2d.drawString("H 查看帮助", (PANEL_SIZE - fm.stringWidth("H 查看帮助")) / 2, PANEL_SIZE - 50);
    }

    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(new Color(20, 20, 20));
        for (int i = 0; i <= GRID_COUNT; i++) {
            g2d.drawLine(i * GRID_SIZE, 0, i * GRID_SIZE, PANEL_SIZE);
            g2d.drawLine(0, i * GRID_SIZE, PANEL_SIZE, i * GRID_SIZE);
        }
    }

    private void drawObstacles(Graphics2D g2d) {
        for (Point p : obstacleManager.getObstacles()) {
            g2d.setColor(new Color(139, 69, 19));
            g2d.fillRect(p.x * GRID_SIZE + 1, p.y * GRID_SIZE + 1, GRID_SIZE - 2, GRID_SIZE - 2);
            g2d.setColor(new Color(160, 82, 45));
            g2d.fillRect(p.x * GRID_SIZE + 3, p.y * GRID_SIZE + 3, GRID_SIZE - 6, GRID_SIZE - 6);
        }
    }

    private void drawFood(Graphics2D g2d) {
        Point p = food.getPosition();
        FoodType type = food.getType();
        int x = p.x * GRID_SIZE;
        int y = p.y * GRID_SIZE;

        // 发光效果
        float remaining = (float) food.getRemainingRatio();
        int glowSize = (int)(GRID_SIZE * 0.8 * remaining);
        g2d.setColor(new Color(type.getPrimaryColor().getRed(),
                type.getPrimaryColor().getGreen(),
                type.getPrimaryColor().getBlue(), 50));
        g2d.fillOval(x + GRID_SIZE/2 - glowSize/2, y + GRID_SIZE/2 - glowSize/2, glowSize, glowSize);

        g2d.setColor(type.getPrimaryColor());
        g2d.fillOval(x + 2, y + 2, GRID_SIZE - 4, GRID_SIZE - 4);
        g2d.setColor(type.getSecondaryColor());
        g2d.fillOval(x + 5, y + 5, GRID_SIZE - 10, GRID_SIZE - 10);

        // 食物类型指示
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 10));
        String symbol = switch (type) {
            case NORMAL -> "●";
            case SPEED_BOOST -> "⚡";
            case SLOW_DOWN -> "❄";
            case DOUBLE_SCORE -> "×2";
            case GHOST -> "👻";
            case SHRINK -> "↓";
        };
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(symbol, x + (GRID_SIZE - fm.stringWidth(symbol))/2, y + GRID_SIZE/2 + 4);
    }

    private void drawSnake(Graphics2D g2d) {
        var body = snake.getBody();
        for (int i = 0; i < body.size(); i++) {
            Point p = body.get(i);
            float ratio = 1.0f - (float) i / body.size() * 0.6f;

            if (snake.isGhostMode()) {
                g2d.setColor(new Color(200, 200, 255, (int)(150 * ratio)));
            } else if (snake.isSpeedBoosted()) {
                g2d.setColor(new Color(255, (int)(200 * ratio), 0));
            } else if (snake.isSlowedDown()) {
                g2d.setColor(new Color(0, (int)(100 * ratio), 255));
            } else {
                g2d.setColor(new Color(0, (int)(200 * ratio), 0));
            }

            g2d.fillRoundRect(p.x * GRID_SIZE + 1, p.y * GRID_SIZE + 1,
                    GRID_SIZE - 2, GRID_SIZE - 2, 10, 10);

            // 蛇身高光
            if (i == 0) {
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.fillRoundRect(p.x * GRID_SIZE + 3, p.y * GRID_SIZE + 3,
                        GRID_SIZE/2, GRID_SIZE/2, 5, 5);
            }
        }

        // 蛇眼
        drawSnakeEyes(g2d);
    }

    private void drawSnakeEyes(Graphics2D g2d) {
        Point head = snake.getHead();
        g2d.setColor(Color.WHITE);
        int eyeSize = 5;
        int pupilSize = 3;

        switch (snake.getDirection()) {
            case UP -> {
                g2d.fillOval(head.x * GRID_SIZE + 4, head.y * GRID_SIZE + 4, eyeSize, eyeSize);
                g2d.fillOval(head.x * GRID_SIZE + 12, head.y * GRID_SIZE + 4, eyeSize, eyeSize);
                g2d.setColor(Color.BLACK);
                g2d.fillOval(head.x * GRID_SIZE + 5, head.y * GRID_SIZE + 5, pupilSize, pupilSize);
                g2d.fillOval(head.x * GRID_SIZE + 13, head.y * GRID_SIZE + 5, pupilSize, pupilSize);
            }
            case DOWN -> {
                g2d.setColor(Color.WHITE);
                g2d.fillOval(head.x * GRID_SIZE + 4, head.y * GRID_SIZE + 12, eyeSize, eyeSize);
                g2d.fillOval(head.x * GRID_SIZE + 12, head.y * GRID_SIZE + 12, eyeSize, eyeSize);
                g2d.setColor(Color.BLACK);
                g2d.fillOval(head.x * GRID_SIZE + 5, head.y * GRID_SIZE + 13, pupilSize, pupilSize);
                g2d.fillOval(head.x * GRID_SIZE + 13, head.y * GRID_SIZE + 13, pupilSize, pupilSize);
            }
            case LEFT -> {
                g2d.setColor(Color.WHITE);
                g2d.fillOval(head.x * GRID_SIZE + 2, head.y * GRID_SIZE + 4, eyeSize, eyeSize);
                g2d.fillOval(head.x * GRID_SIZE + 2, head.y * GRID_SIZE + 12, eyeSize, eyeSize);
                g2d.setColor(Color.BLACK);
                g2d.fillOval(head.x * GRID_SIZE + 2, head.y * GRID_SIZE + 5, pupilSize, pupilSize);
                g2d.fillOval(head.x * GRID_SIZE + 2, head.y * GRID_SIZE + 13, pupilSize, pupilSize);
            }
            case RIGHT -> {
                g2d.setColor(Color.WHITE);
                g2d.fillOval(head.x * GRID_SIZE + 14, head.y * GRID_SIZE + 4, eyeSize, eyeSize);
                g2d.fillOval(head.x * GRID_SIZE + 14, head.y * GRID_SIZE + 12, eyeSize, eyeSize);
                g2d.setColor(Color.BLACK);
                g2d.fillOval(head.x * GRID_SIZE + 15, head.y * GRID_SIZE + 5, pupilSize, pupilSize);
                g2d.fillOval(head.x * GRID_SIZE + 15, head.y * GRID_SIZE + 13, pupilSize, pupilSize);
            }
        }
    }

    private void drawParticles(Graphics2D g2d) {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.update();
            p.draw(g2d);
            if (p.isDead()) it.remove();
        }
    }

    private void spawnParticles(int x, int y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            particles.add(new Particle(x, y, color));
        }
    }

    private void drawHUD(Graphics2D g2d) {
        // 半透明背景条
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, PANEL_SIZE, 45);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 16));

        // 得分
        g2d.drawString("得分: " + score, 10, 20);

        // 最高分
        g2d.drawString("最高: " + highScore, 10, 38);

        // 模式
        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        g2d.drawString(gameMode.getName(), PANEL_SIZE - 100, 20);

        // 等级/长度
        g2d.drawString("Lv." + level + " | 长度:" + snake.getLength(), PANEL_SIZE - 100, 38);

        // 限时模式倒计时
        if (gameMode == GameMode.TIME_ATTACK) {
            g2d.setColor(timeRemaining <= 10 ? Color.RED : Color.YELLOW);
            g2d.setFont(new Font("微软雅黑", Font.BOLD, 20));
            String timeStr = "⏱ " + timeRemaining + "s";
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(timeStr, (PANEL_SIZE - fm.stringWidth(timeStr)) / 2, 30);
        }

        // 状态效果指示
        int effectX = 200;
        if (snake.isGhostMode()) {
            g2d.setColor(new Color(200, 200, 255));
            g2d.drawString("👻 穿墙", effectX, 38);
            effectX += 70;
        }
        if (snake.isSpeedBoosted()) {
            g2d.setColor(Color.YELLOW);
            g2d.drawString("⚡ 加速", effectX, 38);
            effectX += 70;
        }
        if (snake.isSlowedDown()) {
            g2d.setColor(Color.CYAN);
            g2d.drawString("❄ 减速", effectX, 38);
            effectX += 70;
        }
        if (snake.isDoubleScore()) {
            g2d.setColor(Color.MAGENTA);
            g2d.drawString("×2 双倍", effectX, 38);
        }
    }

    private void drawPauseOverlay(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, PANEL_SIZE, PANEL_SIZE);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 48));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "已暂停";
        g2d.drawString(text, (PANEL_SIZE - fm.stringWidth(text)) / 2, PANEL_SIZE / 2 - 30);

        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        fm = g2d.getFontMetrics();
        g2d.drawString("按 P 或 空格 继续", (PANEL_SIZE - fm.stringWidth("按 P 或 空格 继续")) / 2, PANEL_SIZE / 2 + 20);
    }

    private void drawGameOver(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, PANEL_SIZE, PANEL_SIZE);

        g2d.setColor(Color.RED);
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 48));
        FontMetrics fm = g2d.getFontMetrics();
        String text = gameMode == GameMode.TIME_ATTACK ? "时间到!" : "游戏结束";
        g2d.drawString(text, (PANEL_SIZE - fm.stringWidth(text)) / 2, PANEL_SIZE / 2 - 80);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 36));
        fm = g2d.getFontMetrics();
        text = "得分: " + score;
        g2d.drawString(text, (PANEL_SIZE - fm.stringWidth(text)) / 2, PANEL_SIZE / 2 - 20);

        if (score >= highScore && score > 0) {
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("微软雅黑", Font.BOLD, 24));
            fm = g2d.getFontMetrics();
            g2d.drawString("新纪录!", (PANEL_SIZE - fm.stringWidth("新纪录!")) / 2, PANEL_SIZE / 2 + 20);
        }

        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        fm = g2d.getFontMetrics();
        g2d.drawString("R 重新开始 | M 返回菜单 | Q 退出",
                (PANEL_SIZE - fm.stringWidth("R 重新开始 | M 返回菜单 | Q 退出")) / 2, PANEL_SIZE / 2 + 70);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running && gameState == GameState.PLAYING) {
            snake.move();

            // 无尽模式穿墙
            if (gameMode == GameMode.NO_WALL) {
                snake.wrapAround(GRID_COUNT, GRID_COUNT);
            }

            checkCollision();
            timer.setDelay(snake.getCurrentDelay());
        }
        repaint();
    }

    private void checkCollision() {
        Point head = snake.getHead();

        // 检查撞墙
        if (snake.collidesWithWall(GRID_COUNT, GRID_COUNT)) {
            gameOver();
            return;
        }

        // 检查撞自己
        if (snake.collidesWithSelf()) {
            gameOver();
            return;
        }

        // 检查撞障碍物
        if (snake.collidesWithObstacle(obstacleManager.getObstacles())) {
            gameOver();
            return;
        }

        // 检查吃食物
        if (head.equals(food.getPosition())) {
            eatFood();
        }

        // 食物过期
        if (food.isExpired()) {
            food.spawn(GRID_COUNT, GRID_COUNT, snake, obstacleManager.getObstacles());
        }
    }

    private void eatFood() {
        FoodType type = food.getType();
        int points = type.getScore();
        if (snake.isDoubleScore()) points *= 2;
        score += points;

        // 粒子效果
        Point p = food.getPosition();
        spawnParticles(p.x * GRID_SIZE + GRID_SIZE/2, p.y * GRID_SIZE + GRID_SIZE/2,
                type.getPrimaryColor(), 15);

        // 应用食物效果
        switch (type) {
            case NORMAL -> snake.grow();
            case SPEED_BOOST -> {
                snake.grow();
                snake.activateSpeedBoost(5000);
            }
            case SLOW_DOWN -> {
                snake.grow();
                snake.activateSlowDown(5000);
            }
            case DOUBLE_SCORE -> {
                snake.grow();
                snake.activateDoubleScore(8000);
            }
            case GHOST -> {
                snake.grow();
                snake.activateGhost(6000);
            }
            case SHRINK -> snake.shrink();
        }

        // 更新等级
        updateLevel();

        // 生成新食物
        food.spawn(GRID_COUNT, GRID_COUNT, snake, obstacleManager.getObstacles());
    }

    private void updateLevel() {
        int newLevel = score / 100 + 1;
        if (newLevel > level) {
            level = newLevel;
            snake.setBaseSpeed(Math.max(50, 120 - (level - 1) * 8));

            if (gameMode == GameMode.CHALLENGE) {
                obstacleManager.addLevelObstacles(level, snake);
            }
        }
    }

    private void gameOver() {
        running = false;
        timer.stop();
        if (gameTimer != null) gameTimer.stop();
        if (score > highScore) highScore = score;
        gameState = GameState.GAME_OVER;

        // 死亡粒子效果
        Point head = snake.getHead();
        spawnParticles(head.x * GRID_SIZE + GRID_SIZE/2, head.y * GRID_SIZE + GRID_SIZE/2,
                Color.RED, 30);
    }

    private void restart() {
        timer.stop();
        if (gameTimer != null) gameTimer.stop();
        initGame(gameMode);
        requestFocusInWindow();
    }

    private void backToMenu() {
        timer.stop();
        if (gameTimer != null) gameTimer.stop();
        gameState = GameState.MENU;
        repaint();
    }

    private class KeyHandler extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            switch (gameState) {
                case MENU -> handleMenuInput(key);
                case PLAYING -> handleGameInput(key);
                case PAUSED -> handlePausedInput(key);
                case GAME_OVER -> handleGameOverInput(key);
            }
        }

        private void handleMenuInput(int key) {
            switch (key) {
                case KeyEvent.VK_UP -> menuSelection = (menuSelection - 1 + MODES.length) % MODES.length;
                case KeyEvent.VK_DOWN -> menuSelection = (menuSelection + 1) % MODES.length;
                case KeyEvent.VK_ENTER -> initGame(MODES[menuSelection]);
                case KeyEvent.VK_H -> showHelp();
                case KeyEvent.VK_Q -> System.exit(0);
            }
            repaint();
        }

        private void handleGameInput(int key) {
            switch (key) {
                case KeyEvent.VK_UP, KeyEvent.VK_W -> snake.setDirection(Direction.UP);
                case KeyEvent.VK_DOWN, KeyEvent.VK_S -> snake.setDirection(Direction.DOWN);
                case KeyEvent.VK_LEFT, KeyEvent.VK_A -> snake.setDirection(Direction.LEFT);
                case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> snake.setDirection(Direction.RIGHT);
                case KeyEvent.VK_P, KeyEvent.VK_SPACE -> {
                    gameState = GameState.PAUSED;
                    paused = true;
                }
                case KeyEvent.VK_ESCAPE -> backToMenu();
            }
        }

        private void handlePausedInput(int key) {
            if (key == KeyEvent.VK_P || key == KeyEvent.VK_SPACE) {
                gameState = GameState.PLAYING;
                paused = false;
            } else if (key == KeyEvent.VK_ESCAPE) {
                backToMenu();
            }
        }

        private void handleGameOverInput(int key) {
            switch (key) {
                case KeyEvent.VK_R -> restart();
                case KeyEvent.VK_M -> backToMenu();
                case KeyEvent.VK_Q -> System.exit(0);
            }
        }

        private void showHelp() {
            String help = """
                ===== 游戏帮助 =====

                操作:
                  方向键/WASD - 控制方向
                  P/空格 - 暂停
                  ESC - 返回菜单
                  R - 重新开始
                  Q - 退出

                游戏模式:
                  经典模式 - 传统玩法，碰壁即死
                  无尽模式 - 穿墙而过
                  挑战模式 - 躲避障碍物
                  限时模式 - 60秒争高分

                食物类型:
                  红色 ● - 普通 (+10分)
                  黄色 ⚡ - 加速 (+15分)
                  蓝色 ❄ - 减速 (+15分)
                  紫色 ×2 - 双倍分数 (+20分)
                  白色 👻 - 穿墙模式 (+25分)
                  绿色 ↓ - 缩短身体 (+30分)
                """;
            JOptionPane.showMessageDialog(GamePanel.this, help, "帮助", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
