package hibi.scooters;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.entity.api.QuiltBlockEntityTypeBuilder;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.entity.api.QuiltEntityTypeBuilder;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hibi.scooters.recipes.ElectricScooterRecipe;
import hibi.scooters.recipes.KickScooterRecipe;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

// TODO Organize common data
// TODO Organize innitialization
public class Common implements ModInitializer {

	public static final String MODID = "scooters";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static final Identifier KICK_SCOOTER_ID;
	public static final EntityType<ScooterEntity> KICK_SCOOTER_ENTITY;
	public static final Item KICK_SCOOTER_ITEM;
	public static final SpecialRecipeSerializer<KickScooterRecipe> KICK_SCOOTER_CRAFTING_SERIALIZER;

	public static final Identifier ELECTRIC_SCOOTER_ID;
	public static final EntityType<ElectricScooterEntity> ELECTRIC_SCOOTER_ENTITY;
	public static final Item ELECTRIC_SCOOTER_ITEM;
	public static final SpecialRecipeSerializer<ElectricScooterRecipe> ELECTRIC_SCOOTER_CRAFTING_SERIALIZER;

	public static final Identifier CHARGING_STATION_ID;
	public static final ChargingStationBlock CHARGING_STATION_BLOCK;
	public static final BlockEntityType<ChargingStationBlockEntity> CHARGING_STATION_BLOCK_ENTITY;

	public static final Item TIRE_ITEM;
	public static final Item RAW_TIRE_ITEM;

	public static final Item COPPER_NUGGET_ITEM;
	public static final Item POTATO_BATTERY_ITEM;
	public static final Item SPENT_POTATO_BATTERY_ITEM;

	public static final Identifier PACKET_INVENTORY_CHANGED_ID;
	public static final Identifier PACKET_THROTTLE_ID;
	public static final ExtendedScreenHandlerType<ScooterScreenHandler> SCOOTER_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(ScooterScreenHandler::new);
	public static final Identifier SCOOTER_SCREEN_HANDLER_ID;
	public static final TagKey<Block> ABRASIVE_BLOCKS = TagKey.of(RegistryKeys.BLOCK, new Identifier(MODID, "abrasive"));
	
	public static final SoundEvent SOUND_SCOOTER_ROLLING = SoundEvent.createVariableRangeEvent(new Identifier(MODID, "entity.roll"));
	public static final SoundEvent SOUND_SCOOTER_TIRE_POP = SoundEvent.createVariableRangeEvent(new Identifier(MODID, "entity.tire_pop"));
	public static final SoundEvent SOUND_CHARGER_CONNECT = SoundEvent.createVariableRangeEvent(new Identifier(MODID, "charger.connect"));
	public static final SoundEvent SOUND_CHARGER_DISCONNECT = SoundEvent.createVariableRangeEvent(new Identifier(MODID, "charger.disconnect"));
	
	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.debug("Initializing common");
		LOGGER.debug("Common Init: Scooter");

		// Kick Scooter //
		Registry.register(Registries.ENTITY_TYPE, KICK_SCOOTER_ID, KICK_SCOOTER_ENTITY);
		var kickScooter = Registry.register(Registries.ITEM, KICK_SCOOTER_ID, KICK_SCOOTER_ITEM);
		RecipeSerializer.register(KICK_SCOOTER_ID.toString() + "_craft", KICK_SCOOTER_CRAFTING_SERIALIZER);
		LOGGER.debug("Common Init: Escooter");

		// Electric Scooter //
		Registry.register(Registries.ENTITY_TYPE, ELECTRIC_SCOOTER_ID, ELECTRIC_SCOOTER_ENTITY);
		var electricScooter = Registry.register(Registries.ITEM, ELECTRIC_SCOOTER_ID, ELECTRIC_SCOOTER_ITEM);
		RecipeSerializer.register(ELECTRIC_SCOOTER_ID.toString() + "_craft", ELECTRIC_SCOOTER_CRAFTING_SERIALIZER);
		LOGGER.debug("Common Init: Charging Station");

		// Charging Station  //
		Registry.register(Registries.BLOCK, CHARGING_STATION_ID, CHARGING_STATION_BLOCK);
		var chargingStation = Registry.register(Registries.ITEM, CHARGING_STATION_ID, new BlockItem(CHARGING_STATION_BLOCK, new QuiltItemSettings()));
		Registry.register(Registries.BLOCK_ENTITY_TYPE, CHARGING_STATION_ID, CHARGING_STATION_BLOCK_ENTITY);
		LOGGER.debug("Common Init: Tires");

		// Tires //
		var tire = Registry.register(Registries.ITEM, new Identifier(MODID, "tire"), TIRE_ITEM);
		var rawTire = Registry.register(Registries.ITEM, new Identifier(MODID, "raw_tire"), RAW_TIRE_ITEM);

		var copperNugget = Registry.register(Registries.ITEM, new Identifier(MODID, "copper_nugget"), COPPER_NUGGET_ITEM);
		var potatoBattery = Registry.register(Registries.ITEM, new Identifier(MODID, "potato_battery"), POTATO_BATTERY_ITEM);
		var spentPotatoBattery = Registry.register(Registries.ITEM, new Identifier(MODID, "spent_potato_battery"), SPENT_POTATO_BATTERY_ITEM);

		// Networking and Misc //
		ServerPlayNetworking.registerGlobalReceiver(PACKET_THROTTLE_ID, (server, player, handler, buf, responseSender) -> {
			ElectricScooterEntity.updateThrottle((ServerWorld)(player.getWorld()),buf);
		});
		Registry.register(Registries.SCREEN_HANDLER_TYPE, SCOOTER_SCREEN_HANDLER_ID, SCOOTER_SCREEN_HANDLER);
		LOGGER.debug("Common Init: Sounds");

		// Sounds //
		Registry.register(Registries.SOUND_EVENT, SOUND_SCOOTER_ROLLING.getId(), SOUND_SCOOTER_ROLLING);
		Registry.register(Registries.SOUND_EVENT, SOUND_SCOOTER_TIRE_POP.getId(), SOUND_SCOOTER_TIRE_POP);
		Registry.register(Registries.SOUND_EVENT, SOUND_CHARGER_CONNECT.getId(), SOUND_CHARGER_CONNECT);
		Registry.register(Registries.SOUND_EVENT, SOUND_CHARGER_DISCONNECT.getId(), SOUND_CHARGER_DISCONNECT);

		// Item Groups //
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL_BLOCKS).register((content) -> {
			content.addAfter(Items.LODESTONE, chargingStation);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS_AND_UTILITIES).register((content) -> {
			ItemStack electricScooterStack = electricScooter.getDefaultStack();
			NbtList batteries = new NbtList();
			batteries.add(new ItemStack(potatoBattery, 64).writeNbt(new NbtCompound()));
			electricScooterStack.getNbt().put(ElectricScooterEntity.NBT_KEY_BATTERIES, batteries);
			content.addAfter(Items.TNT_MINECART, kickScooter.getDefaultStack(), electricScooterStack, tire.getDefaultStack());
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((content) -> {
			content.addBefore(Items.SCUTE, rawTire);
			content.addAfter(Items.IRON_NUGGET, copperNugget);
			content.addAfter(Items.PHANTOM_MEMBRANE, potatoBattery, spentPotatoBattery);
		});

		LOGGER.debug("Common Init finished");
	}

	static {
		KICK_SCOOTER_ID = new Identifier(MODID, "kick_scooter");
		KICK_SCOOTER_ENTITY = QuiltEntityTypeBuilder.create(SpawnGroup.MISC, ScooterEntity::new)
			.setDimensions(EntityDimensions.fixed(0.8f, 0.8f))
			.maxChunkTrackingRange(4)
			.build();
		KICK_SCOOTER_ITEM = new ScooterItem(new QuiltItemSettings()
			.maxCount(1)
		);
		KICK_SCOOTER_CRAFTING_SERIALIZER = new SpecialRecipeSerializer<KickScooterRecipe>(KickScooterRecipe::new);


		ELECTRIC_SCOOTER_ID = new Identifier(MODID, "electric_scooter");
		ELECTRIC_SCOOTER_ENTITY = QuiltEntityTypeBuilder.create(SpawnGroup.MISC, ElectricScooterEntity::new)
			.setDimensions(EntityDimensions.fixed(0.8f, 0.8f))
			.maxChunkTrackingRange(4)
			.build();
		ELECTRIC_SCOOTER_ITEM = new ScooterItem(new QuiltItemSettings()
			.maxCount(1)
		);
		ELECTRIC_SCOOTER_CRAFTING_SERIALIZER = new SpecialRecipeSerializer<ElectricScooterRecipe>(ElectricScooterRecipe::new);


		CHARGING_STATION_ID = new Identifier(MODID, "charging_station");
		CHARGING_STATION_BLOCK = new ChargingStationBlock(QuiltBlockSettings
			.copyOf(Blocks.STONE_BRICKS)
			.sounds(BlockSoundGroup.METAL)
			.mapColor(Blocks.REPEATER.getDefaultMapColor())
			.strength(4.0f)
		);
		CHARGING_STATION_BLOCK_ENTITY = QuiltBlockEntityTypeBuilder.create(ChargingStationBlockEntity::new, CHARGING_STATION_BLOCK)
			.build(null);
		
		
		TIRE_ITEM = new Item(new QuiltItemSettings()
			.maxDamage(640)
		);
		RAW_TIRE_ITEM = new Item(new QuiltItemSettings()
			.maxCount(16)
		);

		COPPER_NUGGET_ITEM = new Item(new QuiltItemSettings());
		POTATO_BATTERY_ITEM = new Item(new QuiltItemSettings());
		SPENT_POTATO_BATTERY_ITEM = new Item(new QuiltItemSettings());
	
		PACKET_INVENTORY_CHANGED_ID = new Identifier(MODID,"invchange");
		PACKET_THROTTLE_ID = new Identifier(MODID, "esctup");

		SCOOTER_SCREEN_HANDLER_ID = new Identifier(MODID, "scooter_screen_handler");
	}
}
