package me.daddychurchill.CityWorld.Plats;

import java.util.Random;

import me.daddychurchill.CityWorld.PlatMaps.PlatMap;
import me.daddychurchill.CityWorld.Support.ByteChunk;
import me.daddychurchill.CityWorld.Support.Direction.Ladder;
import me.daddychurchill.CityWorld.Support.Direction.TrapDoor;
import me.daddychurchill.CityWorld.Support.SurroundingParks;
import me.daddychurchill.CityWorld.Support.RealChunk;

import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;

public class PlatPark extends PlatLot {

	protected static long connectedkeyForParks = 0;
	
	protected final static int cisternDepth = PlatMap.FloorHeight * 4;
	protected final static int maxWaterDepth = PlatMap.FloorHeight * 2;
	protected final static int groundDepth = 2;
	
	protected final static byte cisternId = (byte) Material.IRON_BLOCK.getId();
	protected final static byte grassId = (byte) Material.GRASS.getId();
	protected final static byte dirtId = (byte) Material.DIRT.getId();
	protected final static byte waterId = (byte) Material.WATER.getId();
	protected final static byte fenceId = (byte) Material.FENCE.getId();
	protected final static byte columnId = (byte) Material.SMOOTH_BRICK.getId();
	protected final static byte pavementId = (byte) Material.SANDSTONE.getId();
	
	protected final static Material ledgeMaterial = Material.IRON_BLOCK;
	
	//TODO NW/SE quarter partial circle sidewalks
	//TODO pond inside of circle sidewalks instead of tree
	//TODO park benches
	
	private boolean circleSidewalk;
	
	public PlatPark(Random rand) {
		super(rand);
		
		// if the master key for paved roads isn't calculated then do it
		if (connectedkeyForParks == 0) {
			connectedkeyForParks = rand.nextLong();
		}

		// all parks are interconnected
		connectedkey = connectedkeyForParks;
		
		// pick a style
		circleSidewalk = rand.nextBoolean();
	}

	@Override
	public void generateChunk(PlatMap platmap, ByteChunk chunk, int platX, int platZ) {

		// starting with the bottom
		int lowestY = PlatMap.StreetLevel - cisternDepth + 1;
		int highestY = PlatMap.StreetLevel - groundDepth;
		generateBedrock(chunk, lowestY);
		chunk.setLayer(lowestY, cisternId);
		
		// fill with water
		lowestY++;
		chunk.setBlocks(0, ByteChunk.Width, lowestY, lowestY + maxWaterDepth, 0, ByteChunk.Width, waterId);
		
		// look around
		SurroundingParks neighbors = new SurroundingParks(platmap, platX, platZ);
		
		// outer columns and walls as needed
		if (neighbors.toWest()) {
			chunk.setBlocks(3, 5, lowestY, highestY, 0, 1, cisternId);
			chunk.setBlocks(11, 13, lowestY, highestY, 0, 1, cisternId);
		} else
			chunk.setBlocks(0, 16, lowestY, highestY + 1, 0, 1, cisternId);
		if (neighbors.toEast()) {
			chunk.setBlocks(3, 5, lowestY, highestY, 15, 16, cisternId);
			chunk.setBlocks(11, 13, lowestY, highestY, 15, 16, cisternId);
		} else
			chunk.setBlocks(0, 16, lowestY, highestY + 1, 15, 16, cisternId);
		if (neighbors.toSouth()) {
			chunk.setBlocks(0, 1, lowestY, highestY, 3, 5, cisternId);
			chunk.setBlocks(0, 1, lowestY, highestY, 11, 13, cisternId);
		} else
			chunk.setBlocks(0, 1, lowestY, highestY + 1, 0, 16, cisternId);
		if (neighbors.toNorth()) {
			chunk.setBlocks(15, 16, lowestY, highestY, 3, 5, cisternId);
			chunk.setBlocks(15, 16, lowestY, highestY, 11, 13, cisternId);
		} else
			chunk.setBlocks(15, 16, lowestY, highestY + 1, 0, 16, cisternId);
		
		// center columns
		chunk.setBlocks(7, 9, lowestY, highestY, 3, 5, cisternId);
		chunk.setBlocks(7, 9, lowestY, highestY, 11, 13, cisternId);
		chunk.setBlocks(3, 5, lowestY, highestY, 7, 9, cisternId);
		chunk.setBlocks(11, 13, lowestY, highestY, 7, 9, cisternId);
		
		// ceiling supports
		chunk.setBlocks(3, 5, highestY, highestY + 1, 0, 16, cisternId);
		chunk.setBlocks(11, 13, highestY, highestY + 1, 0, 16, cisternId);
		chunk.setBlocks(0, 16, highestY, highestY + 1, 3, 5, cisternId);
		chunk.setBlocks(0, 16, highestY, highestY + 1, 11, 13, cisternId);

		// top it off
		chunk.setLayer(highestY + 1, cisternId);
		chunk.setLayer(highestY + 2, dirtId);
		chunk.setLayer(highestY + 3, grassId);
		
		// surface features
		int surfaceY = PlatMap.StreetLevel + 2;
		if (!neighbors.toWest()) {
			chunk.setBlocks(0, 6, surfaceY, surfaceY + 1, 0, 1, fenceId);
			chunk.setBlocks(10, 16, surfaceY, surfaceY + 1, 0, 1, fenceId);
			chunk.setBlocks(6, surfaceY, surfaceY + 2, 0, columnId);
			chunk.setBlocks(9, surfaceY, surfaceY + 2, 0, columnId);
			chunk.setBlock(6, surfaceY, 1, columnId);
			chunk.setBlock(9, surfaceY, 1, columnId);
		}
		if (!neighbors.toEast()) {
			chunk.setBlocks(0, 6, surfaceY, surfaceY + 1, 15, 16, fenceId);
			chunk.setBlocks(10, 16, surfaceY, surfaceY + 1, 15, 16, fenceId);
			chunk.setBlocks(6, surfaceY, surfaceY + 2, 15, columnId);
			chunk.setBlocks(9, surfaceY, surfaceY + 2, 15, columnId);
			chunk.setBlock(6, surfaceY, 14, columnId);
			chunk.setBlock(9, surfaceY, 14, columnId);
		}
		if (!neighbors.toSouth()) {
			chunk.setBlocks(0, 1, surfaceY, surfaceY + 1, 0, 6, fenceId);
			chunk.setBlocks(0, 1, surfaceY, surfaceY + 1, 10, 16, fenceId);
			chunk.setBlocks(0, surfaceY, surfaceY + 2, 6, columnId);
			chunk.setBlocks(0, surfaceY, surfaceY + 2, 9, columnId);
			chunk.setBlock(1, surfaceY, 6, columnId);
			chunk.setBlock(1, surfaceY, 9, columnId);
		}
		if (!neighbors.toNorth()) {
			chunk.setBlocks(15, 16, surfaceY, surfaceY + 1, 0, 6, fenceId);
			chunk.setBlocks(15, 16, surfaceY, surfaceY + 1, 10, 16, fenceId);
			chunk.setBlocks(15, surfaceY, surfaceY + 2, 6, columnId);
			chunk.setBlocks(15, surfaceY, surfaceY + 2, 9, columnId);
			chunk.setBlock(14, surfaceY, 6, columnId);
			chunk.setBlock(14, surfaceY, 9, columnId);
		} 
		
		// draw the sidewalks
		if (circleSidewalk) {
			chunk.setBlocks(7, 9, surfaceY - 1, surfaceY, 0, 3, pavementId);
			chunk.setBlocks(7, 9, surfaceY - 1, surfaceY, 13, 16, pavementId);
			chunk.setBlocks(0, 3, surfaceY - 1, surfaceY, 7, 9, pavementId);
			chunk.setBlocks(13, 16, surfaceY - 1, surfaceY, 7, 9, pavementId);
			chunk.setCircle(8, 8, 4, surfaceY - 1, pavementId);
			chunk.setCircle(8, 8, 3, surfaceY - 1, pavementId);
		} else {
			chunk.setBlocks(7, 9, surfaceY - 1, surfaceY, 0, 8, pavementId);
			chunk.setBlocks(7, 9, surfaceY - 1, surfaceY, 8, 16, pavementId);
			chunk.setBlocks(0, 8, surfaceY - 1, surfaceY, 7, 9, pavementId);
			chunk.setBlocks(8, 16, surfaceY - 1, surfaceY, 7, 9, pavementId);
		}
	}
	
	@Override
	public void generateBlocks(PlatMap platmap, RealChunk chunk, int platX, int platZ) {
		int surfaceY = PlatMap.StreetLevel + 2;
		
		// way down?
		SurroundingParks neighbors = new SurroundingParks(platmap, platX, platZ);
		if (!neighbors.toWest()) {
			int lowestY = PlatMap.StreetLevel - cisternDepth + 1 + maxWaterDepth;
			chunk.setBlocks(4, 7, lowestY, lowestY + 1, 1, 2, ledgeMaterial);
			chunk.setLadder(5, lowestY + 1, surfaceY, 1, Ladder.WEST);
			chunk.setTrapDoor(5, surfaceY, 1, TrapDoor.NORTH);
		}
		
		// sprinkle some trees
		World world = platmap.theWorld;
		if (circleSidewalk) {
			world.generateTree(chunk.getBlockLocation(7, surfaceY, 7), 
					rand.nextBoolean() ? TreeType.BIG_TREE : TreeType.TALL_REDWOOD);
		} else {
			TreeType tree = rand.nextBoolean() ? TreeType.BIRCH : TreeType.TREE;
			world.generateTree(chunk.getBlockLocation(3, surfaceY, 3), tree);
			world.generateTree(chunk.getBlockLocation(12, surfaceY, 3), tree);
			world.generateTree(chunk.getBlockLocation(3, surfaceY, 12), tree);
			world.generateTree(chunk.getBlockLocation(12, surfaceY, 12), tree);
		}
	}

	public void makeConnected(Random rand, PlatBuilding relative) {
		super.makeConnected(rand, relative);
		
		// other bits
	}
	
}
