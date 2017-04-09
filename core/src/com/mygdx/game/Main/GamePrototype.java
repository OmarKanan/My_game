package com.mygdx.game.Main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.mygdx.game.Resources.Assets;

import java.util.HashMap;

public class GamePrototype extends Game {

    private AssetManager assetManager;
    private Assets assets;
    private LoadingScreen loadingScreen = null;
    private MenuScreen menuScreen = null;
    private GameScreen gameScreen = null;
    private float aspectRatio;

    public void setMenuScreen(MenuScreen menuScreen) {
        this.menuScreen = menuScreen;
    }

    public void setGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    public MenuScreen getMenuScreen() {
        return menuScreen;
    }

    public GameScreen getGameScreen() {
        return gameScreen;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public HashMap<Class, HashMap<String, String>> getAssets() {
        return assets.getAssets();
    }

    public void setAssets(Assets assets) {
        this.assets = assets;
    }

    public void loadAssets() {
        assets.loadAssets();
    }

    @Override
    public void create() {
        aspectRatio = (float) Gdx.graphics.getHeight() / (float) Gdx.graphics.getWidth();
        assetManager = new AssetManager();
        loadingScreen = new LoadingScreen(this);
        setScreen(loadingScreen);
    }

    @Override
    public void dispose() {
        assetManager.dispose();
        if (loadingScreen != null)
            loadingScreen.dispose();
        if (menuScreen != null)
            menuScreen.dispose();
        if (gameScreen != null)
            gameScreen.dispose();
    }
}