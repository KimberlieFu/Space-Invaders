package invaders.engine;

import java.util.List;
import java.util.ArrayList;

import invaders.entities.EntityViewImpl;
import invaders.entities.SpaceBackground;
import invaders.observer.*;
import invaders.singleton.Level;
import invaders.singleton.Singleton;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import invaders.entities.EntityView;
import invaders.rendering.Renderable;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

/**
 * Displays the game engine to GUI
 */
public class GameWindow implements Observer {
	private final int width;
    private final int height;
	private Scene scene;
    private Pane pane;
    private GameEngine model;
    private List<EntityView> entityViews =  new ArrayList<EntityView>();
    private Renderable background;

    private double xViewportOffset = 0.0;
    private double yViewportOffset = 0.0;
    private int points = 0;
    private double lives = 0;
    private int timer = 0;
    private Level level;


    /**
     * Game Window Constructor:
     *
     * Pass in game engine and the enum for the level
     */
    public GameWindow(GameEngine model, Level level){
        this.model = model;
		this.width =  model.getGameWidth();
        this.height = model.getGameHeight();
        this.lives = model.getPlayer().getHealth();

        pane = new Pane();
        scene = new Scene(pane, width, height);
        this.background = new SpaceBackground(model, pane);

        KeyboardInputHandler keyboardInputHandler = new KeyboardInputHandler(this.model);

        scene.setOnKeyPressed(keyboardInputHandler::handlePressed);
        scene.setOnKeyReleased(keyboardInputHandler::handleReleased);
        this.level = level;

        buttonSetting();

        // Observer design pattern
        model.attach(this);

    }


    /**
     * Updates and displays time frame
     */
	public void run() {
         Timeline timeline = new Timeline(new KeyFrame(Duration.millis(17), t -> this.draw()));

         timeline.setCycleCount(Timeline.INDEFINITE);
         timeline.play();
    }

    /**
     * Updates and displays main loop
     */
    private void draw(){
        model.update();
        pane.getChildren().removeIf(node -> node instanceof Label);

        List<Renderable> renderables = model.getRenderables();
        for (Renderable entity : renderables) {
            boolean notFound = true;
            for (EntityView view : entityViews) {
                if (view.matchesEntity(entity)) {
                    notFound = false;
                    view.update(xViewportOffset, yViewportOffset);
                    break;
                }
            }
            if (notFound) {
                EntityView entityView = new EntityViewImpl(entity);
                entityViews.add(entityView);
                pane.getChildren().add(entityView.getNode());
            }
        }

        for (Renderable entity : renderables){
            if (!entity.isAlive()){
                for (EntityView entityView : entityViews){
                    if (entityView.matchesEntity(entity)){
                        entityView.markForDelete();
                    }
                }
            }
        }

        for (EntityView entityView : entityViews) {
            if (entityView.isMarkedForDelete()) {
                pane.getChildren().remove(entityView.getNode());
            }
        }


        model.getGameObjects().removeAll(model.getPendingToRemoveGameObject());
        model.getGameObjects().addAll(model.getPendingToAddGameObject());
        model.getRenderables().removeAll(model.getPendingToRemoveRenderable());
        model.getRenderables().addAll(model.getPendingToAddRenderable());

        model.getPendingToAddGameObject().clear();
        model.getPendingToRemoveGameObject().clear();
        model.getPendingToAddRenderable().clear();
        model.getPendingToRemoveRenderable().clear();

        entityViews.removeIf(EntityView::isMarkedForDelete);


        displayText("LIVES: " + (int) lives, 20, 20, 20, Color.WHITE);
        displayText("SCORE: " + points, 20, 50, 20, Color.WHITE);
        displayText("TIME: " + formatTime(timer), 20, 80, 20, Color.WHITE);
        displayText("LEVEL: " + level.getLevel(), 450, 20, 20, level.getColor());
    }

    /**
     * Return Game window scene
     * @return Game window
     */
	public Scene getScene() {
        return scene;
    }

    /**
     * Format game text
     */
    public void displayText(String text, double x, double y, double size, Color color) {
        Label label = new Label(text);
        label.setFont(Font.font("Georgia", size));
        label.setTextFill(color);
        label.setLayoutX(x);
        label.setLayoutY(y);
        pane.getChildren().add(label);
    }

    /**
     * Format game time
     */
    private String formatTime(int frameCount) {
        int seconds = frameCount / 120;
        int minutes = seconds / 60;
        seconds %= 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Button logic
     */
    private void buttonSetting() {
        Button levelButton = new Button("Change difficulty");
        levelButton.setPrefWidth(120);
        levelButton.setPrefHeight(20);
        levelButton.setLayoutX(450);
        levelButton.setLayoutY(50);
        levelButton.setFocusTraversable(false);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem easyItem = new MenuItem("Easy");
        MenuItem mediumItem = new MenuItem("Medium");
        MenuItem hardItem = new MenuItem("Hard");

        levelButton.setOnAction(event -> {
            contextMenu.show(levelButton, Side.BOTTOM, 0, 0);
        });

        contextMenu.getItems().addAll(easyItem, mediumItem, hardItem);
        easyItem.setOnAction(event -> {
            level = Level.EASY;
            changeDifficulty();
        });

        mediumItem.setOnAction(event -> {
            level = Level.MEDIUM;
            changeDifficulty();
        });

        hardItem.setOnAction(event -> {
            level = Level.HARD;
            changeDifficulty();
        });

        levelButton.setContextMenu(contextMenu);
        pane.getChildren().add(levelButton);
    }

    /**
     * Change difficulty
     */
    private void changeDifficulty() {
        GameEngine newGameEngine = null;

        switch (level) {
            case EASY:
                newGameEngine = new GameEngine(Singleton.getInstance().getEasyLevel());
                break;
            case MEDIUM:
                newGameEngine = new GameEngine(Singleton.getInstance().getMediumLevel());
                break;
            case HARD:
                newGameEngine = new GameEngine(Singleton.getInstance().getHardLevel());
                break;
        }

        // Clear Previous Game
        model.clearGameObject();
        draw();
        model.detach(this);

        // Make New Game
        model = newGameEngine;
        model.attach(this);
        KeyboardInputHandler keyboardInputHandler = new KeyboardInputHandler(this.model);
        scene.setOnKeyPressed(keyboardInputHandler::handlePressed);
        scene.setOnKeyReleased(keyboardInputHandler::handleReleased);
    }


    /**
     * Receive updates from the game
     */
    // Observer design pattern
    @Override
    public void update() {
        this.points = model.getPoints();
        this.lives = model.getLives();
        this.timer = model.getTimer();
    }
}
