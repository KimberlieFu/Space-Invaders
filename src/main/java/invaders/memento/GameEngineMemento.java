package invaders.memento;

import invaders.entities.Player;
import invaders.factory.EnemyProjectile;
import invaders.factory.PlayerProjectile;
import invaders.factory.Projectile;
import invaders.gameobject.Bunker;
import invaders.gameobject.Enemy;

import java.util.*;

/**
 * Game engine memento that contains the specific contents
 */
public class GameEngineMemento {
    private int score;
    private int timer;
    private List<Enemy> enemies;
    private List<Bunker> bunkers;
    private Player player;
    private Projectile shot;

    /**
     * GameEngineMemento constructor:
     *
     * pass in all relevant details of game engine to save
     */
    public GameEngineMemento(int score, int timer, List<Enemy> enemies, List<Bunker> bunkers, Projectile shot, Player player) {
        this.score = score;
        this.timer = timer;
        this.enemies = enemies;
        this.bunkers = bunkers;
        this.shot = shot;
        this.player = player;
    }

    /**
     * get memento player previous shot
     */
    public Projectile getShot() {
        return shot;
    }

    /**
     * get memento game points
     */
    public int getScore() {
        return score;
    }

    /**
     * get memento game timer
     */
    public int getTimer() {
        return timer;
    }

    /**
     * get memento enemy list
     */
    public List<Enemy> getEnemies() {
        return enemies;
    }

    /**
     * get memento player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * get memento bunker list
     */
    public List<Bunker> getBunkers() {
        return bunkers;
    }

}
