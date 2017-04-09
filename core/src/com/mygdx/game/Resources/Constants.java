package com.mygdx.game.Resources;

import com.badlogic.gdx.math.Vector2;

public class Constants {

    public final static float MAX_DESYNCH_TIME = 0.1f;
    public final static float PHYSICS_STEP_TIME = 1/60f;

    public final static float WORLD_WIDTH = 20f;
    public final static Vector2 WORLD_GRAVITY = new Vector2(0, -9.8f);

    public final static float TRAJECTORY_PROJECTION_TIME = 1.5f;
    public final static float TRAJECTORY_POINTS_SIZE = WORLD_WIDTH / 100;

    public final static float BALL_DENSITY = 1f;
    public final static float BALL_RESTITUTION = 0.5f;
    public final static float BALL_ANGULAR_DAMPING = 7f;
    public final static float BALL_RADIUS = WORLD_WIDTH / 40;
    public final static float BALL_CONTACT_RATIO = 0.9f;
    public final static float BALL_DETECTION_RATIO = 2.0f;
    public final static float BALL_LAUNCH_COEFF = 10 * BALL_RADIUS;
    public final static float BALL_MAX_LAUNCH = 20f;

    public final static float BORDER_WIDTH = WORLD_WIDTH / 40;
    public final static float BOTTOM_START = WORLD_WIDTH / 5;

    public final static float HOLD_HEIGHT = BORDER_WIDTH * 3;
    public final static float HOLD_WIDTH = HOLD_HEIGHT * 1.3f;

    public final static float BUTTON_WIDTH = BORDER_WIDTH;
    public final static float BUTTON_HEIGHT = BUTTON_WIDTH * 6;

    public final static float BARRIER_DENSITY = 0.5f;
    public final static float BARRIER_RESTITUTION = 0.5f;
    public final static float BARRIER_ANGULAR_DAMPING = 5f;
    public final static float BARRIER_WIDTH = BORDER_WIDTH;
    public static final float BARRIER_JOINT_DAMPING_RATIO = 0.8f;
    public static final float BARRIER_JOINT_FREQUENCY = 100f;
    public static final float BARRIER_ACTIVATION_DAMPING_RATIO = 0.8f;
    public static final float BARRIER_ACTIVATION_FREQUENCY = 0.5f;
    public static final float BARRIER_ACTIVATION_ANGULAR_IMPULSE = 500;

    public final static float TELEPORTER_WIDTH = WORLD_WIDTH / 10;
    public final static float TELEPORTER_HEIGHT = WORLD_WIDTH * 83 / 92 / 10;
    public final static float TELEPORTER_RADIUS = TELEPORTER_WIDTH / 3;
    public final static float TELEPORTER_ROTATION_TICK_DEGREES = 10;
    public final static float TELEPORTER_ARROW_WIDTH = WORLD_WIDTH * 40 / 50 / 15;
    public final static float TELEPORTER_ARROW_HEIGHT = WORLD_WIDTH / 12;
    public final static float TELEPORTER_ARROW_ROTATION_TICK_DEGREES = 2;

    public final static float CAMERA_TRANSLATION_MIN_MARGIN = BORDER_WIDTH * 2;
    public final static float CAMERA_TRANSLATE_STEP_TIME = PHYSICS_STEP_TIME;
    public final static float CAMERA_TRANSLATE_TIME = CAMERA_TRANSLATE_STEP_TIME * 50;
    public final static int CAMERA_TRANSLATE_TICKS_COUNT =
            (int) (CAMERA_TRANSLATE_TIME / CAMERA_TRANSLATE_STEP_TIME);

    // WORLD_WIDTH --> 500 px
    // WORLD_HEIGHT --> 800 px

}