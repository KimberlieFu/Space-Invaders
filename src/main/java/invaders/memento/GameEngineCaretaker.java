package invaders.memento;

import invaders.engine.GameEngine;

/**
 * Caretaker/Manager of the GameEngine memento
 */
public class GameEngineCaretaker {
    private GameEngineMemento gameEngineHistory = null;

    /**
     * Save a memento from game engine
     */
    public void saveGameEngine(GameEngine game) {

        // Single memento
        GameEngineMemento memento = game.save();
        gameEngineHistory = memento;
    }

    /**
     * Restore game engine state from memento
     */
    public void revertGameEngine(GameEngine game) {
        if (gameEngineHistory == null) {
            System.out.println("No previous state to revert to.");
        } else {
            game.restore(gameEngineHistory);
//            gameEngineHistory = null;
        }
    }
}
