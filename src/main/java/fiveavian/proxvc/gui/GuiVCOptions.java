package fiveavian.proxvc.gui;

import fiveavian.proxvc.ProxVCClient;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.OpenALException;

public class GuiVCOptions extends GuiScreen {
    private final ProxVCClient client;
    private final String[] specifiers;

    public GuiVCOptions(ProxVCClient client) {
        this.client = client;
        String result = null;
        try {
            result = ALC10.alcGetString(null, ALC11.ALC_CAPTURE_DEVICE_SPECIFIER);
        } catch (OpenALException ignored) {
        }
        specifiers = result == null ? new String[0] : result.split("\0");
    }

    @Override
    public void init() {
        controlList.add(new GuiButton(0, 0, 0, "No Microphone"));
        for (int i = 0; i < specifiers.length; i++)
            controlList.add(new GuiButton(i + 1, 0, i * 20 + 20, specifiers[i]));
    }

    @Override
    protected void buttonPressed(GuiButton button) {
        if (button.id == 0)
            client.device.open(null);
        else
            client.device.open(button.displayString);
        client.client.displayGuiScreen(null);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        int color = this.mc.gameSettings.guiBackgroundColor.value.getARGB();
        this.drawGradientRect(0, 0, this.width, this.height, color, color);
        super.drawScreen(mouseX, mouseY, partialTick);
    }
}
