package hibi.scooters;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Common implements ModInitializer {

	public static final EntityType<ScooterEntity> SCOOTER_TYPE = Registry.register(
		Registry.ENTITY_TYPE,
		new Identifier("scooters", "scooter"),
		FabricEntityTypeBuilder.create(SpawnGroup.MISC, ScooterEntity::new)
			.dimensions(EntityDimensions.fixed(0.8f, 0.8f))
			.trackRangeBlocks(1)
			.build()
	);

	@Override
	public void onInitialize() {
	}
	
}
