package fiveavian.proxvc.gui;

import fiveavian.proxvc.ProxVCClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.options.components.ButtonComponent;
import net.minecraft.client.option.IntegerOption;
import fiveavian.proxvc.util.MicrophoneUtil;

public class AudioInputDeviceComponent extends ButtonComponent {
    private final IntegerOption option;
    private final GuiButton button;

    private final ProxVCClient client;
    private final String[] specifiers;

    //little mix of GuiVCOptions and IntegerOptionComponent
    public AudioInputDeviceComponent(ProxVCClient client, IntegerOption option) {
        super("options." + option.name);
        this.client = client;
        this.specifiers = MicrophoneUtil.getSpecifiers();
        this.option = option;
        this.button = new GuiButton(0, 0, 0, 150, 20, "No Microphone");
    }

    protected void buttonClicked(int mouseButton, int x, int y, int width, int height, int relativeMouseX, int relativeMouseY) {
        //Reset if its beyond mic amount
        if (this.option.value >= specifiers.length - 1){
            this.option.set(-1);
        }
        else{
            this.option.set(this.option.value + 1);
        }
        this.updateString();
        MicrophoneUtil.updateMicrophone(this.option.value, this.client);
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
        if (this.option.value == -1){
            this.button.displayString = "No Microphone";
        } else
            this.button.displayString = this.specifiers[this.option.value];
    }

    public void resetValue() {
        this.option.set(this.option.getDefaultValue());
        this.updateString();
    }

    public void init(Minecraft mc) {
        updateString();
    }

    public boolean isDefault() {
        return (this.option.value).equals(this.option.getDefaultValue());
    }
}
