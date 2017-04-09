package com.mygdx.game.Entities;

import com.mygdx.game.Main.GameScreen;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

public class LevelGenerator {

    private int level;
    private EntityGenerator generator;
    private GameScreen gameScreen;
    private Options options;
    private Random random;
    private Method[][] optionsPerLevel;

    public LevelGenerator(int level, EntityGenerator generator, GameScreen gameScreen) {

        this.level = level;
        this.generator = generator;
        this.gameScreen = gameScreen;
        this.options = new Options(gameScreen, generator);
        this.random = new Random();
        setOptionsPerLevel();

////////////////////////////////////////////////////////////////////////
        this.level = 4;
////////////////////////////////////////////////////////////////////////
    }

    public void generateEntities() {

        while (generator.getHighestBorderHeight() < gameScreen.getBall().getBallHeightWhenFocusedByCamera()
                + gameScreen.getCamera().viewportHeight * 1.5f) {
            try {
                optionsPerLevel[level - 1][random.nextInt(optionsPerLevel[level - 1].length)].invoke(options, level);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private void setOptionsPerLevel() {
        Method[] level1 = new Method[1], level2 = new Method[2], level3 = new Method[3], level4 = new Method[1];

        try {
            level1[0] = options.getClass().getMethod("option1", int.class);

            level2[0] = options.getClass().getMethod("option1", int.class);
            level2[1] = options.getClass().getMethod("option2", int.class);

            level3[0] = options.getClass().getMethod("option1", int.class);
            level3[1] = options.getClass().getMethod("option2", int.class);
            level3[2] = options.getClass().getMethod("option3", int.class);

            level4[0] = options.getClass().getMethod("option4", int.class);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        this.optionsPerLevel = new Method[][] {
                level1, level2, level3, level4};
    }
}
