package cr0s.warpdrive.block.atomic;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.data.EnumTier;

import javax.annotation.Nonnull;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class BlockParticlesInjector extends BlockAcceleratorControlPoint {
	// @TODO: add on/off textures and states
	
	public BlockParticlesInjector(final String registryName, final EnumTier enumTier) {
		super(registryName, enumTier, true);
		
		setUnlocalizedName("warpdrive.atomic.particles_injector");
		BlockAbstractContainer.registerTileEntity(TileEntityParticlesInjector.class, new ResourceLocation(WarpDrive.MODID, registryName));
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityParticlesInjector(enumTier);
	}
}
