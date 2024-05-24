package fiveavian.proxvc.vc.client;

import fiveavian.proxvc.ProxVCClient;
import fiveavian.proxvc.util.BufferAES;
import fiveavian.proxvc.util.DatagramPacketWrapper;
import fiveavian.proxvc.vc.StreamingAudioSource;
import fiveavian.proxvc.vc.VCProtocol;
import net.minecraft.core.util.helper.AES;
import org.lwjgl.BufferUtils;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Map;

public class VCOutputClient implements Runnable {
    private final ProxVCClient vcClient;
    private final DatagramSocket socket;
    private final DatagramPacketWrapper packet;
    private final Map<Integer, StreamingAudioSource> sources;
    private final ByteBuffer samples = BufferUtils.createByteBuffer(VCProtocol.BUFFER_SIZE + 16);

    public VCOutputClient(ProxVCClient vcClient) {
        this.vcClient = vcClient;
        socket = vcClient.socket;
        packet = new DatagramPacketWrapper(socket, 4 + VCProtocol.BUFFER_SIZE + 16);
        sources = vcClient.sources;
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                receiveNextPacket();
            } catch (SocketException ignored) {
            } catch (Exception ex) {
                System.out.println("Caught an exception during the output client loop.");
                ex.printStackTrace();
            }
        }
        System.out.println("Exited output client loop.");
    }

    private void receiveNextPacket() throws Exception {
        packet.receive();
        if (vcClient.isDisconnected())
            return;
        int entityId = packet.buffer.getInt();
        StreamingAudioSource source = sources.get(entityId);
        if (source == null)
            return;
        samples.limit(VCProtocol.BUFFER_SIZE + 16); // add room for AES padding
        BufferAES.decrypt(AES.clientKeyChain, packet.buffer, samples);
        samples.limit(VCProtocol.BUFFER_SIZE);
        samples.rewind();
        source.queueSamples(samples);
    }
}
