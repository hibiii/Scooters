package hibi.scooters.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import hibi.scooters.ScooterEntity;
import net.minecraft.server.network.ServerPlayNetworkHandler;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @ModifyExpressionValue(
        method = "onVehicleMove",
        at = @At(
            value = "CONSTANT",
            args = "doubleValue=0.0625"
        )
    )
    double accountPathsForScooters(double p) {
        if (((ServerPlayNetworkHandler)(Object)this).player.getVehicle() instanceof ScooterEntity) {
            return 0.64;
        }
        return p;
    }
}
