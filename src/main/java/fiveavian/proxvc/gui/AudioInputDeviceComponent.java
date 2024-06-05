package fiveavian.proxvc.gui;

import fiveavian.proxvc.vc.AudioInputDevice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.options.components.ButtonComponent;
import net.minecraft.client.option.StringOption;

import java.util.Arrays;
import java.util.Objects;

public class AudioInputDeviceComponent extends ButtonComponent {
    private static final int NO_SPECIFIER = -1;

    private final AudioInputDevice device;
    private final StringOption option;
    private final GuiButton button;
    private String[] specifiers;
    private int specifierIndex;

    public AudioInputDeviceComponent(AudioInputDevice device, StringOption option) {
        super("options." + option.name);
        this.device = device;
        this.option = option;
        button = new GuiButton(0, 0, 0, 150, 20, null);
    }

    @Override
    public void init(Minecraft mc) {
        specifiers = AudioInputDevice.getSpecifiers();
        specifierIndex = searchSpecifierIndex();
        device.open(getSpecifier());
    }

    @Override
    protected void buttonClicked(int mouseButton, int x, int y, int width, int height, int relativeMouseX, int relativeMouseY) {
        if (specifierIndex == NO_SPECIFIER) {
            specifiers = AudioInputDevice.getSpecifiers();
        }
        specifierIndex++;
        if (specifierIndex >= specifiers.length) {
            specifierIndex = NO_SPECIFIER;
        }
        option.set(getSpecifier());
        device.open(getSpecifier());
    }

    @Override
    protected void renderButton(int x, int y, int relativeButtonX, int relativeButtonY, int buttonWidth, int buttonHeight, int relativeMouseX, int relativeMouseY) {
        super.renderButton(x, y, relativeButtonX, relativeButtonY, buttonWidth, buttonHeight, relativeMouseX, relativeMouseY);
        button.xPosition = x + relativeButtonX;
        button.yPosition = y + relativeButtonY;
        button.width = buttonWidth;
        button.height = buttonHeight;
        button.displayString = getDisplayName();
        button.drawButton(mc, x + relativeMouseX, y + relativeMouseY);
    }

    @Override
    public boolean isDefault() {
        return Objects.equals(option.value, option.getDefaultValue());
    }

    @Override
    public void resetValue() {
        option.set(option.getDefaultValue());
        specifierIndex = searchSpecifierIndex();
    }

    private int searchSpecifierIndex() {
        specifierIndex = option.value == null ? NO_SPECIFIER : Arrays.binarySearch(specifiers, option.value);
        return specifierIndex < 0 ? NO_SPECIFIER : specifierIndex;
    }

    private String getSpecifier() {
        return specifierIndex == NO_SPECIFIER ? null : specifiers[specifierIndex];
    }

    private String getDisplayName() {
        return specifierIndex == NO_SPECIFIER ? "No Microphone" : specifiers[specifierIndex];
    }
}
