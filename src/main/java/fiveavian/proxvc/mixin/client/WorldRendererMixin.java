package fiveavian.proxvc.mixin.client;

import fiveavian.proxvc.api.ClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(value = WorldRenderer.class, remap = false)
public class WorldRendererMixin {
    @Shadow
    public Minecraft mc;

    @Inject(method = "updateCameraAndRender", at = @At("TAIL"))
    public void updateCameraAndRender(float partialTick, CallbackInfo ci) {
        for (BiConsumer<Minecraft, WorldRenderer> listener : ClientEvents.RENDER) {
            listener.accept(mc, (WorldRenderer) (Object) this);
        }
    }
}
