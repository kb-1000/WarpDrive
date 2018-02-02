package cr0s.warpdrive.block.movement;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockShipController extends BlockAbstractContainer {
	
	public BlockShipController(final String registryName) {
		super(registryName, Material.IRON);
		setUnlocalizedName("warpdrive.movement.ShipController");
		GameRegistry.registerTileEntity(TileEntityShipController.class, WarpDrive.PREFIX + registryName);
	}

	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int metadata) {
		return new TileEntityShipController();
	}
	
	@Override
	public boolean onBlockActivated(final World world, final BlockPos blockPos, final IBlockState blockState,
	                                final EntityPlayer entityPlayer, final EnumHand hand, @Nullable final ItemStack itemStackHeld,
	                                final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (hand != EnumHand.MAIN_HAND) {
			return true;
		}
		
		if (itemStackHeld == null) {
			final TileEntity tileEntity = world.getTileEntity(blockPos);
			if (tileEntity instanceof TileEntityShipController) {
				if (entityPlayer.isSneaking()) {
					Commons.addChatMessage(entityPlayer, ((TileEntityShipController) tileEntity).getStatus());
				} else {
					Commons.addChatMessage(entityPlayer, ((TileEntityShipController) tileEntity).attachPlayer(entityPlayer));
				}
				return true;
			}
		}
		
		return false;
	}
}