package invaders.engine;

import java.util.ArrayList;
import java.util.List;

import invaders.ConfigReader;
import invaders.builder.BunkerBuilder;
import invaders.builder.Director;
import invaders.builder.EnemyBuilder;
import invaders.factory.*;
import invaders.factory.Projectile;
import invaders.gameobject.Bunker;
import invaders.gameobject.Enemy;
import invaders.gameobject.GameObject;
import invaders.entities.Player;
import invaders.memento.*;
import invaders.observer.*;
import invaders.rendering.Renderable;
import invaders.singleton.Singleton;
import invaders.state.RedState;
import invaders.state.YellowState;
import invaders.strategy.*;
import org.json.simple.JSONObject;

/**
 * This class manages the main loop and logic of the game.
 * It controls the game's state, including player actions, game objects, and scoring.
 */
public class GameEngine implements Subject {
	private List<GameObject> gameObjects = new ArrayList<>(); // A list of game objects that gets updated each frame
	private List<GameObject> pendingToAddGameObject = new ArrayList<>();
	private List<GameObject> pendingToRemoveGameObject = new ArrayList<>();

	private List<Renderable> pendingToAddRenderable = new ArrayList<>();
	private List<Renderable> pendingToRemoveRenderable = new ArrayList<>();

	private List<Renderable> renderables =  new ArrayList<>();

	private Player player;

	private boolean left;
	private boolean right;
	private int gameWidth;
	private int gameHeight;
	private int timer = 45;

	// Observers design pattern
	private int gameTimer = 0;
	private int points = 0;
	private List<Observer> observers = new ArrayList<>();

	// Memento design pattern
	private GameEngineCaretaker gameEngineCaretaker = new GameEngineCaretaker();
	private Projectile prevShot = null;



	/**
	 * Constructor for GameEngine:
	 *
	 * Pass Json file contains the map layout of the game.
	 */
	public GameEngine(String config){
		// Read the config here
		ConfigReader.parse(config);


		// Get game width and height
		gameWidth = ((Long)((JSONObject) ConfigReader.getGameInfo().get("size")).get("x")).intValue();
		gameHeight = ((Long)((JSONObject) ConfigReader.getGameInfo().get("size")).get("y")).intValue();

		//Get player info
		this.player = new Player(ConfigReader.getPlayerInfo());
		renderables.add(player);


		Director director = new Director();
		BunkerBuilder bunkerBuilder = new BunkerBuilder();
		//Get Bunkers info
		for(Object eachBunkerInfo:ConfigReader.getBunkersInfo()){
			Bunker bunker = director.constructBunker(bunkerBuilder, (JSONObject) eachBunkerInfo);
			gameObjects.add(bunker);
			renderables.add(bunker);
		}


		EnemyBuilder enemyBuilder = new EnemyBuilder();
		//Get Enemy info
		for(Object eachEnemyInfo:ConfigReader.getEnemiesInfo()){
			Enemy enemy = director.constructEnemy(this,enemyBuilder,(JSONObject)eachEnemyInfo);
			gameObjects.add(enemy);
			renderables.add(enemy);
		}

	}

	/**
	 * Updates the game/simulation (60fps).
	 *
	 * It notifies all observers in observer list every call.
	 */
	public void update(){
		timer+=1;
//		System.out.println("Renderables: " + renderables.size());
//		System.out.println("GameObjects: " + gameObjects.size());
//		System.out.println("Points: " + points);



		movePlayer();

		if (player.isAlive()) {
			gameTimer += 1;
			for(GameObject go: gameObjects){
				go.update(this);
			}
		}


		for (int i = 0; i < renderables.size(); i++) {
			Renderable renderableA = renderables.get(i);
			for (int j = i+1; j < renderables.size(); j++) {
				Renderable renderableB = renderables.get(j);

				if((renderableA.getRenderableObjectName().equals("Enemy") && renderableB.getRenderableObjectName().equals("EnemyProjectile"))
						||(renderableA.getRenderableObjectName().equals("EnemyProjectile") && renderableB.getRenderableObjectName().equals("Enemy"))||
						(renderableA.getRenderableObjectName().equals("EnemyProjectile") && renderableB.getRenderableObjectName().equals("EnemyProjectile"))){
				}else{
					if(renderableA.isColliding(renderableB) && (renderableA.getHealth()>0 && renderableB.getHealth()>0)) {

						renderableA.takeDamage(1);
						renderableB.takeDamage(1);
						pointUpdate(renderableA, renderableB);
					}
				}
			}
		}
		notifyObservers();


		// ensure that renderable foreground objects don't go off-screen
		int offset = 1;
		for(Renderable ro: renderables){
			if(!ro.getLayer().equals(Renderable.Layer.FOREGROUND)){
				continue;
			}
			if(ro.getPosition().getX() + ro.getWidth() >= gameWidth) {
				ro.getPosition().setX((gameWidth - offset) -ro.getWidth());
			}

			if(ro.getPosition().getX() <= 0) {
				ro.getPosition().setX(offset);
			}

			if(ro.getPosition().getY() + ro.getHeight() >= gameHeight) {
				ro.getPosition().setY((gameHeight - offset) -ro.getHeight());
			}

			if(ro.getPosition().getY() <= 0) {
				ro.getPosition().setY(offset);
			}
		}

	}

	/**
	 * Get renderables.
	 */
	public List<Renderable> getRenderables(){
		return renderables;
	}

	/**
	 * Get game objects.
	 */
	public List<GameObject> getGameObjects() {
		return gameObjects;
	}

	/**
	 * Get pending to add to game objects list.
	 */
	public List<GameObject> getPendingToAddGameObject() {
		return pendingToAddGameObject;
	}

	/**
	 * Get pending to remove to game objects list.
	 */
	public List<GameObject> getPendingToRemoveGameObject() {
		return pendingToRemoveGameObject;
	}

	/**
	 * Get pending to add to renderable list.
	 */
	public List<Renderable> getPendingToAddRenderable() {
		return pendingToAddRenderable;
	}

	/**
	 * Get pending to remove to renderable list.
	 */
	public List<Renderable> getPendingToRemoveRenderable() {
		return pendingToRemoveRenderable;
	}


	/**
	 * Control player movement condition
	 */
	public void leftReleased() {
		this.left = false;
	}

	/**
	 * Control player movement condition
	 */
	public void rightReleased(){
		this.right = false;
	}

	/**
	 * Control player movement condition
	 */
	public void leftPressed() {
		this.left = true;
	}

	/**
	 * Control player movement condition
	 */
	public void rightPressed(){
		this.right = true;
	}

	/**
	 * Player shot
	 */
	public boolean shootPressed(){
		if(timer>45 && player.isAlive()){
			Projectile projectile = player.shoot();
			prevShot = projectile;
			gameObjects.add(projectile);
			renderables.add(projectile);
			timer=0;
			return true;
		}
		return false;
	}

	/**
	 * Control main player movement
	 */
	private void movePlayer(){
		if(left){
			player.left();
		}

		if(right){
			player.right();
		}
	}

	/**
	 * Get game width
	 */
	public int getGameWidth() {
		return gameWidth;
	}

	/**
	 * Get game height
	 */
	public int getGameHeight() {
		return gameHeight;
	}

	/**
	 * Get player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Clear all game objects
	 */
	public void clearGameObject() {

		for (Renderable ro : renderables) {
			if (ro.getRenderableObjectName().equals("Enemy") ||
					ro.getRenderableObjectName().equals("EnemyProjectile") ||
					ro.getRenderableObjectName().equals("PlayerProjectile")) {

				pendingToRemoveRenderable.add(ro);
				ro.takeDamage(1);
			}

			if  (ro.getRenderableObjectName().equals("Bunker")) {
				Bunker bunker = (Bunker) ro;
				pendingToRemoveRenderable.add(ro);
				bunker.setLives(0);
			}

			if (ro.getRenderableObjectName().equals("Player")) {
				Player player = (Player) ro;
				pendingToRemoveRenderable.add(ro);
				player.setHealth(0);
			}
		}
		for (GameObject go : gameObjects) {
			pendingToRemoveGameObject.add(go);
		}
	}



	// Observers design pattern
	/**
	 * Gets the number of points in the game.
	 * @return Game points
	 */
	@Override
	public int getPoints() {
		return points;
	}

	/**
	 * Gets the number of lives in the player.
	 * @return Player lives
	 */
	@Override
	public double getLives() {
		return player.getHealth();
	}

	/**
	 * Gets the game time.
	 * @return Game time
	 */
	@Override
	public int getTimer() {
		return gameTimer;
	}

	/**
	 * Adds observer to the observer list in the game.
	 */
	@Override
	public void attach(Observer observer) {
		observers.add(observer);
	}

	/**
	 * Removes observer to the observer list in the game.
	 */
	@Override
	public void detach(Observer observer) {
		observers.remove(observer);
	}

	/**
	 * Notifies all observers in the observer list of the game.
	 */
	@Override
	public void notifyObservers() {
		for (Observer observer : observers) {
			observer.update();
		}
	}


	/**
	 * Deletes all slow projectiles.
	 */
	public void deleteSlowProjectile() {
		int count = 0;
		for (Renderable ro : renderables) {
			if (ro instanceof EnemyProjectile) {
				EnemyProjectile projectile = (EnemyProjectile) ro;
				if (projectile.getStrategy() instanceof SlowProjectileStrategy) {
					pendingToRemoveRenderable.add(ro);
					ro.takeDamage(1);
					count += 1;
				}
			}
		}

		for (GameObject go : gameObjects) {
			if (go instanceof EnemyProjectile) {
				EnemyProjectile projectile = (EnemyProjectile) go;
				if (projectile.getStrategy() instanceof SlowProjectileStrategy) {
					pendingToRemoveGameObject.add(go);
				}
			}
		}

		points += (count * Score.SlowProjectile.getScore());
		notifyObservers();
	}

	/**
	 * Deletes all fast projectiles.
	 */
	public void deleteFastProjectile() {
		int count = 0;
		for (Renderable ro : renderables) {
			if (ro instanceof EnemyProjectile) {
				EnemyProjectile projectile = (EnemyProjectile) ro;
				if (projectile.getStrategy() instanceof FastProjectileStrategy) {
					pendingToRemoveRenderable.add(ro);
					ro.takeDamage(1);
					count += 1;
				}
			}
		}

		for (GameObject go : gameObjects) {
			if (go instanceof EnemyProjectile) {
				EnemyProjectile projectile = (EnemyProjectile) go;
				if (projectile.getStrategy() instanceof FastProjectileStrategy) {
					pendingToRemoveGameObject.add(go);
				}
			}
		}

		points += (count * Score.FastProjectile.getScore());
		notifyObservers();
	}

	/**
	 * Updates points based on number of enemies hit.
	 */
	public void pointUpdate(Renderable renderableA, Renderable renderableB) {

		// Enemy Projectile Collision
		if ((renderableA instanceof EnemyProjectile || renderableB instanceof EnemyProjectile) &&
				(renderableA instanceof PlayerProjectile || renderableB instanceof PlayerProjectile))  {

			EnemyProjectile enemyProjectile = null;
			if (renderableA instanceof EnemyProjectile) {
				enemyProjectile = (EnemyProjectile) renderableA;
			}

			if (renderableB instanceof EnemyProjectile) {
				enemyProjectile = (EnemyProjectile) renderableB;
			}


			int addPoints = 0;
			if (enemyProjectile.getStrategy() instanceof SlowProjectileStrategy) {
				addPoints = Score.SlowProjectile.getScore();
			} else {
				addPoints = Score.FastProjectile.getScore();
			}
			points += addPoints;
		}

		// Enemy Collision
		if ((renderableA instanceof Enemy || renderableB instanceof Enemy) &&
				(renderableA instanceof PlayerProjectile || renderableB instanceof PlayerProjectile)) {

			Enemy enemy = null;
			if (renderableA instanceof Enemy) {
				enemy = (Enemy) renderableA;
			}

			if (renderableB instanceof Enemy) {
				enemy = (Enemy) renderableB;
			}

			int addPoints = 0;
			if (enemy.getProjectileStrategy() instanceof SlowProjectileStrategy) {
				addPoints = Score.SlowAlien.getScore();
			} else {
				addPoints = Score.FastAlien.getScore();
			}
			points += addPoints;
		}
	}



	// Memento design pattern
	/**
	 * Saves the game state in a memento.
	 */
	public GameEngineMemento save() {

		List<Enemy> enemyCopies = new ArrayList<>();
		List<Bunker> bunkerCopies = new ArrayList<>();
		Player playerCopy = (Player) getPlayer().copy();

		for (Renderable ro : renderables) {
			if (ro instanceof Enemy) {
				Enemy enemy = (Enemy) ((Enemy) ro).copy();
				enemyCopies.add(enemy);

			}

			if (ro instanceof Bunker) {
				Bunker bunker = (Bunker) ((Bunker) ro).copy();
				bunkerCopies.add(bunker);
			}
		}


		GameEngineMemento memento = new GameEngineMemento(points, gameTimer, enemyCopies, bunkerCopies, prevShot, playerCopy);
		return memento;
	}

	/**
	 * Reverts the game state in the memento.
	 */
	public void restore(GameEngineMemento memento) {

		for (Renderable ro : renderables) {
			if (ro.getRenderableObjectName().equals("Enemy") ||
					ro.getRenderableObjectName().equals("EnemyProjectile") ||
					ro.equals(memento.getShot())) {

				pendingToRemoveRenderable.add(ro);
				ro.takeDamage(1);
			}

			if  (ro.getRenderableObjectName().equals("Bunker")) {
				Bunker bunker = (Bunker) ro;
				pendingToRemoveRenderable.add(ro);
				bunker.setLives(0);
			}

			if (ro.getRenderableObjectName().equals("Player")) {
				Player player = (Player) ro;
				pendingToRemoveRenderable.add(ro);
				player.setHealth(0);
			}

		}
		for (GameObject go : gameObjects) {
			if (go instanceof Enemy ||
					go instanceof EnemyProjectile ||
					go instanceof Bunker ||
					go.equals(memento.getShot()) ||
					go instanceof Player) {
				pendingToRemoveGameObject.add(go);
			}
		}


		for (Enemy enemy : memento.getEnemies()) {
			Enemy enemyCopy = (Enemy) enemy.copy();

			for (Projectile projectile : enemyCopy.getEnemyProjectile()) {
				renderables.add(projectile);
				gameObjects.add(projectile);
			}
			renderables.add(enemyCopy);
			gameObjects.add(enemyCopy);
		}

		for (Bunker bunker : memento.getBunkers()) {
			Bunker bunkerCopy = (Bunker) bunker.copy();

			renderables.add(bunkerCopy);
			gameObjects.add(bunkerCopy);
		}

		points = memento.getScore();
		gameTimer = memento.getTimer();
		player = (Player) memento.getPlayer().copy();
		renderables.add(player);

		notifyObservers();
	}

	/**
	 * Returns the game care taker.
	 * @return Game care taker
	 */
	public GameEngineCaretaker getGameEngineCaretaker() {
		return gameEngineCaretaker;
	}
}
