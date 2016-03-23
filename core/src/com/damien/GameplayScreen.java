package com.damien;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Iterator;

public class GameplayScreen implements Screen {

    private static final float WORLD_WIDTH = 800;
    private static final float WORLD_HEIGHT = 600;
    private static final float CELL_SIZE = 25;

    private ShapeRenderer shapeRenderer;
    private Viewport viewport;
    private OrthographicCamera camera;
    private SpriteBatch spriteBatch;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private final MyGdxGame game;

    Area[][] overWorld = new Area[10][10];
    int playerRow = 4;
    int playerCol = 4;
    Area currentArea;

    public static Array<Bullet> playerBullets = new Array<Bullet>();

    private TiledMap tiledMap;

    private Player player;

    public GameplayScreen(MyGdxGame game) {
        this.game = game;
        makeOverWorld();
    }

    private void makeOverWorld() {
        overWorld[4][4] = new Area((TiledMap) game.getAssetManager().get("map1.tmx"));
        overWorld[3][4] = new Area((TiledMap) game.getAssetManager().get("map2.tmx"));
        overWorld[4][3] = new Area((TiledMap) game.getAssetManager().get("map2.tmx"));
        overWorld[4][5] = new Area((TiledMap) game.getAssetManager().get("map2.tmx"));
        currentArea = overWorld[4][4];
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        camera.update();

        viewport = new FitViewport(WORLD_WIDTH,WORLD_HEIGHT,camera);
        viewport.apply(true);
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        spriteBatch = new SpriteBatch();
        tiledMap = game.getAssetManager().get("firstMap.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(currentArea.getMap(), spriteBatch);
        tiledMapRenderer.setView(camera);

        Texture temp = game.getAssetManager().get("guy.png");
        player = new Player(temp);
    }

    @Override
    public void render(float delta) {
        update(delta);
        clearScreen();
        draw();
        drawDebug();
        //System.out.println(whichCellsDoesPlayerCover());
    }

    private void drawDebug() {
        shapeRenderer.setProjectionMatrix(camera.projection);
        shapeRenderer.setTransformMatrix(camera.view);
        shapeRenderer.begin();
        player.drawDebug(shapeRenderer);

        //for every bullet
        for(Bullet b: playerBullets) {
            b.drawDebug(shapeRenderer);
        }

        shapeRenderer.end();
    }

    private void draw() {
        spriteBatch.setProjectionMatrix(camera.projection);
        spriteBatch.setTransformMatrix(camera.view);
        tiledMapRenderer.render();
        spriteBatch.begin();
        player.draw(spriteBatch);
        spriteBatch.end();
    }

    private void stopPlayerLeavingTheScreen() {
        //bottom of the screen
        if(player.getY() < 0) {
            player.setPosition(player.getX(), WORLD_HEIGHT - player.HEIGHT-100);//put to top of screen
            playerRow++;//move player row
            currentArea = overWorld[playerRow][playerCol];//grab the new Area
            tiledMapRenderer.setMap(currentArea.getMap());//change to draw the new map
            playerBullets.clear();
        }
        //top of the screen
        if(player.getY() >= WORLD_HEIGHT-100) {
            player.setPosition(player.getX(), 0);//put to bottom of screen
            playerRow--;//move player row
            currentArea = overWorld[playerRow][playerCol];//grab the new Area
            tiledMapRenderer.setMap(currentArea.getMap());//change to draw the new map
            playerBullets.clear();
        }
        //left of the screen
        if(player.getX() < 0) {
            player.setPosition(WORLD_WIDTH-player.WIDTH-10, player.getY());
            playerCol--;
            currentArea = overWorld[playerRow][playerCol];//grab the new Area
            tiledMapRenderer.setMap(currentArea.getMap());//change to draw the new map
            playerBullets.clear();
        }
        if(player.getX() + Player.WIDTH > WORLD_WIDTH) {
            player.setPosition(10, player.getY());
            playerCol++;
            currentArea = overWorld[playerRow][playerCol];//grab the new Area
            tiledMapRenderer.setMap(currentArea.getMap());//change to draw the new map
            playerBullets.clear();
        }
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(1,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void update(float delta) {
        player.update(delta);
        stopPlayerLeavingTheScreen();
        handlePlayerCollision();
        updateBullets(delta);
    }

    private void updateBullets(float delta) {
        for(Bullet b: playerBullets) {
            b.update(delta);
        }
    }

    public Array<CollisionCell> whichCellsDoesPlayerCover() {
        float x = player.getX();
        float y = player.getY();
        Array<CollisionCell> cellsCovered = new Array<CollisionCell>();
        float cellRow = x / CELL_SIZE;
        float cellCol = y / CELL_SIZE;

        int bottomLeftCellRow = MathUtils.floor(cellRow);
        int bottomLeftCellCol = MathUtils.floor(cellCol);

        TiledMapTileLayer tiledMapTileLayer = (TiledMapTileLayer)currentArea.getMap().getLayers().get(0);

        cellsCovered.add(new CollisionCell(tiledMapTileLayer.getCell(bottomLeftCellRow,
                bottomLeftCellCol), bottomLeftCellRow, bottomLeftCellCol));

        if(cellRow % 1 != 0 && cellCol % 1 != 0) {
            int topRightCellRow = bottomLeftCellRow + 1;
            int topRightCellCol = bottomLeftCellCol + 1;
            cellsCovered.add(new CollisionCell(tiledMapTileLayer.getCell(topRightCellRow,
                    topRightCellCol), topRightCellRow, topRightCellCol));
        }
        if(cellRow % 1 != 0) {
            int bottomRightCellRow = bottomLeftCellRow + 1;
            int bottomRightCellCol = bottomLeftCellCol;
            cellsCovered.add(new CollisionCell(tiledMapTileLayer.getCell(bottomRightCellRow,
                    bottomRightCellCol), bottomRightCellRow, bottomRightCellCol));

        }
        if(cellCol % 1 != 0) {
            int topLeftCellRow = bottomLeftCellRow;
            int topLeftCellCol = bottomLeftCellCol + 1;
            cellsCovered.add(new CollisionCell(tiledMapTileLayer.getCell(topLeftCellRow,
                    topLeftCellCol), topLeftCellRow, topLeftCellCol));
        }

        return cellsCovered;
    }

    private Array<CollisionCell> filterOutNonCollisionCells(Array<CollisionCell> cells) {
        for(Iterator<CollisionCell> iter = cells.iterator(); iter.hasNext();) {
            CollisionCell collisionCell = iter.next();

            if(collisionCell.isEmpty()) {
                iter.remove();
            }
            if(collisionCell.getId() == 1) {
                iter.remove();
            }
            if(collisionCell.getId() == 2) {
                iter.remove();
            }
        }

        return cells;
    }

    public void handlePlayerCollision() {
        Array<CollisionCell> playerCells = whichCellsDoesPlayerCover();
        playerCells = filterOutNonCollisionCells(playerCells);
        for(CollisionCell cell: playerCells) {
            float cellLevelX = cell.getCellRow() * CELL_SIZE;
            float cellLevelY = cell.getCellCol() * CELL_SIZE;
            Rectangle intersection = new Rectangle();
            Intersector.intersectRectangles(player.getCollisionRectangle(),
                    new Rectangle(cellLevelX,cellLevelY,CELL_SIZE,CELL_SIZE),
                    intersection);
            if(intersection.getHeight() < intersection.getWidth()) {

                if(intersection.getY() == player.getY()) {
                    player.setPosition(player.getX(), intersection.getY() + intersection.getHeight());
                }
                if(intersection.getY() > player.getY()) {
                    player.setPosition(player.getX(), intersection.getY() - player.HEIGHT);
                }
            }
            else if (intersection.getWidth() < intersection.getHeight()) {
                if(intersection.getX() == player.getX()) {
                    player.setPosition(intersection.getX() + intersection.getWidth(),
                            player.getY());
                }
                if(intersection.getX() > player.getX()) {
                    player.setPosition(intersection.getX() - player.WIDTH,
                            player.getY());
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width,height);
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

    @Override
    public void dispose() {

    }
}