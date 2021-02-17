package cr0s.warpdrive.event;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.forcefield.BlockForceField;
import cr0s.warpdrive.block.movement.TileEntityShipCore;
import cr0s.warpdrive.config.Dictionary;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.EnumGlobalRegionType;
import cr0s.warpdrive.data.GlobalRegion;
import cr0s.warpdrive.data.GlobalRegionManager;
import cr0s.warpdrive.data.OfflineAvatarManager;

import javax.annotation.Nonnull;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

public class PlayerHandler {
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onPlayerLoadFromFile(@Nonnull final PlayerEvent.LoadFromFile event) {
		OfflineAvatarManager.onPlayerLoggedIn(event.getEntityPlayer());
	}
	
	@SubscribeEvent
	public void onPlayerLoggedOut(@Nonnull final PlayerLoggedOutEvent event) {
		if (WarpDriveConfig.OFFLINE_AVATAR_ENABLE) {
			OfflineAvatarManager.onPlayerLoggedOut(event.player);
		}
	}
	
	@SubscribeEvent
	public void onBreakSpeed(@Nonnull final BreakSpeed event) {
		final BlockPos blockPos = event.getPos();
		
		// check for lock
		doCancelEventDuringJump(event, blockPos);
		if (event.isCanceled()) {
			return;
		}
		
		// check for maintenance and member access
		final IBlockState blockState = event.getState();
		doCancelEventForNonMembers(event, blockPos, blockState);
	}
	
	@SubscribeEvent
	public void onEntityItemPickup(@Nonnull final EntityItemPickupEvent event) {
		doCancelEventDuringJump(event, event.getItem().getPosition());
	}
	
	@SubscribeEvent
	public void onRightClickBlock(@Nonnull final RightClickBlock event) {
		final BlockPos blockPos = event.getPos();
		
		// check for lock
		doCancelEventDuringJump(event, blockPos);
		
		// check for maintenance and member access
		final IBlockState blockState = event.getWorld().getBlockState(blockPos);
		doCancelEventForNonMembers(event, blockPos, blockState);
	}
	
	private void doCancelEventForNonMembers(@Nonnull final PlayerEvent event, @Nonnull final BlockPos blockPos, @Nonnull final IBlockState blockState) {
		final EntityPlayer entityPlayer = event.getEntityPlayer();
		final Block block = blockState.getBlock();
		final float hardness = blockState.getBlockHardness(entityPlayer.getEntityWorld(), blockPos);
		
		// skip force field, anchor and non-reinforced blocks
		if ( block instanceof BlockForceField
		  || hardness < WarpDriveConfig.HULL_HARDNESS[1]
		  || Dictionary.BLOCKS_ANCHOR.contains(block) ) {
			return;
		}
		// keep blocks inside a ship
		final ArrayList<GlobalRegion> globalRegions = GlobalRegionManager.getContainers(EnumGlobalRegionType.SHIP, entityPlayer.world, blockPos);
		if (globalRegions.isEmpty()) {
			return;
		}
		
		// sanitize & summarize the ship list
		boolean isUnderMaintenance = true;
		boolean isCrewMember = true;
		for (final GlobalRegion globalRegion : globalRegions) {
			// abort on invalid ship cores
			final TileEntity tileEntity = entityPlayer.world.getTileEntity(globalRegion.getBlockPos());
			if (!(tileEntity instanceof TileEntityShipCore)) {
				if (Commons.throttleMe("onBreakSpeed-InvalidInstance")) {
					WarpDrive.logger.error(String.format("Unable to adjust harvest speed due to invalid tile entity for global region, expecting TileEntityShipCore, got %s",
					                                     tileEntity ));
				}
				return;
			}
			final TileEntityShipCore tileEntityShipCore = (TileEntityShipCore) tileEntity;
			if (!tileEntityShipCore.isAssemblyValid()) {
				if (Commons.throttleMe("onBreakSpeed-InvalidAssembly")) {
					WarpDrive.logger.error(String.format("Unable to adjust harvest speed due to invalid ship assembly for %s",
					                                     tileEntity ));
				}
				return;
			}
			isUnderMaintenance &= tileEntityShipCore.isUnderMaintenance();
			isCrewMember &= tileEntityShipCore.isCrewMember(entityPlayer);
		}
		
		// apply maintenance bypass/boost
		if (isUnderMaintenance) {
			if (event instanceof BreakSpeed) {
				((BreakSpeed) event).setNewSpeed(5.0F * hardness);
			}
			return;
		}
		
		// restrict mining/access to members only
		if ( !isCrewMember
		  && !entityPlayer.isCreative() ) {
			WarpDrive.logger.error(String.format("Cancelling event for non-member %s",
			                                     entityPlayer ));
			
			event.setCanceled(true);
		}
	}
	
	private void doCancelEventDuringJump(@Nonnull final PlayerEvent event, @Nonnull final BlockPos blockPos) {
		assert event.isCancelable();
		if (event.isCanceled()) {
			return;
		}
		
		if (AbstractSequencer.isLocked(blockPos)) {
			event.setCanceled(true);
		}
	}
}
