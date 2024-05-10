package b100.shaders.lightsources;

import static org.lwjgl.opengl.GL20.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import b100.json.element.JsonElement;
import b100.json.element.JsonObject;
import b100.shaders.Shader;
import b100.shaders.ShaderMod;
import b100.shaders.ShaderRenderer;
import net.minecraft.client.entity.player.EntityPlayerSP;
import net.minecraft.core.block.Block;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.phys.Vec3d;
import net.minecraft.core.world.World;

public class LightSources {

	public final ShaderRenderer renderer;
	
	public boolean enabled = false;
	public int lightSourceCount = 0;
	public FloatBuffer lightSourcePositions = null;
	public FloatBuffer lightSourceColors = null;
	public int maxLightSources;
	public int scanRadius;
	
	public double lastScanPosX;
	public double lastScanPosY;
	public double lastScanPosZ;
	
	private final List<LightSource> sortedLightSources = new ArrayList<>();
	private Comparator<LightSource> lightSourceSorter = (o1, o2) -> o1.distance - o2.distance;
	
	Map<Integer, Vec3d> blockColors = new HashMap<>();

	public volatile World world;
	public volatile EntityPlayerSP player;
	
	public boolean lightSourcesUpdated = false;
	public List<LightSource> lightSourcesToRemove = new ArrayList<>();
	public List<LightSource> lightSourcesToAdd = new ArrayList<>();
	
	private Thread scanThread;
	
	public LightSources(ShaderRenderer shaderRenderer) {
		this.renderer = shaderRenderer;
	}
	
	public void setup(JsonObject root) {
		this.world = null;
		
		JsonElement lightSources = root.get("lightSources");
		if(lightSources == null) {
			disable();
			ShaderMod.log("Light Sources Disabled");
			return;
		}

		this.enabled = true;
		this.world = renderer.mc.theWorld;
		this.player = renderer.mc.thePlayer;
		
		JsonObject obj = lightSources.getAsObject();
		
		maxLightSources = obj.getInt("maxLightSources");
		scanRadius = obj.getInt("scanRadius");

		ShaderMod.log("Light Sources Enabled");
		ShaderMod.log("Max Light Sources: " + maxLightSources);
		ShaderMod.log("Light Source Scan Radius: " + scanRadius);
		
		lightSourcePositions = ByteBuffer.allocateDirect(3 * 4 * maxLightSources).order(ByteOrder.nativeOrder()).asFloatBuffer();
		lightSourceColors = ByteBuffer.allocateDirect(3 * 4 * maxLightSources).order(ByteOrder.nativeOrder()).asFloatBuffer();
		
		blockColors.clear();
		blockColors.put(Block.torchCoal.id, Vec3d.createVectorHelper(1.0f, 1.0f, 1.0f));
		blockColors.put(Block.lanternFireflyRed.id, Vec3d.createVectorHelper(1.0f, 0.5f, 0.5f));
		blockColors.put(Block.lanternFireflyGreen.id, Vec3d.createVectorHelper(0.5f, 1.0f, 0.5f));
		blockColors.put(Block.lanternFireflyBlue.id, Vec3d.createVectorHelper(0.5f, 0.75f, 1.0f));
		blockColors.put(Block.lanternFireflyOrange.id, Vec3d.createVectorHelper(1.0f, 0.75f, 0.5f));
		
		if(scanThread == null) {
			scanThread = new Thread(new ScanThread(this));
			scanThread.setDaemon(true);
			scanThread.setName("Scan Thread");
			
			System.out.println("Starting Scan Thread");
			scanThread.start();
		}
		
		if(world != null && player != null) {
			updateLightSourceList();	
		}
	}
	
	public void update() {
		World currentWorld = renderer.mc.theWorld;
		EntityPlayerSP currentPlayer = renderer.mc.thePlayer;
		
		if(currentWorld != world || currentPlayer != player) {
			ShaderMod.log("World or player changed");
			
			world = currentWorld;
			player = currentPlayer;
			
			if(world != null && player != null) {
				scanThread.interrupt();	
			}
		}
		
		if(lightSourcesUpdated) {
			lightSourcesUpdated = false;
			synchronized(lightSourcesToAdd) {
				for(int i=0; i < lightSourcesToAdd.size(); i++) {
					this.sortedLightSources.add(lightSourcesToAdd.get(i));
				}
				lightSourcesToAdd.clear();
			}
			
			synchronized(lightSourcesToRemove) {
				for(int i=0; i < lightSourcesToRemove.size(); i++) {
					LightSource lightSource = lightSourcesToRemove.get(i);
					boolean removed = this.sortedLightSources.remove(lightSource);
					if(!removed) {
						throw new RuntimeException("Light source '" + lightSource + "' not removed!");
					}
				}
				lightSourcesToRemove.clear();
			}
			
			updateLightSourceList();
		}
	}
	
	public void updateLightSourceList() {
		int x = MathHelper.floor_double(player.x);
		int y = MathHelper.floor_double(player.y);
		int z = MathHelper.floor_double(player.z);
		
		for(int i=0; i < sortedLightSources.size(); i++) {
			LightSource lightSource = sortedLightSources.get(i);
			
			int dx = x - lightSource.x;
			int dy = y - lightSource.y;
			int dz = z - lightSource.z;
			
			lightSource.distance = dx * dx + dy * dy + dz * dz;
		}
		
		sortedLightSources.sort(lightSourceSorter);
		
		lightSourcePositions.clear();
		lightSourceColors.clear();
		
		int count = Math.min(maxLightSources, sortedLightSources.size());
		for(int i=0; i < count; i++) {
			LightSource lightSource = sortedLightSources.get(i);
			
			lightSourcePositions.put(lightSource.x + 0.5f);
			lightSourcePositions.put(lightSource.y + 0.5f);
			lightSourcePositions.put(lightSource.z + 0.5f);
			
			lightSourceColors.put(lightSource.red);
			lightSourceColors.put(lightSource.green);
			lightSourceColors.put(lightSource.blue);
		}
		
		lightSourcePositions.flip();
		lightSourceColors.flip();
		lightSourceCount = count;
	}
	
	public void delete() {
		disable();
	}
	
	public void disable() {
		enabled = false;
		world = null;
		player = null;
		
		sortedLightSources.clear();
		lightSourcesToAdd.clear();
		lightSourcesToRemove.clear();
	}
	
	/*
	public void scanForLightSources(World world) {
		int x0 = MathHelper.floor_double(renderer.mc.thePlayer.x);
		int y0 = MathHelper.floor_double(renderer.mc.thePlayer.y);
		int z0 = MathHelper.floor_double(renderer.mc.thePlayer.z);
		
		lightSources.clear();
		
		for(int i = -scanRadius; i <= scanRadius; i++) {
			for(int j = -scanRadius; j <= scanRadius; j++) {
				for(int k = -scanRadius; k <= scanRadius; k++) {
					int x1 = x0 + i;
					int y1 = y0 + j;
					int z1 = z0 + k;
					
					int id = world.getBlockId(x1, y1, z1);
					
					Vec3d color = blockColors.get(id);
					if(color != null) {
						LightSource lightSource = new LightSource();
						lightSource.x = x1;
						lightSource.y = y1;
						lightSource.z = z1;
						lightSource.red = (float) color.xCoord;
						lightSource.green = (float) color.yCoord;
						lightSource.blue = (float) color.zCoord;
						lightSource.distance = i * i + j * j + k * k;
						lightSources.add(lightSource);
					}
				}
			}
		}
		
	}
	*/
	
	public void setUniforms(Shader shader) {
		lightSourcePositions.position(0);
		lightSourcePositions.limit(lightSourceCount * 3);
		glUniform3(shader.getUniform("lightSourcePositions"), lightSourcePositions);
		
		lightSourceColors.position(0);
		lightSourceColors.limit(lightSourceCount * 3);
		glUniform3(shader.getUniform("lightSourceColors"), lightSourceColors);
		
		glUniform1i(shader.getUniform("lightSourceCount"), lightSourceCount);
	}

}
