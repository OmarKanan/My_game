package com.mygdx.game.Main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.mygdx.game.Resources.Assets;

public class LoadingScreen implements Screen {

    private GamePrototype game;

    public LoadingScreen(GamePrototype game) {
        this.game = game;
        game.setAssets(new Assets(game));
        game.loadAssets();
    }

    @Override
    public void render(float delta) {
        if (game.getAssetManager().update()) {
            game.setMenuScreen(new MenuScreen(game));
            game.setScreen(game.getMenuScreen());
        }

        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void dispose() {

    }

    @Override
    public void show() {

    }

    @Override
    public void resize(int width, int height) {

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
