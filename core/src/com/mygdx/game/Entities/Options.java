package com.mygdx.game.Entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.mygdx.game.Main.GameScreen;
import com.mygdx.game.Resources.Constants;

public class Options {

    private GameScreen gameScreen;
    private com.mygdx.game.Entities.EntityGenerator generator;
    private float[][] borderDeltaYPerLevelPerOption;
    private float[][] borderLengthPerLevelPerOption;
    private boolean noHold = false;


    public Options(GameScreen gameScreen, EntityGenerator generator) {
        this.gameScreen = gameScreen;
        this.generator = generator;
        setArrays();
    }

    @SuppressWarnings("unused")
    public void option1(int level) {
        generateBorder(1, level);
    }

    @SuppressWarnings("unused")
    public void option2(int level) {
        noHold = true;
        generateBorder(2, level);
        getLastBorderMinus(0).setBorderOption(2);
    }

    @SuppressWarnings("unused")
    public void option3(int level) {
        noHold = true;
        generateBorder(3, level);
        getLastBorderMinus(0).setBorderOption(3);
    }

    @SuppressWarnings("unused")
    public void option4(int level) {
        noHold = true;
        generateBorder(4, level);
        getLastBorderMinus(0).setBorderOption(4);
    }

    private void generateBorder(int option, int level) {
        float borderDeltaY = borderDeltaYPerLevelPerOption[option - 1][level - 1];
        float borderLength = borderLengthPerLevelPerOption[option - 1][level - 1];
        float centerX = (gameScreen.getCamera().viewportWidth / 2 - borderLength / 2 - Constants.BORDER_WIDTH);
        float centerY = generator.getHighestBorderHeight() + borderDeltaY;
        boolean leftHold = true;
        boolean rightHold = false;
        boolean isRightOfScreen = true;

        if (getLastBorderMinus(0).borderIsRightOfScreen()) {
            centerX = -centerX;
            leftHold = false;
            rightHold = true;
            isRightOfScreen = false;
        }
        if (noHold) {
            leftHold = false;
            rightHold = false;
            noHold = false;
        }
        generator.generateBorder(centerX, centerY, 90, borderLength, leftHold, rightHold);
        getLastBorderMinus(0).setBorderIsRightOfScreen(isRightOfScreen);

        switch (getLastBorderMinus(1).getBorderOption()) {
            case (2):
                setOption2();
                break;
            case (3):
                setOption3();
                break;
            case (4):
                setOption4();
                break;
        }
    }

    private void setOption2() {
        setButton(gameScreen.getCamera().viewportWidth / 2 - Constants.BORDER_WIDTH - Constants.BUTTON_WIDTH / 2,
                MathUtils.random((getLastBorderMinus(1).getCenterY() + getLastBorderMinus(2).getCenterY()) / 2,
                        (getLastBorderMinus(0).getCenterY() + getLastBorderMinus(1).getCenterY()) / 2), 180);
        float barrierLength = (getLastBorderMinus(0).getCenterY() - getLastBorderMinus(1).getCenterY()) * 0.99f;
        setBarrier(barrierLength, getLastBorderMinus(1).getCenterX() + (getLastBorderMinus(1).getLength() - Constants.BARRIER_WIDTH) / 2,
                getLastBorderMinus(1).getCenterY() + barrierLength / 2 - Constants.BORDER_WIDTH / 2, 0, BodyDef.BodyType.DynamicBody);
        setBarrierJoints(barrierLength);
        getBeforeLastBorderLastEntityMinus(1).setButtonAssociatedBarrier(getBeforeLastBorderLastEntityMinus(0));
        getBeforeLastBorderLastEntityMinus(1).setButtonAssociatedBorder(getLastBorderMinus(1));
    }

    private void setOption3() {
        float barrierLength = getLastBorderMinus(0).getCenterY() - getLastBorderMinus(1).getCenterY() - Constants.BORDER_WIDTH;
        setBarrier(barrierLength, getLastBorderMinus(1).getCenterX() + MathUtils.random(0, (getLastBorderMinus(1).getLength() - Constants.BARRIER_WIDTH) / 2),
                getLastBorderMinus(1).getCenterY() + barrierLength / 2 + Constants.BORDER_WIDTH / 2, 0, BodyDef.BodyType.StaticBody);
        setTeleporter(MathUtils.random(getBeforeLastBorderLastEntityMinus(0).getCenterX() + Constants.BARRIER_WIDTH / 2 + Constants.TELEPORTER_WIDTH / 2,
                gameScreen.getCamera().viewportWidth / 2 - Constants.BORDER_WIDTH - Constants.TELEPORTER_WIDTH / 2), getLastBorderMinus(1).getCenterY()
                + barrierLength / 2 + Constants.BORDER_WIDTH + MathUtils.random(-barrierLength / 8, barrierLength / 2 - Constants.TELEPORTER_HEIGHT));
        setTeleporter(MathUtils.random(-gameScreen.getCamera().viewportWidth / 2 + Constants.BORDER_WIDTH + Constants.TELEPORTER_WIDTH / 2,
                getBeforeLastBorderLastEntityMinus(1).getCenterX() - Constants.BARRIER_WIDTH / 2 - Constants.TELEPORTER_WIDTH / 2), getLastBorderMinus(1).getCenterY()
                + barrierLength / 2 + Constants.BORDER_WIDTH + MathUtils.random(-barrierLength / 4, barrierLength / 4));
        getBeforeLastBorderLastEntityMinus(0).setTeleporterAssociatedTeleporter(getBeforeLastBorderLastEntityMinus(1));
        getBeforeLastBorderLastEntityMinus(1).setTeleporterAssociatedTeleporter(getBeforeLastBorderLastEntityMinus(0));
        getBeforeLastBorderLastEntityMinus(0).setTeleporterAssociatedBorder(getLastBorderMinus(1));
        getBeforeLastBorderLastEntityMinus(1).setTeleporterAssociatedBorder(getLastBorderMinus(1));
    }

    private void setOption4() {
        setOption3();
        if (getLastBorderMinus(1).borderIsRightOfScreen())
            getBeforeLastBorderLastEntityMinus(1).setTeleporterArrow(generator);
        else
            getBeforeLastBorderLastEntityMinus(0).setTeleporterArrow(generator);

    }

    private void setOption5() {
        aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa;
    }

    private void setButton(float buttonCenterX, float buttonCenterY, float buttonRotation) {
        if (getLastBorderMinus(1).borderIsRightOfScreen()) {
            buttonCenterX = -buttonCenterX;
            buttonRotation = 0;
        }
        generator.generateAssociatedEntity(generator.getLastBorderNumber() - 1, PhysicalEntity.EntityType.Button, "button",
                buttonCenterX, buttonCenterY, buttonRotation, Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT, 0, BodyDef.BodyType.StaticBody);
    }

    private void setBarrier(float barrierLength, float barrierCenterX, float barrierCenterY, float barrierRotation, BodyDef.BodyType bodyType) {
        if (getLastBorderMinus(1).borderIsRightOfScreen()) {
            barrierCenterX -= (barrierCenterX - getLastBorderMinus(1).getCenterX()) * 2;
            barrierRotation = 180;
        }
        generator.generateAssociatedEntity(generator.getLastBorderNumber() - 1, PhysicalEntity.EntityType.Barrier, "barrier",
                barrierCenterX, barrierCenterY, barrierRotation, Constants.BARRIER_WIDTH, barrierLength, barrierLength, bodyType);
    }

    private void setBarrierJoints(float barrierLength) {
        if (getLastBorderMinus(1).borderIsRightOfScreen()) {
            getBeforeLastBorderLastEntityMinus(0).setBarrierRevoluteJoint(0, barrierLength / 2 - Constants.BORDER_WIDTH / 2, 0,
                    getLastBorderMinus(1).getLength() / 2 - Constants.BORDER_WIDTH / 2, getLastBorderMinus(1).getBody(), gameScreen.getWorld());
            getBeforeLastBorderLastEntityMinus(0).setBarrierDistanceJoint(0, -barrierLength / 2,
                    -barrierLength / 2 - Constants.BORDER_WIDTH / 2, getLastBorderMinus(1).getLength() / 2 - Constants.BORDER_WIDTH / 2,
                    1.5f * barrierLength, getLastBorderMinus(1).getBody(), gameScreen.getWorld());
        } else {
            getBeforeLastBorderLastEntityMinus(0).setBarrierRevoluteJoint(0, -barrierLength / 2 + Constants.BORDER_WIDTH / 2, 0,
                    -getLastBorderMinus(1).getLength() / 2 + Constants.BORDER_WIDTH / 2, getLastBorderMinus(1).getBody(), gameScreen.getWorld());
            getBeforeLastBorderLastEntityMinus(0).setBarrierDistanceJoint(0, barrierLength / 2,
                    -barrierLength / 2 - Constants.BORDER_WIDTH / 2, -getLastBorderMinus(1).getLength() / 2 + Constants.BORDER_WIDTH / 2,
                    1.5f * barrierLength, getLastBorderMinus(1).getBody(), gameScreen.getWorld());
        }
    }

    private void setTeleporter(float teleporterCenterX, float teleporterCenterY) {
        generator.generateAssociatedEntity(generator.getLastBorderNumber() - 1, PhysicalEntity.EntityType.Teleporter, "teleporter",
                teleporterCenterX, teleporterCenterY, 0, Constants.TELEPORTER_WIDTH, Constants.TELEPORTER_HEIGHT, 0, BodyDef.BodyType.StaticBody);
    }

    private PhysicalEntity getLastBorderMinus(int i) {
        return gameScreen.getBorders().get(generator.getLastBorderNumber() - i);
    }

    private PhysicalEntity getBeforeLastBorderLastEntityMinus(int i) {
        return getLastBorderMinus(1).getBorderAssociatedEntities().get(getLastBorderMinus(1).getBorderAssociatedEntities().size - (i+1));
    }

    private void setArrays() {
        float viewportHeight = gameScreen.getCamera().viewportHeight;
        float viewportWidth = gameScreen.getCamera().viewportWidth;

        float[] borderDeltaYPerLevelOption1 = new float[] {
                viewportHeight * 0.30f, viewportHeight * 0.35f, viewportHeight * 0.40f, viewportHeight * 0f};
        float[] borderLengthPerLevelOption1 = new float[] {
                viewportWidth * 0.75f, viewportWidth * 0.70f, viewportWidth * 0.65f, viewportWidth * 0f};

        float[] borderDeltaYPerLevelOption2 = new float[] {
                viewportHeight * 0f, viewportHeight * 0.30f, viewportHeight * 0.35f, viewportHeight * 0f};
        float[] borderLengthPerLevelOption2 = new float[] {
                viewportWidth * 0f, viewportWidth * 0.75f, viewportWidth * 0.70f, viewportWidth * 0f};

        float[] borderDeltaYPerLevelOption3 = new float[] {
                viewportHeight * 0f, viewportHeight * 0f, viewportHeight * 0.30f, viewportHeight * 0f};
        float[] borderLengthPerLevelOption3 = new float[] {
                viewportWidth * 0f, viewportWidth * 0f, viewportWidth * 0.75f, viewportWidth * 0f};

        float[] borderDeltaYPerLevelOption4 = new float[] {
                viewportHeight * 0f, viewportHeight * 0f, viewportHeight * 0f, viewportHeight * 0.30f};
        float[] borderLengthPerLevelOption4 = new float[] {
                viewportWidth * 0f, viewportWidth * 0f, viewportWidth * 0f, viewportWidth * 0.75f};

        borderDeltaYPerLevelPerOption = new float[][] {
                borderDeltaYPerLevelOption1, borderDeltaYPerLevelOption2, borderDeltaYPerLevelOption3, borderDeltaYPerLevelOption4};
        borderLengthPerLevelPerOption = new float[][] {
                borderLengthPerLevelOption1, borderLengthPerLevelOption2, borderLengthPerLevelOption3, borderLengthPerLevelOption4};
    }
}
