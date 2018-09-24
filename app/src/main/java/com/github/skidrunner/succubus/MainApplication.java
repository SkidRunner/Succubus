package com.github.skidrunner.succubus;

import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingSphere;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import de.lessvoid.nifty.Nifty;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.jme3.terrain.Terrain;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.NormalRecalcControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.texture.TextureCubeMap;

public class MainApplication extends SimpleApplication {

	public static final ColorRGBA Gray = new ColorRGBA(0x22 / 255.0f, 0x22 / 255.0f, 0x22 / 255.0f, 1.0f);
	public static final ColorRGBA Yellow = new ColorRGBA(0xFF / 255.0f, 0xCC / 255.0f, 0x00 / 255.0f, 1.0f);

    public static void main(String[] args) {
        MainApplication app = new MainApplication();
        app.start();
    }

	@Override
	public void simpleInitApp() {
		// Create jMonkeyEngine3 Splash Screen
		createSplashScreen();

		new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						//enqueue(destroyFlyByCam()).get();
						Nifty nifty = enqueue(initNiftyGui()).get();
						enqueue(destroySplash()).get();
						enqueue(createScene()).get();
						nifty.gotoScreen("main");
					} catch(InterruptedException | ExecutionException x) {
						throw new RuntimeException(x);
					}
				}
			}).start();

		cam.setLocation(new Vector3f(1, 1, 2).normalizeLocal().multLocal(8));
		cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
	}

	private void createSplashScreen() {
		Camera camera = getCamera();
		Node node;
		Material material;
		Geometry geometry;
		int width = camera.getWidth();
		int height = camera.getHeight();
		float size = (512.0f / 600.0f) * Math.min(width, height);

		node = new Node("Splash Screen");
		material = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		material.setColor("Color", Gray);
		geometry = new Geometry("Geometry.00", new Quad(width, height));
		geometry.setMaterial(material);
		node.attachChild(geometry);
		material = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		material.setTexture("ColorMap", getAssetManager().loadTexture("Interface/Logo/Monkey.png"));
		material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
		geometry = new Geometry("Geometry.01", new Quad(size, size));
		geometry.setMaterial(material);
		geometry.setLocalTranslation((width - size) / 2.0f, (height - size) / 2.0f, 0.0f);
		node.attachChild(geometry);
		guiNode.attachChild(node);
	}

	private Callable<Void> destroyFlyByCam() {
		return new Callable<Void>() {
			public Void call() {
				FlyCamAppState flyCamAppState = getStateManager().getState(FlyCamAppState.class);
				if(flyCamAppState != null) {
					getStateManager().detach(flyCamAppState);
				}
				return null;
			}
		};
	}

	private Callable<Nifty> initNiftyGui() {
		final NiftyJmeDisplay niftyProcessor = new NiftyJmeDisplay(
			getAssetManager(), getInputManager(),
			getAudioRenderer(), getGuiViewPort());
		final Nifty nifty = niftyProcessor.getNifty();

		float width = getCamera().getWidth();
		float height = getCamera().getHeight();

		if(width > height) {
			width = (width / height) * 600.0f;
			height = 600.0f;
		} else {
			height = (height / width) * 600.0f;
			width = 600.0f;
		}

		nifty.enableAutoScaling(((int)(width)), ((int)(height)));
		nifty.fromXml("Interface/Screens.xml", "start");
		return new Callable<Nifty>() {
			@Override
			public Nifty call() {
				getGuiViewPort().addProcessor(niftyProcessor);
				return nifty;
			}
		};
	}

	private Callable<Void> destroySplash() {
		return new Callable<Void>() {
			@Override
			public Void call() {
				Spatial spatial = guiNode.getChild("Splash Screen");
				if(spatial != null) {
					spatial.removeFromParent();
				}
				return null;
			}
		};
	}

	private Callable<Void> createScene() {
		final Node scene = new Node("Scene");
		
		Material defaultMaterial = assetManager.loadMaterial("Materials/Default.j3m");
		
		DirectionalLight light = new DirectionalLight();
		light.setDirection(new Vector3f(0.5f, -1.0f, 0.0f).normalizeLocal());
		light.setColor(new ColorRGBA(1.0f, 0.96f, 0.93f, 1.0f));
		scene.addLight(light);
		
		Texture skyWest = assetManager.loadTexture("Textures/Skybox/West.png");
		Texture skyEast = assetManager.loadTexture("Textures/Skybox/East.png");
		Texture skyNorth = assetManager.loadTexture("Textures/Skybox/North.png");
		Texture skySouth = assetManager.loadTexture("Textures/Skybox/South.png");
		Texture skyTop = assetManager.loadTexture("Textures/Skybox/Top.png");
		Texture skyBottom = assetManager.loadTexture("Textures/Skybox/Bottom.png");
		Geometry skybox = ((Geometry)(SkyFactory.createSky(assetManager, skyWest, skyEast, skyNorth, skySouth, skyTop, skyBottom)));
		TextureCubeMap skyTexture = ((TextureCubeMap)(skybox.getMaterial().getTextureParam("Texture").getTextureValue()));
		defaultMaterial.setTexture("EnvMap", skyTexture);
		defaultMaterial.setVector3("FresnelParams", new Vector3f(1.0f, 6.0f, 3.5f));
		skybox.setName("Skybox");
		scene.attachChild(skybox);
		
		TerrainQuad terrain = new TerrainQuad("Terrain", 65, 257, new float[256 * 256]);
		terrain.addControl(new TerrainLodControl(terrain, cam));
		terrain.addControl(new NormalRecalcControl(terrain));
		terrain.addControl(new RigidBodyControl(0));
		terrain.setMaterial(defaultMaterial);
		scene.attachChild(terrain);
		
		return new Callable<Void>() {
			@Override
			public Void call() {
				rootNode.attachChild(scene);
				return null;
			}
		};
	}
}
