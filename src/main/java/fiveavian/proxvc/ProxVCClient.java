package fiveavian.proxvc;

import fiveavian.proxvc.api.ClientEvents;
import fiveavian.proxvc.gui.MicrophoneListComponent;
import fiveavian.proxvc.util.OptionStore;
import fiveavian.proxvc.vc.AudioInputDevice;
import fiveavian.proxvc.vc.StreamingAudioSource;
import fiveavian.proxvc.vc.client.VCInputClient;
import fiveavian.proxvc.vc.client.VCOutputClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.options.components.*;
import net.minecraft.client.gui.options.data.OptionsPage;
import net.minecraft.client.gui.options.data.OptionsPages;
import net.minecraft.client.input.InputDevice;
import net.minecraft.client.option.*;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.net.packet.Packet1Login;
import net.minecraft.core.util.phys.Vec3d;
import org.lwjgl.input.Keyboard;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.opengl.GL11;

import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProxVCClient implements ClientModInitializer {
    public Minecraft client;
    public DatagramSocket socket;
    public AudioInputDevice device;
    public final Map<Integer, StreamingAudioSource> sources = new HashMap<>();
    public SocketAddress serverAddress;
    private Thread inputThread;
    private Thread outputThread;

    public final KeyBinding keyMute = new KeyBinding("key.mute").setDefault(InputDevice.keyboard, Keyboard.KEY_M);
    public final KeyBinding keyPushToTalk = new KeyBinding("key.push_to_talk").setDefault(InputDevice.keyboard, Keyboard.KEY_V);
    public final KeyBinding[] keyBindings = {keyMute, keyPushToTalk};
    public FloatOption voiceChatVolume;
    public BooleanOption isMuted;
    public BooleanOption usePushToTalk;
    public StringOption selectedInputDevice;
    public Option<?>[] options;
    public Path optionFilePath;
    private boolean isMutePressed = false;

    public boolean isDisconnected() {
        return !client.isMultiplayerWorld() || serverAddress == null;
    }

    @Override
    public void onInitializeClient() {
        ClientEvents.START.add(this::start);
        ClientEvents.STOP.add(this::stop);
        ClientEvents.TICK.add(this::tick);
        ClientEvents.RENDER.add(this::render);
        ClientEvents.LOGIN.add(this::login);
        ClientEvents.DISCONNECT.add(this::disconnect);
    }

    private void start(Minecraft client) {
        this.client = client;
        voiceChatVolume = new FloatOption(client.gameSettings, "sound.voice_chat", 1.0f);
        isMuted = new BooleanOption(client.gameSettings, "is_muted", false);
        usePushToTalk = new BooleanOption(client.gameSettings, "use_push_to_talk", false);
        selectedInputDevice = new StringOption(client.gameSettings, "selected_input_device", null);
        options = new Option[]{voiceChatVolume, isMuted, usePushToTalk, selectedInputDevice};
        optionFilePath = FabricLoader.getInstance().getConfigDir().resolve("proxvc_client.properties");
        OptionStore.loadOptions(optionFilePath, options, keyBindings);
        OptionStore.saveOptions(optionFilePath, options, keyBindings);
        try {
            socket = new DatagramSocket();
            device = new AudioInputDevice();
            serverAddress = null;
            inputThread = new Thread(new VCInputClient(this));
            outputThread = new Thread(new VCOutputClient(this));
            inputThread.start();
            outputThread.start();

            OptionsCategory generalCategory = new OptionsCategory("gui.options.page.proxvc.category.general")
                    .withComponent(new FloatOptionComponent(voiceChatVolume))
                    .withComponent(new BooleanOptionComponent(isMuted))
                    .withComponent(new BooleanOptionComponent(usePushToTalk));
            OptionsCategory devicesCategory = new OptionsCategory("gui.options.page.proxvc.category.devices")
                    .withComponent(new MicrophoneListComponent(device, selectedInputDevice));
            OptionsCategory controlsCategory = new OptionsCategory("gui.options.page.proxvc.category.controls")
                    .withComponent(new KeyBindingComponent(keyMute))
                    .withComponent(new KeyBindingComponent(keyPushToTalk));
            OptionsPages.register(new OptionsPage("gui.options.page.proxvc.title", Block.noteblock.getDefaultStack()))
                    .withComponent(generalCategory)
                    .withComponent(devicesCategory)
                    .withComponent(controlsCategory);
            device.open(selectedInputDevice.value);
        } catch (SocketException ex) {
            System.out.println("Failed to start the ProxVC client because of an exception.");
            System.out.println("Continuing without ProxVC.");
            ex.printStackTrace();
        }
    }

    private void stop(Minecraft client) {
        if (optionFilePath != null)
            OptionStore.saveOptions(optionFilePath, options, keyBindings);
        try {
            if (socket != null) {
                socket.close();
            }
            if (inputThread != null) {
                inputThread.join();
            }
            if (outputThread != null) {
                outputThread.join();
            }
            if (device != null) {
                device.close();
            }
        } catch (InterruptedException ex) {
            System.out.println("Failed to stop the ProxVC client because of an exception.");
            ex.printStackTrace();
        }
    }

    private void tick(Minecraft client) {
        if (isDisconnected())
            return;

        Set<Integer> toRemove = new HashSet<>(sources.keySet());
        Set<Integer> toAdd = new HashSet<>();
        for (Entity entity : client.theWorld.loadedEntityList) {
            if (entity instanceof EntityPlayer && entity.id != client.thePlayer.id) {
                toRemove.remove(entity.id);
                toAdd.add(entity.id);
            }
        }
        for (int entityId : toRemove) {
            sources.remove(entityId).close();
        }
        for (int entityId : toAdd) {
            if (!sources.containsKey(entityId)) {
                sources.put(entityId, new StreamingAudioSource());
            }
        }

        if (client.currentScreen == null) {
            if (keyMute.isPressEvent(InputDevice.keyboard))
                isMutePressed = true;
            if (keyMute.isReleaseEvent(InputDevice.keyboard) && isMutePressed) {
                isMutePressed = false;
                isMuted.value = !isMuted.value;
            }
        }

        for (Entity entity : client.theWorld.loadedEntityList) {
            StreamingAudioSource source = sources.get(entity.id);
            if (source == null) {
                continue;
            }
            Vec3d look = entity.getLookAngle();
            AL10.alDistanceModel(AL11.AL_LINEAR_DISTANCE);
            AL10.alSourcef(source.source, AL10.AL_MAX_DISTANCE, 32f);
            AL10.alSourcef(source.source, AL10.AL_REFERENCE_DISTANCE, 16f);
            AL10.alSource3f(source.source, AL10.AL_POSITION, (float) entity.x, (float) entity.y, (float) entity.z);
            AL10.alSource3f(source.source, AL10.AL_DIRECTION, (float) look.xCoord, (float) look.yCoord, (float) look.zCoord);
            AL10.alSource3f(source.source, AL10.AL_VELOCITY, (float) entity.xd, (float) entity.yd, (float) entity.zd);
            AL10.alSourcef(source.source, AL10.AL_GAIN, voiceChatVolume.value);
        }
    }

    private void render(Minecraft client, WorldRenderer renderer) {
        if (isDisconnected() || !client.gameSettings.immersiveMode.drawOverlays()) {
            return;
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, client.renderEngine.getTexture("/proxvc.png"));
        GL11.glColor4d(1.0, 1.0, 1.0, 1.0);
        double u = 0.0;
        if (isMuted.value) {
            u = 0.25;
        } else if (device.isClosed()) {
            u = 0.5;
        } else if (usePushToTalk.value && !keyPushToTalk.isPressed()) {
            u = 0.75;
        }
        Tessellator.instance.startDrawingQuads();
        Tessellator.instance.setColorRGBA_F(1f, 1f, 1f, 0.5f);
        Tessellator.instance.drawRectangleWithUV(4, client.resolution.scaledHeight - 24 - 4, 24, 24, u, 0.0, 0.25, 1.0);
        Tessellator.instance.draw();
    }

    private void login(Minecraft client, Packet1Login packet) {
        Socket socket = client.getSendQueue().netManager.networkSocket;
        serverAddress = socket.getRemoteSocketAddress();
    }

    private void disconnect(Minecraft client) {
        serverAddress = null;
        for (StreamingAudioSource source : sources.values()) {
            source.close();
        }
        sources.clear();
    }
}
