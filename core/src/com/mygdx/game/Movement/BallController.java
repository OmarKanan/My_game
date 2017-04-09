package com.mygdx.game.Movement;

import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.Main.GameScreen;
import com.mygdx.game.Resources.Constants;

public class BallController implements GestureDetector.GestureListener {

    private GameScreen gameScreen;
    private Vector3 touchCoords;
    private Vector3 releaseCoords;
    private Vector3 impulse;
    private boolean ballHit = false;
    private boolean drawTrajectory = false;

    public boolean isDrawTrajectory() {
        return drawTrajectory;
    }

    public BallController(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        touchCoords = new Vector3();
        releaseCoords = new Vector3();
        impulse = new Vector3();
    }

    public float getImpulseX() {
        return impulse.x;
    }

    public float getImpulseY() {
        return impulse.y;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if (!ballHit) {
            touchCoords.set(x, y, 0);
            gameScreen.getCamera().unproject(touchCoords);

            if (Math.abs(touchCoords.x - gameScreen.getBall().getCenterX())
                    < gameScreen.getBall().getRadius() * Constants.BALL_DETECTION_RATIO
                    && Math.abs(touchCoords.y - gameScreen.getBall().getCenterY())
                    < gameScreen.getBall().getRadius() * Constants.BALL_DETECTION_RATIO
                    && !gameScreen.getBall().getBody().isAwake() && !gameScreen.isCameraTranslating()) {

                ballHit = true;
            }
        }
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        ballHit = false;
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (ballHit) {
            releaseCoords.set(x, y, 0);
            gameScreen.getCamera().unproject(releaseCoords);

            impulse.set((gameScreen.getBall().getCenterX() - releaseCoords.x) * Constants.BALL_LAUNCH_COEFF,
                    (gameScreen.getBall().getCenterY() - releaseCoords.y) * Constants.BALL_LAUNCH_COEFF, 0);
            float length = impulse.len();
            impulse.set(impulse.x * Math.min(1, Constants.BALL_MAX_LAUNCH / length),
                    impulse.y * Math.min(1, Constants.BALL_MAX_LAUNCH / length), 0);

            drawTrajectory = true;
        }
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        if (ballHit) {
            gameScreen.getBall().getBody().applyLinearImpulse(impulse.x, impulse.y,
                    gameScreen.getBall().getBody().getWorldCenter().x,
                    gameScreen.getBall().getBody().getWorldCenter().y, true);

            drawTrajectory = false;
        }
        ballHit = false;
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1,
                         Vector2 pointer2) {
        return false;
    }
}
