package invaders.observer;

/**
 * Score enum that stores the game points
 */
public enum Score {
    SlowProjectile(1),
    FastProjectile(2),
    SlowAlien(3),
    FastAlien(4);

    private final int score;

    /**
     * Score enum constructor:
     * instantiate enum
     */
    Score (int score) {
        this.score = score;
    }

    /**
     * get score enum amount
     */
    public int getScore() {
        return score;
    }
}
