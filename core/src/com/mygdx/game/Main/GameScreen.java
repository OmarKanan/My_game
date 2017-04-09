package com.mygdx.game.Main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.JointEdge;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.game.Movement.BallController;
import com.mygdx.game.Movement.Trajectory;
import com.mygdx.game.Entities.EntityGenerator;
import com.mygdx.game.Entities.LevelGenerator;
import com.mygdx.game.Entities.PhysicalEntity;
import com.mygdx.game.Resources.Constants;

import java.util.HashMap;
import java.util.Iterator;

public class GameScreen implements Screen {

    private GamePrototype game;
    private GestureDetector detector;
    private Trajectory trajectory;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private Sprite background;
    private World world;
    private PhysicalEntity ball;
    private PhysicalEntity[] edges;
    private HashMap<Integer, PhysicalEntity> borders;
    private Array<PhysicalEntity> dynamicEntities;
    private Array<PhysicalEntity> teleporters;
    private EntityGenerator entityGenerator;
    private LevelGenerator levelGenerator;
    @SuppressWarnings("FieldCanBeLocal")
    private Iterator<Integer> borderIterator;
    private long renderStartTimeMillis = 0;
    private float desynchTime = 0;
    private int translateCameraTickCount = 0;
    private boolean isCameraTranslating = false;
    private boolean teleportBall = false, arrowTeleportBall = false;
    private float ballTeleportX, ballTeleportY;
    private float teleporterArrowAngle;
    private Box2DDebugRenderer debugRenderer;
    private Matrix4 debugMatrix;

    public PhysicalEntity getBall() {
        return ball;
    }

    public void setBall(PhysicalEntity ball) {
        this.ball = ball;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public World getWorld() {
        return world;
    }

    public PhysicalEntity[] getEdges() {
        return edges;
    }

    public HashMap<Integer, PhysicalEntity> getBorders() {
        return borders;
    }

    public Array<PhysicalEntity> getDynamicEntities() {
        return dynamicEntities;
    }

    public boolean isCameraTranslating() {
        return isCameraTranslating;
    }

    public Array<PhysicalEntity> getTeleporters() {
        return teleporters;
    }

    public GameScreen(GamePrototype game) {

        this.game = game;
        BallController controller = new BallController(this);
        detector = new GestureDetector(controller);
        trajectory = new Trajectory(controller, game, this);

        camera = new OrthographicCamera();
        viewport = new StretchViewport(Constants.WORLD_WIDTH, Constants.WORLD_WIDTH * game.getAspectRatio(),camera);
        viewport.apply();

        batch = new SpriteBatch();
        background = new Sprite(game.getAssetManager().get(game.getAssets().get(Texture.class).get("background"), Texture.class));
        background.setSize(camera.viewportWidth, camera.viewportHeight);
        background.setPosition(-camera.viewportWidth / 2, -camera.viewportHeight / 2);

        world = new World(Constants.WORLD_GRAVITY, true);
        setWorldContactListener();

        edges = new PhysicalEntity[2];
        borders = new HashMap<Integer, PhysicalEntity>();
        dynamicEntities = new Array<PhysicalEntity>();
        teleporters = new Array<PhysicalEntity>();

        entityGenerator = new EntityGenerator(game, this);
        entityGenerator.generateStartEntities();
        levelGenerator = new LevelGenerator(1, entityGenerator, this);
        levelGenerator.generateEntities();

        debugRenderer = new Box2DDebugRenderer();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(detector);
    }

    @Override
    public void render(float delta) {
        drawBatch();
        //debugRenderer.render(world, debugMatrix);
        stepWorld();
        stepGame();
    }

    private void drawBatch() {

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        //debugMatrix = batch.getProjectionMatrix();

        batch.begin();
            background.draw(batch);
            drawEntities();
            trajectory.draw(batch);
            ball.draw(batch);
        batch.end();
    }

    private void drawEntities() {
        edges[0].draw(batch);
        edges[1].draw(batch);

        for (int i : borders.keySet()) {
            for (PhysicalEntity entity : borders.get(i).getBorderAssociatedEntities())
                if (!(entity.getType() == PhysicalEntity.EntityType.Barrier))
                entity.draw(batch);
            borders.get(i).draw(batch);
            for (PhysicalEntity entity : borders.get(i).getBorderAssociatedEntities())
                if (entity.getType() == PhysicalEntity.EntityType.Barrier)
                    entity.draw(batch);
        }
    }

    private void stepWorld() {

        desynchTime += Math.min(TimeUtils.timeSinceMillis(renderStartTimeMillis) / 1000f,
                Constants.MAX_DESYNCH_TIME);
        renderStartTimeMillis = TimeUtils.millis();

        while (desynchTime >= Constants.PHYSICS_STEP_TIME) {
            ball.storeBodyPosition();
            for (PhysicalEntity entity : dynamicEntities)
                entity.storeBodyPosition();
            world.step(Constants.PHYSICS_STEP_TIME, 6, 2);
            desynchTime -= Constants.PHYSICS_STEP_TIME;
        }
        if (teleportBall) {
            ball.getBody().setTransform(ballTeleportX, ballTeleportY, ball.getBody().getAngle());
            teleportBall = false;
            if (arrowTeleportBall) {
                float velocity = ball.getBody().getLinearVelocity().len();
                ball.getBody().setLinearVelocity(-velocity * MathUtils.sinDeg(teleporterArrowAngle), velocity * MathUtils.cosDeg(teleporterArrowAngle));
                arrowTeleportBall = false;
            }
            removeDeadTeleporters();
            ball.adjustSpritePosition();
        } else {
            ball.interpolateSpritePositionAndAngle(desynchTime / Constants.PHYSICS_STEP_TIME);
        }
        for (PhysicalEntity entity : dynamicEntities)
            entity.interpolateSpritePositionAndAngle(desynchTime / Constants.PHYSICS_STEP_TIME);
        for (PhysicalEntity teleporter : teleporters) {
            teleporter.rotateTeleporter(Constants.TELEPORTER_ROTATION_TICK_DEGREES);
            if (teleporter.teleporterHasArrow())
                teleporter.rotateArrow(Constants.TELEPORTER_ARROW_ROTATION_TICK_DEGREES);
        }
    }

    private void stepGame() {

        if (!ball.getBody().isAwake() && !isCameraTranslating &&
                ball.getCenterY() > ball.getBallHeightWhenFocusedByCamera() + Constants.CAMERA_TRANSLATION_MIN_MARGIN) {

            isCameraTranslating = true;
            updateBallCurrentBorder();

            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    camera.translate(0, (ball.getCenterY() - ball.getBallHeightWhenFocusedByCamera())
                            / (float) Constants.CAMERA_TRANSLATE_TICKS_COUNT);
                    background.translate(0, (ball.getCenterY() - ball.getBallHeightWhenFocusedByCamera())
                            / (float) Constants.CAMERA_TRANSLATE_TICKS_COUNT);
                    edges[0].translateEdge((ball.getCenterY() - ball.getBallHeightWhenFocusedByCamera()) /
                            (float) Constants.CAMERA_TRANSLATE_TICKS_COUNT);
                    edges[1].translateEdge((ball.getCenterY() - ball.getBallHeightWhenFocusedByCamera()) /
                            (float) Constants.CAMERA_TRANSLATE_TICKS_COUNT);

                    translateCameraTickCount++;
                    if (translateCameraTickCount == Constants.CAMERA_TRANSLATE_TICKS_COUNT) {
                        removeDeadEntities();
                        ball.setBallHeightWhenFocusedByCamera(ball.getCenterY());
                        levelGenerator.generateEntities();
                        translateCameraTickCount = 0;
                        isCameraTranslating = false;
                    }
                }
            }, 0, Constants.PHYSICS_STEP_TIME, Constants.CAMERA_TRANSLATE_TICKS_COUNT - 1);
        }
    }

    private void removeDeadEntities() {
        borderIterator = borders.keySet().iterator();
        while (borderIterator.hasNext()){
            int i = borderIterator.next();
            if (ball.getCenterY() - borders.get(i).getCenterY() > camera.viewportHeight / 2) {
                for (PhysicalEntity entity : borders.get(i).getBorderAssociatedEntities()) {
                    for (JointEdge jointEdge : entity.getBody().getJointList())
                        world.destroyJoint(jointEdge.joint);
                    dynamicEntities.removeValue(entity, true);
                    teleporters.removeValue(entity, true);
                    entityGenerator.getAssociatedEntitiesPool().free(entity);
                }
                entityGenerator.getBorderPool().free(borders.get(i));
                borderIterator.remove();
            }
        }
    }

    private void removeDeadTeleporters() {
        for (int i = 0; i < teleporters.size; i++) {
            if (!teleporters.get(i).isTeleporterAlive()) {
                teleporters.get(i).getTeleporterAssociatedBorder().getBorderAssociatedEntities().removeValue(teleporters.get(i), true);
                entityGenerator.getAssociatedEntitiesPool().free(teleporters.get(i));
                teleporters.removeValue(teleporters.get(i), true);
                i--;
            }
        }
    }

    private void updateBallCurrentBorder() {
        for (int i : borders.keySet()) {
            if (ball.getCenterY() - borders.get(i).getCenterY() < Constants.CAMERA_TRANSLATION_MIN_MARGIN) {
                ball.setBallCurrentBorder(i);
                break;
            }
        }
    }

    private void setWorldContactListener() {
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {

                if (contactIsScore(contact))
                    score();

                if (buttonActivated(contact.getFixtureA()))
                    activateBarrier((PhysicalEntity) contact.getFixtureA().getBody().getUserData());
                else if (buttonActivated(contact.getFixtureB()))
                    activateBarrier((PhysicalEntity) contact.getFixtureB().getBody().getUserData());

                if (teleporterUsed(contact.getFixtureA()))
                    teleportBallFrom((PhysicalEntity) contact.getFixtureA().getBody().getUserData());
                else if (teleporterUsed(contact.getFixtureB()))
                    teleportBallFrom((PhysicalEntity) contact.getFixtureB().getBody().getUserData());

            }

            @Override
            public void endContact(Contact contact) {

            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });
    }

    private boolean contactIsScore(Contact contact) {
        return (((contact.getFixtureA().getBody().getUserData() == ball && contact.getFixtureB().getBody().getUserData() == edges[0])
                    || (contact.getFixtureB().getBody().getUserData() == ball && contact.getFixtureA().getBody().getUserData() == edges[0]))
                && borders.get(ball.getBallCurrentBorder()).borderIsRightOfScreen()
                && contact.getWorldManifold().getPoints()[0].y > borders.get(ball.getBallCurrentBorder() + 1).getCenterY())
            ||
                (((contact.getFixtureA().getBody().getUserData() == ball && contact.getFixtureB().getBody().getUserData() == edges[1])
                    || (contact.getFixtureB().getBody().getUserData() == ball && contact.getFixtureA().getBody().getUserData() == edges[1]))
                && !borders.get(ball.getBallCurrentBorder()).borderIsRightOfScreen()
                && contact.getWorldManifold().getPoints()[0].y > borders.get(ball.getBallCurrentBorder() + 1).getCenterY());
    }

    private boolean buttonActivated(Fixture fixture) {
        return ((PhysicalEntity) fixture.getBody().getUserData()).getType() == PhysicalEntity.EntityType.Button
                && !((PhysicalEntity) fixture.getBody().getUserData()).buttonIsActivated();
    }

    private void activateBarrier(PhysicalEntity button) {
        button.setButtonActivated(true);
        ((DistanceJoint) button.getButtonAssociatedBarrier().getBody().getJointList().get(1).joint).setLength(0);
        ((DistanceJoint) button.getButtonAssociatedBarrier().getBody().getJointList().get(1).joint).setFrequency(Constants.BARRIER_ACTIVATION_FREQUENCY);
        ((DistanceJoint) button.getButtonAssociatedBarrier().getBody().getJointList().get(1).joint).setDampingRatio(Constants.BARRIER_ACTIVATION_DAMPING_RATIO);
        if (button.getButtonAssociatedBorder().borderIsRightOfScreen())
            button.getButtonAssociatedBarrier().getBody().applyAngularImpulse(-Constants.BARRIER_ACTIVATION_ANGULAR_IMPULSE, true);
        else
            button.getButtonAssociatedBarrier().getBody().applyAngularImpulse(Constants.BARRIER_ACTIVATION_ANGULAR_IMPULSE, true);
    }

    private boolean teleporterUsed(Fixture fixture) {
        return ((PhysicalEntity) fixture.getBody().getUserData()).getType() == PhysicalEntity.EntityType.Teleporter;
    }

    private void teleportBallFrom(PhysicalEntity teleporter) {
        teleportBall = true;
        ballTeleportX = teleporter.getTeleporterAssociatedTeleporter().getCenterX();
        ballTeleportY = teleporter.getTeleporterAssociatedTeleporter().getCenterY();
        teleporter.setTeleporterAlive(false);
        teleporter.getTeleporterAssociatedTeleporter().setTeleporterAlive(false);
        if (teleporter.getTeleporterAssociatedTeleporter().teleporterHasArrow()) {
            arrowTeleportBall = true;
            teleporterArrowAngle = teleporter.getTeleporterAssociatedTeleporter().getTeleporterArrowSprite().getRotation();
        }
    }

    private void score() {
        Gdx.app.log("", "Score !!!");
    }

    @Override
    public void dispose() {
        batch.dispose();
        world.dispose();
        debugRenderer.dispose();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }
}
