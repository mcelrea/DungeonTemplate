package com.damien;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;

public class Bullet {

    public static final float RADIUS = 10;
    private float xVel;
    private float yVel;
    private float x;
    private float y;
    private Circle collisionCircle;

    public Bullet(float x, float y, float xVel, float yVel) {
        this.x = x;
        this.y = y;
        this.xVel = xVel;
        this.yVel = yVel;
        collisionCircle = new Circle(x,y,RADIUS);
        System.out.println("Created Bullet with speed:" + xVel + "," + yVel);
    }

    public void update(float delta) {
        this.x += xVel * delta * 30;
        this.y += yVel * delta * 30;
        collisionCircle.setPosition(x,y);
    }

    public void drawDebug(ShapeRenderer shapeRenderer) {
        shapeRenderer.circle(collisionCircle.x, collisionCircle.y, RADIUS);
    }

}
