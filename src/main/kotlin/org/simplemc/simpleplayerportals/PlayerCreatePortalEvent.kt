package org.simplemc.simpleplayerportals

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class PlayerCreatePortalEvent(
    who: Player,
    clickedBlock: Block,
    clickedFace: BlockFace
) : PlayerInteractEvent(who, Action.RIGHT_CLICK_BLOCK, ItemStack(Material.COMPASS), clickedBlock, clickedFace, EquipmentSlot.HAND)
