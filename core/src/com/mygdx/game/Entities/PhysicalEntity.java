package com.mygdx.game.Entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.mygdx.game.Main.GameScreen;
import com.mygdx.game.Resources.Constants;

public class PhysicalEntity implements Pool.Poolable {

    private GameScreen gameScreen;
    private EntityType type;
    private Body body;
    private Sprite sprite;
    private Texture texture;
    private BodyDef bodyDef;
    private FixtureDef fixtureDef;
    private TextureRegion borderTextureRegion;
    private Array<PhysicalEntity> borderAssociatedEntities;
    private RevoluteJointDef barrierRevoluteJointDef;
    private DistanceJointDef barrierDistanceJointDef;
    private PhysicalEntity buttonAssociatedBarrier;
    private PhysicalEntity buttonAssociatedBorder;
    private PhysicalEntity teleporterAssociatedTeleporter;
    private PhysicalEntity teleporterAssociatedBorder;
    private Sprite teleporterArrowSprite;
    private int ballCurrentBorder;
    private int borderOption;
    private float previousBodyX;
    private float previousBodyY;
    private float previousBodyAngle;
    private float[] holdVertices;
    private float ballHeightWhenFocusedByCamera;
    private boolean isFirstUse;
    private boolean borderIsRightOfScreen;
    private boolean buttonActivated;
    private boolean teleporterAlive;
    private boolean teleporterHasArrow = false;
    public final static short BALL_ENTITY = 0x1 << 1;
    public final static short BARRIER_ENTITY = 0x1;

    public enum EntityType {
        Ball, Border, Hold, Button, Barrier, Teleporter
    }

    public PhysicalEntity(GameScreen gameScreen) {

        this.gameScreen = gameScreen;
        sprite = new Sprite();
        bodyDef = new BodyDef();
        fixtureDef = new FixtureDef();
        borderTextureRegion = new TextureRegion();
        borderAssociatedEntities = new Array<PhysicalEntity>();
        barrierRevoluteJointDef = new RevoluteJointDef();
        barrierDistanceJointDef= new DistanceJointDef();
        teleporterArrowSprite = new Sprite();
        holdVertices = new float[6];
        isFirstUse = true;
        teleporterAlive = true;
    }

    public PhysicalEntity init(EntityType type, Texture tex, World world, float xCntr, float yCntr, float rot, float spriteW,
                               float spriteH, float len, BodyDef.BodyType bodyType) {

        this.type = type;
        this.texture = tex;

        if (type == EntityType.Border) {
            borderTextureRegion.setTexture(texture);
            borderTextureRegion.setRegionWidth(texture.getWidth());
            borderTextureRegion.setRegionHeight((int) ((float) texture.getWidth() * len / Constants.BORDER_WIDTH));
            sprite.setRegion(borderTextureRegion);
        } else {
            sprite.setRegion(texture);
        }

        sprite.setSize(spriteW, spriteH);
        sprite.setOriginCenter();
        sprite.setPosition(xCntr - sprite.getWidth() / 2, yCntr - sprite.getHeight() / 2);
        sprite.setRotation(rot);

        bodyDef.position.set(xCntr, yCntr);
        bodyDef.angle = rot * MathUtils.degreesToRadians;
        bodyDef.type = bodyType;

        if (isFirstUse)
            body = world.createBody(bodyDef);
        body.setActive(true);

        if (type == EntityType.Ball || type == EntityType.Teleporter) {
            CircleShape shape = new CircleShape();
            if (type == EntityType.Ball) {
                shape.setRadius(Constants.BALL_RADIUS * Constants.BALL_CONTACT_RATIO);
                fixtureDef.density = Constants.BALL_DENSITY;
                fixtureDef.restitution = Constants.BALL_RESTITUTION;
                fixtureDef.filter.categoryBits = BALL_ENTITY;
                fixtureDef.isSensor = false;
                body.setAngularDamping(Constants.BALL_ANGULAR_DAMPING);
                ballHeightWhenFocusedByCamera = yCntr;
                ballCurrentBorder = 1;
            } else {
                shape.setRadius(Constants.TELEPORTER_RADIUS);
                fixtureDef.isSensor = true;
                gameScreen.getTeleporters().add(this);
            }
            fixtureDef.shape = shape;
            body.createFixture(fixtureDef);
            shape.dispose();

        } else {
            PolygonShape shape = new PolygonShape();
            if (type == EntityType.Hold) {
                holdVertices[0] = -sprite.getWidth() / 2;
                holdVertices[1] = -sprite.getHeight() / 2;
                holdVertices[2] = sprite.getWidth() / 2;
                holdVertices[3] = -sprite.getHeight() / 2;
                holdVertices[4] = 0;
                holdVertices[5] = sprite.getHeight() / 2;
                shape.set(holdVertices);
            } else {
                shape.setAsBox(sprite.getWidth() / 2, sprite.getHeight() / 2);
                if (type == EntityType.Barrier) {
                    fixtureDef.density = Constants.BARRIER_DENSITY;
                    fixtureDef.restitution = Constants.BARRIER_RESTITUTION;
                    fixtureDef.filter.categoryBits = BARRIER_ENTITY;
                    fixtureDef.filter.maskBits = BALL_ENTITY;
                    body.setAngularDamping(Constants.BARRIER_ANGULAR_DAMPING);
                } else if (type == EntityType.Border) {
                    borderOption = 1;
                } else if (type == EntityType.Button) {
                    buttonActivated = false;
                }
            }
            fixtureDef.isSensor = false;
            fixtureDef.shape = shape;
            body.createFixture(fixtureDef);
            shape.dispose();
        }

        this.storeBodyPosition();
        body.setUserData(this);

        return this;
    }

    @Override
    public void reset() {
        teleporterHasArrow = false;
        teleporterAssociatedTeleporter = null;
        buttonAssociatedBarrier = null;
        buttonAssociatedBorder = null;
        borderAssociatedEntities.clear();
        fixtureDef.density = 0;
        fixtureDef.restitution = 0;
        fixtureDef.isSensor = false;
        fixtureDef.filter.maskBits = -1;
        fixtureDef.filter.categoryBits = 1;
        body.setAngularDamping(0);
        teleporterAlive = true;
        body.setActive(false);
    }

    public void storeBodyPosition() {
        previousBodyX = body.getPosition().x;
        previousBodyY = body.getPosition().y;
        previousBodyAngle = body.getAngle();
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
        if (type == EntityType.Teleporter && teleporterHasArrow)
            teleporterArrowSprite.draw(batch);
    }

    public void interpolateSpritePositionAndAngle(float alpha) {
        sprite.setPosition(body.getPosition().x * (1 + alpha) - previousBodyX * alpha
                - sprite.getWidth() / 2, body.getPosition().y * (1 + alpha) - previousBodyY * alpha
                - sprite.getHeight() / 2);

        sprite.setRotation((body.getAngle() * (1 + alpha) - previousBodyAngle * alpha)
                * MathUtils.radiansToDegrees);
    }

    public void adjustSpritePosition() {
        sprite.setPosition(body.getPosition().x - sprite.getWidth() / 2, body.getPosition().y - sprite.getHeight() / 2);
    }

    public void setBarrierRevoluteJoint(float anchorAX, float anchorAY, float anchorBX, float anchorBY, Body border, World world) {
        barrierRevoluteJointDef.bodyA = body;
        barrierRevoluteJointDef.bodyB = border;
        barrierRevoluteJointDef.collideConnected = false;
        barrierRevoluteJointDef.localAnchorA.set(anchorAX, anchorAY);
        barrierRevoluteJointDef.localAnchorB.set(anchorBX, anchorBY);
        world.createJoint(barrierRevoluteJointDef);
    }

    public void setBarrierDistanceJoint(float anchorAX, float anchorAY, float anchorBX, float anchorBY, float length, Body border, World world) {
        barrierDistanceJointDef.bodyA = body;
        barrierDistanceJointDef.bodyB = border;
        barrierDistanceJointDef.localAnchorA.set(anchorAX, anchorAY);
        barrierDistanceJointDef.localAnchorB.set(anchorBX, anchorBY);
        barrierDistanceJointDef.length = length;
        barrierDistanceJointDef.collideConnected = false;
        barrierDistanceJointDef.dampingRatio = Constants.BARRIER_JOINT_DAMPING_RATIO;
        barrierDistanceJointDef.frequencyHz = Constants.BARRIER_JOINT_FREQUENCY;
        world.createJoint(barrierDistanceJointDef);
        gameScreen.getDynamicEntities().add(this);
    }

    public void translateEdge(float deltaY) {
        body.setTransform(body.getPosition().x, body.getPosition().y + deltaY, 0);
        sprite.translate(0, deltaY);
    }

    public float getCenterX() {
        return body.getPosition().x;
    }

    public float getCenterY() {
        return body.getPosition().y;
    }

    public float getLength() {
        return sprite.getHeight();
    }

    public Body getBody() {
        return body;
    }

    public boolean isTeleporterAlive() {
        return teleporterAlive;
    }

    public void setTeleporterAlive(boolean teleporterAlive) {
        this.teleporterAlive = teleporterAlive;
    }

    public Array<PhysicalEntity> getBorderAssociatedEntities() {
        return borderAssociatedEntities;
    }

    public boolean borderIsRightOfScreen() {
        return borderIsRightOfScreen;
    }

    public void setBorderIsRightOfScreen(boolean isRightOfScreen) {
        this.borderIsRightOfScreen = isRightOfScreen;
    }

    public EntityType getType() {
        return type;
    }

    public float getRadius() {
        return sprite.getHeight() / 2;
    }

    public float getBallHeightWhenFocusedByCamera() {
        return ballHeightWhenFocusedByCamera;
    }

    public void setBallHeightWhenFocusedByCamera(float ballHeightWhenFocusedByCamera) {
        this.ballHeightWhenFocusedByCamera = ballHeightWhenFocusedByCamera;
    }

    public int getBallCurrentBorder() {
        return ballCurrentBorder;
    }

    public void setBallCurrentBorder(int currentBorder) {
        this.ballCurrentBorder = currentBorder;
    }

    public PhysicalEntity getButtonAssociatedBarrier() {
        return buttonAssociatedBarrier;
    }

    public void setButtonAssociatedBarrier(PhysicalEntity associatedBarrier) {
        this.buttonAssociatedBarrier = associatedBarrier;
    }

    public PhysicalEntity getButtonAssociatedBorder() {
        return buttonAssociatedBorder;
    }

    public void setButtonAssociatedBorder(PhysicalEntity associatedBorder) {
        this.buttonAssociatedBorder = associatedBorder;
    }

    public boolean buttonIsActivated() {
        return buttonActivated;
    }

    public void setButtonActivated(boolean activated) {
        this.buttonActivated = activated;
    }

    public int getBorderOption() {
        return borderOption;
    }

    public void setBorderOption(int borderOption) {
        this.borderOption = borderOption;
    }

    public void rotateTeleporter(float angle) {
        sprite.rotate(angle);
    }

    public void rotateArrow(float angle) {
        teleporterArrowSprite.rotate(angle);
    }

    public PhysicalEntity getTeleporterAssociatedBorder() {
        return teleporterAssociatedBorder;
    }

    public void setTeleporterAssociatedBorder(PhysicalEntity teleporterAssociatedBorder) {
        this.teleporterAssociatedBorder = teleporterAssociatedBorder;
    }

    public PhysicalEntity getTeleporterAssociatedTeleporter() {
        return teleporterAssociatedTeleporter;
    }

    public void setTeleporterAssociatedTeleporter(PhysicalEntity associatedTeleporter) {
        this.teleporterAssociatedTeleporter = associatedTeleporter;
    }

    public void setTeleporterArrow(EntityGenerator generator) {
        teleporterArrowSprite = new Sprite(generator.getImage("arrow"));
        teleporterArrowSprite.setSize(Constants.TELEPORTER_ARROW_WIDTH, Constants.TELEPORTER_ARROW_HEIGHT);
        teleporterArrowSprite.setPosition(body.getPosition().x - teleporterArrowSprite.getWidth() / 2, body.getPosition().y);
        teleporterArrowSprite.setOrigin(teleporterArrowSprite.getWidth() / 2, 0);
        teleporterHasArrow = true;
    }

    public boolean teleporterHasArrow() {
        return teleporterHasArrow;
    }

    public Sprite getTeleporterArrowSprite() {
        return teleporterArrowSprite;
    }
}
