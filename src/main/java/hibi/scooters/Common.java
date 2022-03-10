package hibi.scooters;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.Material;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Common implements ModInitializer {

	public static final EntityType<ScooterEntity> SCOOTER_ENTITY = Registry.register(
		Registry.ENTITY_TYPE,
		new Identifier("scooters", "kick_scooter"),
		FabricEntityTypeBuilder.create(SpawnGroup.MISC, ScooterEntity::new)
			.dimensions(EntityDimensions.fixed(0.8f, 0.8f))
			.trackRangeBlocks(10)
			.build()
	);

	public static final EntityType<ElectricScooterEntity> ELECTRIC_SCOOTER_ENTITY = Registry.register(
		Registry.ENTITY_TYPE,
		new Identifier("scooters", "electric_scooter"),
		FabricEntityTypeBuilder.create(SpawnGroup.MISC, ElectricScooterEntity::new)
			.dimensions(EntityDimensions.fixed(0.8f, 0.8f))
			.trackRangeBlocks(10)
			.build()
	);

	public static final Item SCOOTER_ITEM = Registry.register(Registry.ITEM, new Identifier("scooters", "kick_scooter"), new ScooterItem(new FabricItemSettings().group(ItemGroup.TRANSPORTATION).maxCount(1)));
	public static final Item ELECTRIC_SCOOTER_ITEM = Registry.register(Registry.ITEM, new Identifier("scooters", "electric_scooter"), new ScooterItem(new FabricItemSettings().group(ItemGroup.TRANSPORTATION).maxCount(1)));

	public static final DockBlock DOCK_BLOCK = new DockBlock(FabricBlockSettings.of(Material.METAL).strength(4.0f));

	@Override
	public void onInitialize() {
		Registry.register(Registry.BLOCK, new Identifier("scooters", "charging_station"), DOCK_BLOCK);
		Registry.register(Registry.ITEM, new Identifier("scooters", "charging_station"), new BlockItem(DOCK_BLOCK, new FabricItemSettings().group(ItemGroup.TRANSPORTATION)));
	}
}
