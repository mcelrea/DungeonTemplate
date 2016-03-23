package com.damien;

import com.badlogic.gdx.maps.tiled.TiledMap;

public class Area {
    private TiledMap map;

    public Area(TiledMap map) {
        this.map = map;
    }

    public TiledMap getMap() {
        return map;
    }

    public void setMap(TiledMap map) {
        this.map = map;
    }
}
