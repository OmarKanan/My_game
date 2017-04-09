package com.mygdx.game.Entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Pool;
import com.mygdx.game.Main.GamePrototype;
import com.mygdx.game.Main.GameScreen;
import com.mygdx.game.Resources.Constants;

public class EntityGenerator {

    private GamePrototype game;
    private GameScreen gameScreen;
    private Pool<PhysicalEntity> borderPool;
    private Pool<PhysicalEntity> associatedEntitiesPool;
    private float highestBorderHeight;
    private int startSign;
    private int lastBorderNumber = 0;

    public EntityGenerator(GamePrototype game, final GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;

        borderPool = new Pool<PhysicalEntity>() {
            @Override
            protected PhysicalEntity newObject() {
                return new PhysicalEntity(gameScreen);
            }
        };
        associatedEntitiesPool = new Pool<PhysicalEntity>() {
            @Override
            protected PhysicalEntity newObject() {
                return new PhysicalEntity(gameScreen);
            }
        };
    }

    public void generateStartEntities() {
        generateBall();
        generateEdges();
        generateStartGround();
    }

    private void generateBall() {
        startSign = MathUtils.randomSign();
        gameScreen.setBall(new PhysicalEntity(gameScreen).init(
                PhysicalEntity.EntityType.Ball, getImage("ball"), gameScreen.getWorld(), startSign * gameScreen.getCamera().viewportWidth / 4,
                -gameScreen.getCamera().viewportHeight / 2 + Constants.BORDER_WIDTH + Constants.BOTTOM_START + Constants.BALL_RADIUS, 0,
                Constants.BALL_RADIUS * 2, Constants.BALL_RADIUS * 2, 0, BodyDef.BodyType.DynamicBody));
    }

    private void generateEdges() {
        gameScreen.getEdges()[1] = new PhysicalEntity(gameScreen).init(PhysicalEntity.EntityType.Border, getImage("border"), gameScreen.getWorld(),
                gameScreen.getCamera().viewportWidth / 2 - Constants.BORDER_WIDTH / 2, 0, 0, Constants.BORDER_WIDTH,
                gameScreen.getCamera().viewportHeight * 2, gameScreen.getCamera().viewportHeight * 2, BodyDef.BodyType.StaticBody);
        gameScreen.getEdges()[0] = new PhysicalEntity(gameScreen).init(PhysicalEntity.EntityType.Border, getImage("border"), gameScreen.getWorld(),
                -gameScreen.getCamera().viewportWidth / 2 + Constants.BORDER_WIDTH / 2, 0, 180, Constants.BORDER_WIDTH,
                gameScreen.getCamera().viewportHeight * 2, gameScreen.getCamera().viewportHeight * 2, BodyDef.BodyType.StaticBody);
    }

    private void generateStartGround() {
        generateBorder(0, -gameScreen.getCamera().viewportHeight / 2 + Constants.BORDER_WIDTH / 2
                + Constants.BOTTOM_START, 90, gameScreen.getCamera().viewportWidth
                - Constants.BORDER_WIDTH * 2, false, false);

        if (startSign == -1) {
            gameScreen.getBorders().get(lastBorderNumber).setBorderIsRightOfScreen(false);
        } else {
            gameScreen.getBorders().get(lastBorderNumber).setBorderIsRightOfScreen(true);
        }
    }

    public void generateBorder(float xCntr, float yCntr, float rot, float len, boolean leftHold, boolean rightHold) {
        lastBorderNumber++;
        gameScreen.getBorders().put(lastBorderNumber, borderPool.obtain().init(PhysicalEntity.EntityType.Border, getImage("border"),
                gameScreen.getWorld(), xCntr, yCntr, rot, Constants.BORDER_WIDTH, len, len, BodyDef.BodyType.StaticBody));
        highestBorderHeight = gameScreen.getBorders().get(lastBorderNumber).getCenterY();

        if (leftHold) {
            generateAssociatedEntity(lastBorderNumber, PhysicalEntity.EntityType.Hold, "hold", xCntr - len / 2 + Constants.HOLD_WIDTH / 2,
                    yCntr + Constants.HOLD_HEIGHT / 2 - Constants.BORDER_WIDTH / 2, 0, Constants.HOLD_WIDTH, Constants.HOLD_HEIGHT, 0, BodyDef.BodyType.StaticBody);
        }
        if (rightHold) {
            generateAssociatedEntity(lastBorderNumber, PhysicalEntity.EntityType.Hold, "hold", xCntr + len / 2 - Constants.HOLD_WIDTH / 2,
                    yCntr + Constants.HOLD_HEIGHT / 2 - Constants.BORDER_WIDTH / 2, 0, Constants.HOLD_WIDTH, Constants.HOLD_HEIGHT, 0, BodyDef.BodyType.StaticBody);
        }
    }

    public void generateAssociatedEntity(int associatedBorder, PhysicalEntity.EntityType type, String entity, float xCntr, float yCntr,
                                         float rotation, float spriteW, float spriteH, float length, BodyDef.BodyType bodyType) {
        gameScreen.getBorders().get(associatedBorder).getBorderAssociatedEntities().add(
                associatedEntitiesPool.obtain().init(type, getImage(entity), gameScreen.getWorld(), xCntr, yCntr, rotation, spriteW, spriteH, length, bodyType));
    }

    public Texture getImage(String entity) {
        return game.getAssetManager().get(game.getAssets().get(Texture.class).get(entity), Texture.class);
    }

    public float getHighestBorderHeight() {
        return highestBorderHeight;
    }

    public int getLastBorderNumber() {
        return lastBorderNumber;
    }

    public Pool<PhysicalEntity> getBorderPool() {
        return borderPool;
    }

    public Pool<PhysicalEntity> getAssociatedEntitiesPool() {
        return associatedEntitiesPool;
    }
}
