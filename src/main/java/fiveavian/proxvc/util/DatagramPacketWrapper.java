package fiveavian.proxvc.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class DatagramPacketWrapper {
    private final DatagramSocket socket;
    public final byte[] array;
    public final ByteBuffer buffer;
    public final DatagramPacket packet;

    public DatagramPacketWrapper(DatagramSocket socket, int maxSize) {
        this.socket = socket;
        array = new byte[maxSize];
        buffer = ByteBuffer.wrap(array);
        packet = new DatagramPacket(array, maxSize);
    }

    public void receive() throws IOException {
        packet.setLength(array.length);
        socket.receive(packet);
        buffer.rewind();
    }

    public void send(SocketAddress address) throws IOException {
        packet.setSocketAddress(address);
        packet.setLength(buffer.position());
        socket.send(packet);
        buffer.rewind();
    }
}
