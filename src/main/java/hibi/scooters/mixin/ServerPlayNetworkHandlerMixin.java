package hibi.scooters.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import hibi.scooters.ScooterEntity;
import net.minecraft.server.network.ServerPlayNetworkHandler;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @ModifyConstant(
        method = "onVehicleMove",
        constant = @Constant(doubleValue = 0.0625D)
    )
    double accountPathsForScooters(double p) {
        if (((ServerPlayNetworkHandler)(Object)this).player.getVehicle() instanceof ScooterEntity) {
            return 0.64;
        }
        return p;
    }
}
