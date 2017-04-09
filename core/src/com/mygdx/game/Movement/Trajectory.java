package com.mygdx.game.Movement;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.game.Main.GamePrototype;
import com.mygdx.game.Main.GameScreen;
import com.mygdx.game.Resources.Constants;

public class Trajectory {

    private GameScreen gameScreen;
    private BallController controller;
    private Sprite lineSprite;

    public Trajectory(BallController controller, GamePrototype game, GameScreen gameScreen) {
        this.controller = controller;
        this.gameScreen = gameScreen;
        lineSprite = new Sprite(game.getAssetManager().get(
                game.getAssets().get(Texture.class).get("trajectory"), Texture.class));
        lineSprite.setSize(Constants.TRAJECTORY_POINTS_SIZE, Constants.TRAJECTORY_POINTS_SIZE);
    }

    public void draw(Batch batch) {
        if (controller.isDrawTrajectory()) {
            for (int i = 0; i < 60 * Constants.TRAJECTORY_PROJECTION_TIME; i += 2) {
                lineSprite.setPosition(getX(i * Constants.PHYSICS_STEP_TIME), getY(i * Constants.PHYSICS_STEP_TIME));
                lineSprite.draw(batch);
            }
        }
    }

    private float getX(float timeStep) {
        return gameScreen.getBall().getCenterX() + timeStep * controller.getImpulseX() / gameScreen.getBall().getBody().getMass();
    }

    private float getY(float timeStep) {
        return gameScreen.getBall().getCenterY() + timeStep * controller.getImpulseY() / gameScreen.getBall().getBody().getMass()
                + (timeStep * timeStep) / 2 * gameScreen.getWorld().getGravity().y;
    }

}
