package b100.shaders.lightsources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.world.chunk.provider.ChunkProviderStatic;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.phys.Vec3d;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.Chunk;
import net.minecraft.core.world.chunk.ChunkCoordinate;
import net.minecraft.core.world.chunk.ChunkPosition;
import net.minecraft.core.world.chunk.provider.IChunkProvider;

class ScanThread implements Runnable {

	public final LightSources lightSources;

	public ScanThread(LightSources lightSources) {
		this.lightSources = lightSources;
	}

	private final Map<ChunkPosition, LightSource> lightSourceMap = new HashMap<>();
	
	private final List<LightSource> removedLightSources = new ArrayList<>();
	private final List<LightSource> addedLightSources = new ArrayList<>();
	
	private final List<ChunkPosition> positionList = new ArrayList<>();
	
	private Map<ChunkCoordinate, Chunk> chunkCache = new HashMap<>();
	
	@Override
	public void run() {
		log("Scan thread started!");
		
		while(true) {
			if(this.lightSources.world == null || this.lightSources.player == null) {
				log("World is null, sleep!");
				try {
					Thread.sleep(3000L);
				}catch (Exception e) {
					log("interrupted");
				}
				
				lightSourceMap.clear();
				removedLightSources.clear();
				addedLightSources.clear();
				positionList.clear();
				chunkCache.clear();
				
				continue;
			}
			
			update(this.lightSources.world, this.lightSources.player);
		}
	}
	
	public void update(World world, EntityPlayer player) {
		chunkCache.clear();
		
		System.out.println("Update!");
//		log("Check for removed light sources");
		
		positionList.clear();
		positionList.addAll(lightSourceMap.keySet());
		
		for(int i=0; i < positionList.size(); i++) {
			ChunkPosition pos = positionList.get(i);
			
			int x = pos.x;
			int y = pos.y;
			int z = pos.z;
			if(!world.isBlockLoaded(x, y, z)) {
				lightSourceRemoved(pos);
				continue;
			}
			
			int id = world.getBlockId(x, y, z);
			LightSource lightSource = this.lightSourceMap.get(pos);
			
			if(lightSource.id != id) {
				lightSourceRemoved(pos);
				continue;
			}
		}
		
//		log("Scan for light sources");
		
		int x0 = MathHelper.floor_double(player.x);
		int y0 = MathHelper.floor_double(player.y);
		int z0 = MathHelper.floor_double(player.z);
		
		int minX = x0 - this.lightSources.scanRadius;
		int minY = Math.max(0, y0 - this.lightSources.scanRadius);
		int minZ = z0 - this.lightSources.scanRadius;
		
		int maxX = x0 + this.lightSources.scanRadius;
		int maxY = Math.min(255, y0 + this.lightSources.scanRadius);
		int maxZ = z0 + this.lightSources.scanRadius;
		
		for(int x = minX; x <= maxX; x++) {
			for(int y = minY; y <= maxY; y++) {
				for(int z = minZ; z <= maxZ; z++) {
					if(!world.isBlockLoaded(x, y, z)) {
						continue;
					}
//					int id = world.getBlockId(x, y, z);
					int id = getBlock(world, x, y, z);
					Vec3d color = null;
					
					synchronized(this.lightSources.blockColors) {
						color = this.lightSources.blockColors.get(id);
					}
					
					if(color == null) {
						continue;
					}
					
					ChunkPosition position = new ChunkPosition(x, y, z);
					LightSource lightSource = lightSourceMap.get(position);
					
					if(lightSource == null) {
						lightSource = new LightSource();
						
						lightSource.x = x;
						lightSource.y = y;
						lightSource.z = z;
						
						lightSource.red = (float) color.xCoord;
						lightSource.green = (float) color.yCoord;
						lightSource.blue = (float) color.zCoord;
						
						lightSource.id = id;
						
						lightSourceAdded(position, lightSource);
					}
				}
			}
		}
		
//		log("Send to render thread");
		
		boolean changed = false;
		
		if(addedLightSources.size() > 0) {
			changed = true;
			synchronized(lightSources.lightSourcesToAdd) {
				lightSources.lightSourcesToAdd.addAll(addedLightSources);
			}
			addedLightSources.clear();
		}

		if(removedLightSources.size() > 0) {
			changed = true;
			synchronized(lightSources.lightSourcesToRemove) {
				lightSources.lightSourcesToRemove.addAll(removedLightSources);
			}
			removedLightSources.clear();
		}
		
		lightSources.lightSourcesUpdated = changed;
		
		try {
			Thread.sleep(10L);
		}catch (Exception e) {
			log("sleep");
		}
		
		chunkCache.clear();
	}
	
	public int getBlock(World world, int x, int y, int z) {
		IChunkProvider chunkProvider = world.getChunkProvider();
		
		ChunkProviderStatic chunkProviderStatic = (ChunkProviderStatic) chunkProvider;
		
		int chunkX = x >> 4;
		int chunkZ = z >> 4;
		
		ChunkCoordinate chunkCoordinate = new ChunkCoordinate(chunkX, chunkZ);
		Chunk chunk = chunkCache.get(chunkCoordinate);
		
		if(chunk == null) {
			for(int i=0; i < chunkProviderStatic.chunks.length; i++) {
				Chunk chunk1 = chunkProviderStatic.chunks[i];
				
				if(chunk1 != null && chunk1.isAtLocation(chunkX, chunkZ)) {
					chunk = chunk1;
					break;
				}
			}
			
			if(chunk == null) {
				return 0;
			}
			
			chunkCache.put(chunkCoordinate, chunk);
		}
		
		
		int inChunkX = x & 0xF;
		int inChunkZ = z & 0xF;
		return chunk.getBlockID(inChunkX, y, inChunkZ);
	}
	
	private void lightSourceRemoved(ChunkPosition position) {
		LightSource removed = lightSourceMap.remove(position);
		if(removed == null) {
			throw new RuntimeException("Not removed!");
		}
		
		log("Light source removed: " + position.x + ", " + position.y + ", " + position.z);
		removedLightSources.add(removed);
	}
	
	private void lightSourceAdded(ChunkPosition position, LightSource lightSource) {
		log("Light source added: " + position.x + ", " + position.y + ", " + position.z);
		addedLightSources.add(lightSource);
		lightSourceMap.put(position, lightSource);
	}
	
	public void log(String string) {
		System.out.print("[Scan Thread] " + string + "\n");
	}
	
}