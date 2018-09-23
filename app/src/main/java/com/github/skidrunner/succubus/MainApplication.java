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
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import de.lessvoid.nifty.Nifty;
import java.util.concurrent.Callable;
import com.jme3.scene.shape.Box;

public class MainApplication extends SimpleApplication {

	public static final ColorRGBA Gray = new ColorRGBA(0x22 / 255.0f, 0x22 / 255.0f, 0x22 / 255.0f, 1.0f);
	public static final ColorRGBA Yellow = new ColorRGBA(0xFF / 255.0f, 0xCC / 255.0f, 0x00 / 255.0f, 1.0f);

	@Override
	public void simpleInitApp() {
		// Create jMonkeyEngine3 Splash Screen
		createSplashScreen();
		
		new Thread(new Runnable() {
				@Override
				public void run() {
					destroyFlyByCam();
					createScene();
					initNiftyGui();
					//destroySplash();
				}
			}).start();

		cam.setLocation(new Vector3f(1, 1, 2).normalizeLocal().multLocal(8));
		cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

		/*
		 flyCam.setEnabled(false);


		 BulletAppState bulletAppState = new BulletAppState();
		 getStateManager().attach(bulletAppState);
		 PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();
		 */
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

	private void destroyFlyByCam() {
		try {
			enqueue(new Callable<Void>() {
					@Override
					public Void call() {
						FlyCamAppState flyCamAppState = getStateManager().getState(FlyCamAppState.class);
						if(flyCamAppState != null) {
							getStateManager().detach(flyCamAppState);
						}
						return null;
					}
				}).get();
		} catch(Exception e) {
			//throw new RuntimeException(e);
		}
	}

	private void initNiftyGui() {
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
		try {
			enqueue(new Callable<Void>() {
					@Override
					public Void call() {
						getGuiViewPort().addProcessor(niftyProcessor);
						return null;
					}
				}).get();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		destroySplash();
		try {
			enqueue(new Callable<Void>() {
				@Override
				public Void call() {
					nifty.gotoScreen("main");
					return null;
				}
				}).get();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void destroySplash() {
		try {
			enqueue(new Callable<Void>() {
					@Override
					public Void call() {
						Spatial spatial = guiNode.getChild("Splash Screen");
						if(spatial != null) {
							spatial.removeFromParent();
						}
						return null;
					}
				}).get();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void createScene() {
		final Node scene = new Node("Scene");
		
		DirectionalLight light = new DirectionalLight();
		light.setColor(new ColorRGBA(1.0f, 0.96f, 0.93f, 1.0f));
		scene.addLight(light);

		Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		material.setBoolean("UseMaterialColors", true);
		material.setColor("Diffuse", new ColorRGBA(0, 0.5f, 0, 1));
		
		Geometry geometry = new Geometry("Geometry0", new Box(128, 0.5f, 128));
		geometry.setMaterial(material); 
		geometry.setLocalTranslation(0, -0.5f, 0);
		scene.attachChild(geometry);
		
		scene.attachChild(createSky());
		try {
			enqueue(new Callable<Void>() {
					@Override
					public Void call() {
						rootNode.attachChild(scene);
						return null;
					}
				}).get();
		} catch(Exception e) {
			//throw new RuntimeException(e);
		}
		
	}
	
	private Spatial createSky() {
		Mesh sphereMesh = new Sphere(18, 36, 16, true, true);
		Geometry sky = new Geometry("Sky", sphereMesh);
        sky.setQueueBucket(RenderQueue.Bucket.Sky);
        sky.setCullHint(Spatial.CullHint.Never);
        sky.setModelBound(new BoundingSphere(Float.POSITIVE_INFINITY, Vector3f.ZERO));
		sky.setMaterial(assetManager.loadMaterial("Materials/DefaultSky.j3m"));
        return sky;
	}
}
