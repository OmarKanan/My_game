package com.mygdx.game.Resources;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.mygdx.game.Main.GamePrototype;

import java.util.HashMap;

public class Assets {

    private GamePrototype game;
    private HashMap<String, String> textures;
    private HashMap<String, String> sounds;
    private HashMap<String, String> musics;
    private HashMap<Class, HashMap<String, String>> assets;

    public Assets(GamePrototype game) {
        this.game = game;
        assets = new HashMap<Class, HashMap<String, String>>(3);

        textures = new HashMap<String, String>();
        sounds = new HashMap<String, String>();
        musics = new HashMap<String, String>();

        assets.put(Texture.class, textures);
        assets.put(Sound.class, sounds);
        assets.put(Music.class, musics);

        fillTextures();
        fillSounds();
        fillMusics();
    }

    public HashMap<Class, HashMap<String, String>> getAssets() {
        return assets;
    }

    public void loadAssets() {
        for (Class clss : assets.keySet())
            for (String string : assets.get(clss).keySet())
                game.getAssetManager().load(assets.get(clss).get(string), clss);
    }

    private void fillTextures() {
        textures.put("arrow", "arrow.png");
        textures.put("background", "background.png");
        textures.put("ball", "ball.png");
        textures.put("barrier", "barrier.png");
        textures.put("border", "border.png");
        textures.put("button", "button.png");
        textures.put("hold", "hold.png");
        textures.put("teleporter", "teleporter.png");
        textures.put("trajectory", "trajectory.png");
    }

    private void fillSounds() {

    }

    private void fillMusics() {

    }

}
