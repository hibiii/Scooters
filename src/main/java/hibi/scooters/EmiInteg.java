package hibi.scooters;

import java.util.ArrayList;
import java.util.List;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import hibi.scooters.recipes.ElectricScooterRecipe;
import hibi.scooters.recipes.KickScooterRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;

public class EmiInteg implements EmiPlugin {
    
    @Override
    public void register(EmiRegistry registry) {
        List<EmiIngredient> input = new ArrayList<EmiIngredient>();
        for (Ingredient ingredient : KickScooterRecipe.RECIPE) {
            input.add(EmiIngredient.of(ingredient));
        }
        registry.addRecipe(new EmiCraftingRecipe(input, EmiStack.of(KickScooterRecipe.getOutput()), new Identifier("scooters", "kick_scooter"), false));
        
        input = new ArrayList<EmiIngredient>();
        for (Ingredient ingredient : ElectricScooterRecipe.RECIPE) {
            input.add(EmiIngredient.of(ingredient));
        }
        registry.addRecipe(new EmiCraftingRecipe(input, EmiStack.of(ElectricScooterRecipe.getOutput()), new Identifier("scooters", "electrc_scooter"), false));
    }
}
