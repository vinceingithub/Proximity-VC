package fiveavian.proxvc.gui;

import fiveavian.proxvc.ProxVCClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.options.components.ButtonComponent;
import net.minecraft.client.option.IntegerOption;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.OpenALException;

public class AudioInputDeviceComponent extends ButtonComponent {
    private final IntegerOption option;
    private final GuiButton button;

    private final ProxVCClient client;
    private final String[] specifiers;

    //little mix of GuiVCOptions and IntegerOptionComponent
    public AudioInputDeviceComponent(ProxVCClient client, IntegerOption option) {
        super("options." + option.name);
        this.client = client;
        String result = null;
        try {
            result = ALC10.alcGetString(null, ALC11.ALC_CAPTURE_DEVICE_SPECIFIER);
        } catch (OpenALException ignored) {
        }
        specifiers = result == null ? new String[0] : result.split("\0");
        this.option = option;
        this.button = new GuiButton(0, 0, 0, 150, 20, this.specifiers[this.option.value]);
    }

    protected void buttonClicked(int mouseButton, int x, int y, int width, int height, int relativeMouseX, int relativeMouseY) {
        //Reset if its beyond mic amount
        if (this.option.value >= specifiers.length - 1){
            this.option.set(0);
        }
        else{
            this.option.set(this.option.value + 1);
        }
        System.out.println(this.option.value);
        this.updateString();
        this.updateMicrophone();
    }

    protected void renderButton(int x, int y, int relativeButtonX, int relativeButtonY, int buttonWidth, int buttonHeight, int relativeMouseX, int relativeMouseY) {
        super.renderButton(x, y, relativeButtonX, relativeButtonY, buttonWidth, buttonHeight, relativeMouseX, relativeMouseY);
        this.button.xPosition = x + relativeButtonX;
        this.button.yPosition = y + relativeButtonY;
        this.button.width = buttonWidth;
        this.button.height = buttonHeight;
        this.button.drawButton(mc, x + relativeMouseX, y + relativeMouseY);
    }

    private void updateString(){
        this.button.displayString = this.specifiers[this.option.value];
    }

    public void resetValue() {
        this.option.set(this.option.getDefaultValue());
        this.updateString();
        //this.option.onUpdate();
    }

    public void init(Minecraft mc) {
        this.button.displayString = this.specifiers[this.option.value];
    }

    private void updateMicrophone(){
        this.client.device.open(this.specifiers[this.option.value]);
    }

    public boolean isDefault() {
        return (this.option.value).equals(this.option.getDefaultValue());
    }
}
