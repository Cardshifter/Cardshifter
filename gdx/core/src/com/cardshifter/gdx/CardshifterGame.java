package com.cardshifter.gdx;

import com.badlogic.gdx.*;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.cardshifter.gdx.screens.MenuScreen;

public class CardshifterGame extends Game {
	
    public static float STAGE_WIDTH;
    public static float STAGE_HEIGHT;
    private final CardshifterPlatform platform;
    private SpriteBatch batch;
    public Skin skin;
    private OrthographicCamera camera;

    public Stage stage;

    public CardshifterGame(CardshifterPlatform platform) {
        this.platform = platform;
    }

    @Override
	public void create () {
    	
		//configure screen size
		if (Gdx.app.getType() == ApplicationType.Desktop ||
			Gdx.app.getType() == ApplicationType.WebGL) {
	        CardshifterGame.STAGE_WIDTH = Gdx.graphics.getWidth();
	        CardshifterGame.STAGE_HEIGHT = Gdx.graphics.getHeight();
		} else {
			//good for phone shape
	        CardshifterGame.STAGE_WIDTH = 800; //Gdx.graphics.getWidth();
	        CardshifterGame.STAGE_HEIGHT = 480; //Gdx.graphics.getHeight();
		}
    	
		Gdx.app.setLogLevel(Application.LOG_INFO);
		if (Gdx.app.getType() == ApplicationType.WebGL) {
			Gdx.app.setLogLevel(Application.LOG_NONE);
		}
        
        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        camera = new OrthographicCamera(STAGE_WIDTH, STAGE_HEIGHT);
        camera.setToOrtho(false, STAGE_WIDTH, STAGE_HEIGHT);

        batch = new SpriteBatch();
        stage = new Stage(new StretchViewport(STAGE_WIDTH, STAGE_HEIGHT, camera), batch);

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);

        setScreen(new MenuScreen(this));

        inputMultiplexer.addProcessor(new InputAdapter(){
            private boolean debugMode;

            @Override
            public boolean keyTyped(char character) {
                if (character == 'd') {
                    debugMode = !debugMode;
                    for (Actor actor : stage.getActors()) {
                        if (actor instanceof Table) {
                            Table table = (Table) actor;
                            table.setDebug(debugMode, true);
                        }
                    }
                    return true;
                }
                if (character == 'x') {
                    Gdx.app.log("XY", Gdx.input.getX() + ", " + Gdx.input.getY());
                }
                return false;
            }
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        skin.dispose();
        stage.dispose();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        stage.getViewport().update(width, height, true);
    }

    @Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
		batch.begin();
        super.render();
		batch.end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    public CardshifterPlatform getPlatform() {
        return platform;
    }
}
