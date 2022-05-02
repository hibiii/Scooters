package hibi.scooters;

import hibi.scooters.recipes.ElectricScooterRecipe;
import hibi.scooters.recipes.KickScooterRecipe;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.TagKey;
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

	public static final Identifier PACKET_INVENTORY_CHANGED = new Identifier("scooters","invchange");
	public static final Item SCOOTER_ITEM = Registry.register(Registry.ITEM, new Identifier("scooters", "kick_scooter"), new ScooterItem(new FabricItemSettings().group(ItemGroup.TRANSPORTATION).maxCount(1)));
	public static final Item ELECTRIC_SCOOTER_ITEM = Registry.register(Registry.ITEM, new Identifier("scooters", "electric_scooter"), new ScooterItem(new FabricItemSettings().group(ItemGroup.TRANSPORTATION).maxCount(1)));

	public static final DockBlock DOCK_BLOCK = new DockBlock(FabricBlockSettings.of(Material.METAL).strength(4.0f));
	public static final BlockEntityType<DockBlockEntity> DOCK_BLOCK_ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier("scooters", "charging_station"), FabricBlockEntityTypeBuilder.create(DockBlockEntity::new, DOCK_BLOCK).build(null));

	public static final Item TIRE_ITEM = Registry.register(Registry.ITEM, new Identifier("scooters","tire"), new Item(new FabricItemSettings().group(ItemGroup.TRANSPORTATION).maxDamage(640)));
	public static final Item RAW_TIRE_ITEM = Registry.register(Registry.ITEM, new Identifier("scooters","raw_tire"), new Item(new FabricItemSettings().group(ItemGroup.MATERIALS).maxCount(16)));

	public static final ScreenHandlerType<ScooterScreenHandler> SCOOTER_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(new Identifier("scooters", "scooter"), ScooterScreenHandler::new);

	public static final TagKey<Block> ABRASIVE_BLOCKS = TagKey.of(Registry.BLOCK_KEY, new Identifier("scooters", "abrasive"));

	public static final SpecialRecipeSerializer<KickScooterRecipe> SCOOTER_CRAFTING_SERIALIZER = RecipeSerializer.register("scooters:kick_scooter_craft", new SpecialRecipeSerializer<KickScooterRecipe>(KickScooterRecipe::new));
	public static final SpecialRecipeSerializer<ElectricScooterRecipe> ELECTRIC_SCOOTER_CRAFTING_SERIALIZER = RecipeSerializer.register("scooters:electric_scooter_craft", new SpecialRecipeSerializer<ElectricScooterRecipe>(ElectricScooterRecipe::new));
	
	public static final SoundEvent SCOOTER_ROLLING = new SoundEvent(new Identifier("scooters", "entity.roll"));
	public static final SoundEvent SCOOTER_TIRE_POP = new SoundEvent(new Identifier("scooters", "entity.tire_pop"));
	public static final SoundEvent CHARGER_CONNECT = new SoundEvent(new Identifier("scooters", "charger.connect"));
	public static final SoundEvent CHARGER_DISCONNECT = new SoundEvent(new Identifier("scooters", "charger.disconnect"));
	@Override
	public void onInitialize() {
		Registry.register(Registry.BLOCK, new Identifier("scooters", "charging_station"), DOCK_BLOCK);
		Registry.register(Registry.ITEM, new Identifier("scooters", "charging_station"), new BlockItem(DOCK_BLOCK, new FabricItemSettings().group(ItemGroup.TRANSPORTATION)));
		Registry.register(Registry.SOUND_EVENT, SCOOTER_ROLLING.getId(), SCOOTER_ROLLING);
		Registry.register(Registry.SOUND_EVENT, SCOOTER_TIRE_POP.getId(), SCOOTER_TIRE_POP);
		Registry.register(Registry.SOUND_EVENT, CHARGER_CONNECT.getId(), CHARGER_CONNECT);
		Registry.register(Registry.SOUND_EVENT, CHARGER_DISCONNECT.getId(), CHARGER_DISCONNECT);
	}
}
