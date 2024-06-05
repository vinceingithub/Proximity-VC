package fiveavian.proxvc.mixin.client;

import fiveavian.proxvc.api.ClientEvents;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(value = Minecraft.class, remap = false)
public class MinecraftMixin {
    @Inject(method = "startGame", at = @At("TAIL"))
    public void startGame(CallbackInfo ci) {
        for (Consumer<Minecraft> listener : ClientEvents.START) {
            listener.accept((Minecraft) (Object) this);
        }
    }

    @Inject(method = "shutdownMinecraftApplet", at = @At("HEAD"))
    public void shutdownMinecraftApplet(CallbackInfo ci) {
        for (Consumer<Minecraft> listener : ClientEvents.STOP) {
            listener.accept((Minecraft) (Object) this);
        }
    }

    @Inject(method = "runTick", at = @At("TAIL"))
    public void runTick(CallbackInfo ci) {
        for (Consumer<Minecraft> listener : ClientEvents.TICK) {
            listener.accept((Minecraft) (Object) this);
        }
    }
}
