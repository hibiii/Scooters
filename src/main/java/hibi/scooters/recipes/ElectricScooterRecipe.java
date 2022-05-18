package hibi.scooters.recipes;

import hibi.scooters.Common;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class ElectricScooterRecipe
extends SpecialCraftingRecipe {

	public ElectricScooterRecipe(Identifier id) {
		super(id);
	}

	@Override
	public boolean matches(CraftingInventory inv, World w) {
		// The scooter in the middle is the anchor
		// (Not applicable now because we're discarding inventories sizeof != 3x3)

		boolean flip = this.isFlipped(inv);

		// Simple row-wise test
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if(!INPUT[i * 3 + j].test(inv.getStack(flip? i * 3 + 2 - j: i * 3 + j)))
					return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack craft(CraftingInventory inv) {
		ItemStack out = this.getOutput().copy();
		out.setNbt(inv.getStack(4).getNbt());
		return out;
	}

	@Override
	public boolean fits(int w, int h) {
		// FIXME: Recipes do not work on a crafting inventory larger than 3x3
		return w * h == 9;
	}

	@Override
	public ItemStack getOutput() {
		return Common.ELECTRIC_SCOOTER_ITEM.getDefaultStack().copy();
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

	@Override
	public RecipeSerializer<?> getSerializer() {
		return Common.ELECTRIC_SCOOTER_CRAFTING_SERIALIZER;
	}
	
	private boolean isFlipped(CraftingInventory inv) {
		return !INPUT[3].test(inv.getStack(3));
	}

	private static final Ingredient[] INPUT = {
		Ingredient.EMPTY,                          Ingredient.EMPTY,                             Ingredient.ofItems(Items.LEVER),
		Ingredient.ofItems(Items.REDSTONE_TORCH),  Ingredient.ofItems(Common.KICK_SCOOTER_ITEM), Ingredient.ofItems(Items.REDSTONE),
		Ingredient.ofItems(Items.NETHERITE_SCRAP), Ingredient.ofItems(Items.NETHERITE_SCRAP),    Ingredient.ofItems(Items.REDSTONE)
	};

	private static final DefaultedList<Ingredient> RECIPE = DefaultedList.copyOf(Ingredient.EMPTY, INPUT);
}
