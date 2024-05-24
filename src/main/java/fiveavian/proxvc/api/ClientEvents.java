package fiveavian.proxvc.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.core.net.packet.Packet1Login;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ClientEvents {
    public static final List<Consumer<Minecraft>> START = new ArrayList<>();
    public static final List<Consumer<Minecraft>> STOP = new ArrayList<>();
    public static final List<Consumer<Minecraft>> TICK = new ArrayList<>();
    public static final List<BiConsumer<Minecraft, WorldRenderer>> RENDER = new ArrayList<>();

    public static final List<BiConsumer<Minecraft, Packet1Login>> LOGIN = new ArrayList<>();
    public static final List<Consumer<Minecraft>> DISCONNECT = new ArrayList<>();
}
