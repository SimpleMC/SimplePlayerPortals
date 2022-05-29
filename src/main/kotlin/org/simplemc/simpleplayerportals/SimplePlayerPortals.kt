package org.simplemc.simpleplayerportals

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.PortalCreateEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.BoundingBox

/**
 * KotlinPluginTemplate plugin
 */
class SimplePlayerPortals : JavaPlugin(), Listener {

    companion object {
        private val cartesianBlockFaces = BlockFace.values().filter(BlockFace::isCartesian)
    }

    override fun onEnable() {
        // ensure config file exists
        saveDefaultConfig()

        server.pluginManager.registerEvents(this, this)

        logger.info("${description.name} version ${description.version} enabled!")
    }

    @EventHandler(ignoreCancelled = true)
    private fun blockInteract(event: PlayerInteractEvent) {
        if (event.hand == EquipmentSlot.HAND &&
            event.action == Action.RIGHT_CLICK_BLOCK &&
            event.item.isValidPortalItem() &&
            event.player.hasPermission("simpleplayerportals.create")
        ) {
        }

        event.clickedBlock!!.getPortalBlocks(event.blockFace)
    }

    @EventHandler(ignoreCancelled = true)
    private fun createPortalEvent(event: PortalCreateEvent) {
        event.entity?.sendMessage(event.blocks.joinToString { it.type.name })
    }

    private fun ItemStack?.isValidPortalItem(): Boolean =
        this != null && type == Material.COMPASS && itemMeta.hasDisplayName()

    // private fun Block.isValidPortalFrameBlock(): Boolean {
    //     val neighbors = getNeighbors()
    //     val adjacentAirFares = neighbors.filter { (_, block) -> block.type == Material.AIR }.keys
    //
    //     if (airNeighbors.isNotEmpty()) {
    //
    //     }
    //
    //     return false
    // }

    private fun Block.getPortalBlocks(
        facing: BlockFace,
        portalMaterial: Material = Material.EMERALD_BLOCK,
        minPortalMaterialInFrame: Int = 2
    ): Collection<Block>? {
        val airBlock = getRelative(facing).takeUnless { it.isSolid } ?: return null
        val oppositeEdgeBlock = airBlock.nextSolidBlockLocation(facing, 23) ?: return null
        val portalBoundingBox = BoundingBox.of(location, oppositeEdgeBlock)

        val possibleAdjacentEdgeBlocks = when (facing) {
            BlockFace.UP,
            BlockFace.DOWN ->
                mapOf(
                    airBlock.nextSolidBlockLocation(BlockFace.EAST, 22) to airBlock.nextSolidBlockLocation(BlockFace.WEST, 22),
                    airBlock.nextSolidBlockLocation(BlockFace.NORTH, 22) to airBlock.nextSolidBlockLocation(BlockFace.SOUTH, 22)
                )
            BlockFace.EAST,
            BlockFace.WEST,
            BlockFace.NORTH,
            BlockFace.SOUTH ->
                mapOf(airBlock.nextSolidBlockLocation(BlockFace.UP, 22) to airBlock.nextSolidBlockLocation(BlockFace.DOWN, 22))
            else -> return null // Maybe should be an IllegalArgumentException...non-cartesian face
        }.filterNot { it.component1() == null || it.component2() == null }.takeIf { it.isNotEmpty() } ?: return null

        val possiblePortals = possibleAdjacentEdgeBlocks
            .map { portalBoundingBox.clone().union(it.component1()!!).union(it.component2()!!) }
            .filter { it.blockHeight >= 5 && (it.blockWidthX >= 4 || it.blockWidthZ >= 4) }
            .map { world.blocksIn(it). }

        logger.info(possiblePortals.joinToString())

        return null
    }

    private fun Block.nextSolidBlockLocation(facingDirection: BlockFace, maxBlockDistance: Int = 22): Location? =
        if (maxBlockDistance > 0 ) {
            val nextBlock = getRelative(facingDirection)
            if (nextBlock.isSolid) {
                nextBlock.location
            } else {
                nextBlock.nextSolidBlockLocation(facingDirection, maxBlockDistance - 1)
            }
        } else {
            null
        }

    private fun Block.getNeighbors(): Map<BlockFace, Block> = cartesianBlockFaces.associateWith { face -> this.getRelative(face) }

    private fun World.blocksIn(boundingBox: BoundingBox) =
        (Location.locToBlock(boundingBox.minX) .. Location.locToBlock(boundingBox.maxX)).flatMap { x ->
            (Location.locToBlock(boundingBox.minY) .. Location.locToBlock(boundingBox.maxY)).flatMap { y ->
                (Location.locToBlock(boundingBox.minZ) .. Location.locToBlock(boundingBox.maxZ)).map { z ->
                    getBlockAt(x, y, z).type
                }
            }
        }

    private val BoundingBox.blockHeight: Int
        get() = height.toInt() + 1
    private val BoundingBox.blockWidthX: Int
        get() = widthX.toInt() + 1
    private val BoundingBox.blockWidthZ: Int
        get() = widthZ.toInt() + 1

    override fun onDisable() {
        logger.info("${description.name} disabled.")
    }
}
