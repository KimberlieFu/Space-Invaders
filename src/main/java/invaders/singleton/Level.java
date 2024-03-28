package invaders.singleton;

import javafx.scene.paint.Color;

/**
 * Level enum that stores the game level
 */
public enum Level {
    EASY("Easy", Color.GREEN),
    MEDIUM("Medium", Color.YELLOW),
    HARD("Hard", Color.RED);

    private String level;
    private Color color;

    /**
     * Level enum constructor:
     * instantiate enum
     */
    Level(String level, Color color) {
        this.level = level;
        this.color = color;
    }

    /**
     * get level String
     */
    public String getLevel() {
        return level;
    }

    /**
     * get Color class
     */
    public Color getColor() {
        return color;
    }
}
