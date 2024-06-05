package fiveavian.proxvc.gui;

import fiveavian.proxvc.ProxVCClient;
import fiveavian.proxvc.vc.AudioInputDevice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.options.components.ButtonComponent;
import net.minecraft.client.option.StringOption;

import java.util.Arrays;

public class AudioInputDeviceComponent extends ButtonComponent {
    private final StringOption option;
    private final GuiButton button;

    private int currentSelection;

    private final ProxVCClient client;
    private String[] specifiers;

    //little mix of GuiVCOptions and StringOptionComponent
    public AudioInputDeviceComponent(ProxVCClient client, StringOption option) {
        super("options." + option.name);
        this.client = client;
        this.specifiers = AudioInputDevice.getSpecifiers();
        this.option = option;
        this.button = new GuiButton(0, 0, 0, 150, 20, option.value);
    }

    protected void buttonClicked(int mouseButton, int x, int y, int width, int height, int relativeMouseX, int relativeMouseY) {
        currentSelection++;
        //Reset if its beyond mic amount
        if (currentSelection >= this.specifiers.length) {
            resetValue();
        }
        else {
            this.option.set(this.specifiers[currentSelection]);
            this.updateString();
            this.client.device.open(option.value);
        }

    }

    protected void renderButton(int x, int y, int relativeButtonX, int relativeButtonY, int buttonWidth, int buttonHeight, int relativeMouseX, int relativeMouseY) {
        super.renderButton(x, y, relativeButtonX, relativeButtonY, buttonWidth, buttonHeight, relativeMouseX, relativeMouseY);
        this.button.xPosition = x + relativeButtonX;
        this.button.yPosition = y + relativeButtonY;
        this.button.width = buttonWidth;
        this.button.height = buttonHeight;
        this.button.drawButton(mc, x + relativeMouseX, y + relativeMouseY);
    }

    private void updateString() {
            this.button.displayString = option.value;
    }

    public void resetValue() {
        this.option.set(this.option.getDefaultValue());
        this.currentSelection = -1;
        this.client.device.open(null);
        this.updateString();

        //Also check for new microphones when reloading
        this.specifiers = AudioInputDevice.getSpecifiers();
    }

    public void init(Minecraft mc) {
        this.currentSelection = Arrays.stream(this.specifiers)
                .filter(c -> c.equals(this.option.value))
                .findFirst()
                .map(c -> Arrays.asList(this.specifiers).indexOf(c))
                .orElse(-1);

        this.updateString();
    }

    public boolean isDefault() {
        return (this.option.value).equals(this.option.getDefaultValue());
    }
}
