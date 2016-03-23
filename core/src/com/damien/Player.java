package com.damien;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class Player {
    private static final float MAX_X_SPEED = 6;
    private static final float MAX_Y_SPEED = 6;
    public static final int WIDTH = 25;
    public static final int HEIGHT = 25;
    private static final float MAX_JUMP_DISTANCE = 4 * HEIGHT;
    private final Rectangle collisionRectangle = new Rectangle(0,0,WIDTH,HEIGHT);
    private float x = 10;
    private float y = 33;
    private float xSpeed = 0;
    private float ySpeed = 0;
    private boolean blockJump = false;
    private float jumpYDistance = 0;

    private float animationTimer = 0;

    public Player(Texture texture) {
    }


    public void update(float delta) {
        animationTimer += delta;
        Input input = Gdx.input;

        //x movement
        if(input.isKeyPressed(Input.Keys.RIGHT)) {
            xSpeed = MAX_X_SPEED;
        }
        else if(input.isKeyPressed(Input.Keys.LEFT)) {
            xSpeed = -MAX_X_SPEED;
        }
        else {
            xSpeed = 0;
        }

        //y movement
        if(input.isKeyPressed(Input.Keys.UP)) {
            ySpeed = MAX_Y_SPEED;
        }
        else if(input.isKeyPressed(Input.Keys.DOWN)) {
            ySpeed = -MAX_Y_SPEED;
        }
        else {
            ySpeed = 0;
        }

        x += xSpeed;
        y += ySpeed;
        updateCollisionRectangle();

        //System.out.println("Speed: " + xSpeed + "," + ySpeed);

        if(input.isKeyJustPressed(Input.Keys.S)) {
            if(xSpeed == 0 && ySpeed == 0) {
                xSpeed = (float) (-5 + Math.random() * 11);
                ySpeed = (float) (-5 + Math.random() * 11);
            }
            GameplayScreen.playerBullets.add(new Bullet(x,y,xSpeed*3,ySpeed*3));
        }
    }

    public void landed() {
        blockJump = false;
        jumpYDistance = 0;
        ySpeed = 0;
    }

    public void draw(SpriteBatch batch) {

    }

    public void drawDebug(ShapeRenderer shapeRenderer) {
        shapeRenderer.rect(collisionRectangle.x, collisionRectangle.y, collisionRectangle.width,
                collisionRectangle.height);
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        updateCollisionRectangle();
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    private void updateCollisionRectangle() {
        collisionRectangle.setPosition(x,y);
    }

    public Rectangle getCollisionRectangle() {
        return collisionRectangle;
    }

    public float getxSpeed() {
        return xSpeed;
    }

    public float getySpeed() {
        return ySpeed;
    }
}