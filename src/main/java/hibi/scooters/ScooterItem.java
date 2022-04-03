package hibi.scooters;

import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ScooterItem
extends Item {

	public ScooterItem(Settings settings) {
		super(settings);
	}
	
	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		if(context.getSide() == Direction.DOWN)
			return ActionResult.FAIL;
		World world = context.getWorld();
		ScooterEntity scooter = ScooterEntity.create(this.asItem() == Common.ELECTRIC_SCOOTER_ITEM? Common.ELECTRIC_SCOOTER_ENTITY: Common.SCOOTER_ENTITY, context);
		if(!world.isSpaceEmpty(scooter, scooter.getBoundingBox()) || !world.getOtherEntities(scooter, scooter.getBoundingBox()).isEmpty())
			return ActionResult.FAIL;
		scooter.setYaw(context.getPlayerYaw());
		if(world instanceof ServerWorld) {
			world.spawnEntity(scooter);
			world.emitGameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, scooter);
		}
		context.getStack().decrement(1);
		return ActionResult.success(world.isClient);
	}
}
