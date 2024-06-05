package fiveavian.proxvc.mixin.client;

import fiveavian.proxvc.api.ClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.net.handler.NetClientHandler;
import net.minecraft.core.net.packet.Packet1Login;
import net.minecraft.core.net.packet.Packet255KickDisconnect;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mixin(value = NetClientHandler.class, remap = false)
public class NetClientHandlerMixin {
    @Shadow
    @Final
    private Minecraft mc;
    @Shadow
    private boolean disconnected;

    @Inject(method = "handleLogin", at = @At("TAIL"))
    public void handleLogin(Packet1Login packet, CallbackInfo ci) {
        for (BiConsumer<Minecraft, Packet1Login> listener : ClientEvents.LOGIN) {
            listener.accept(mc, packet);
        }
    }

    @Inject(method = "handleKickDisconnect", at = @At("HEAD"))
    public void handleKickDisconnect(Packet255KickDisconnect packet, CallbackInfo ci) {
        for (Consumer<Minecraft> listener : ClientEvents.DISCONNECT) {
            listener.accept(mc);
        }
    }

    @Inject(method = "handleErrorMessage", at = @At("HEAD"))
    public void handleErrorMessage(String s, Object[] errorLines, CallbackInfo ci) {
        if (!disconnected) {
            for (Consumer<Minecraft> listener : ClientEvents.DISCONNECT) {
                listener.accept(mc);
            }
        }
    }
}
