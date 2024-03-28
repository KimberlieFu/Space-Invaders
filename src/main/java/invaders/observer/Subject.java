package invaders.observer;

// Game Engine is the subject
/**
 * Subject interface:
 */
public interface Subject {
    /**
     * get game points
     */
    int getPoints();

    /**
     * get player lives
     */
    double getLives();

    /**
     * get game timer
     */
    int getTimer();

    /**
     * attach observer
     */
    void attach(Observer observer);

    /**
     * detach observer
     */
    void detach(Observer observer);

    /**
     * notify all observers
     */
    void notifyObservers();
}
