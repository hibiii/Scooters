package hibi.scooters.recipes;

import hibi.scooters.Common;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.CraftingCategory;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class KickScooterRecipe
extends SpecialCraftingRecipe {

	public KickScooterRecipe(Identifier id, CraftingCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(RecipeInputInventory inv, World w) {
		// The anchor is the bottomost row because it's the same flipped
		if(!(
			INPUT[6].test(inv.getStack(6)) &&
			INPUT[7].test(inv.getStack(7)) &&
			INPUT[8].test(inv.getStack(8))))
			return false;

		boolean flip = this.isFlipped(inv);
		
		// Check the other two rows
		for (int j = 0; j < 2; j++) {
			for (int i = 0; i < 3; i++) {
				if(!INPUT[j * 3 + i].test(inv.getStack(flip? j * 3 + 2 - i : j * 3 + i)))
					return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack craft(RecipeInputInventory inv, DynamicRegistryManager manager) {
		ItemStack iout = Common.KICK_SCOOTER_ITEM.getDefaultStack().copy();
		boolean flip = this.isFlipped(inv);
		NbtList tires = new NbtList();
		// Unrolled loop
			NbtCompound compound = new NbtCompound();
			inv.getStack(flip? 6: 8).writeNbt(compound);
			tires.add(compound);
		// --- //
			compound = new NbtCompound();
			inv.getStack(flip? 8: 6).writeNbt(compound);
			tires.add(compound);
		NbtCompound nbtout = new NbtCompound();
		nbtout.put("Tires", tires);
		iout.setNbt(nbtout);
		return iout;
	}

	private boolean isFlipped(RecipeInputInventory inv) {
		if(INPUT[3].test(inv.getStack(5))) return true;
		return false;
	}

	@Override
	public boolean fits(int w, int h) {
		// because 3 is prime, it's safe to compare against 3^2
		// FIXME: Recipes do not work on a crafting inventory larger than 3x3
		return w * h == 9;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return Common.KICK_SCOOTER_CRAFTING_SERIALIZER;
	}

	@Override
	public DefaultedList<Ingredient> getIngredients() {
		return RECIPE;
	}

	@Override
	public boolean isIgnoredInRecipeBook() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	private static final Ingredient[] INPUT = {
		Ingredient.EMPTY, Ingredient.EMPTY, Ingredient.ofTag(ItemTags.WOOL),
		Ingredient.ofTag(ConventionalItemTags.IRON_INGOTS), Ingredient.EMPTY, Ingredient.ofItems(Items.IRON_BARS),
		Ingredient.ofItems(Common.TIRE_ITEM), Ingredient.ofItems(Items.HEAVY_WEIGHTED_PRESSURE_PLATE), Ingredient.ofItems(Common.TIRE_ITEM)
	};
	private static final DefaultedList<Ingredient> RECIPE = DefaultedList.copyOf(Ingredient.EMPTY, INPUT);
}