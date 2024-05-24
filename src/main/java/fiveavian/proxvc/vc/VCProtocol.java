package fiveavian.proxvc.vc;

public class VCProtocol {
    public static final int SAMPLE_RATE = 16384;
    public static final int SAMPLE_SIZE = 2; // AL_FORMAT_MONO16
    public static final int PACKET_RATE = 32; // chosen so the packet size stays below the Ethernet MTU of 1500
    public static final int SAMPLE_COUNT = SAMPLE_RATE / PACKET_RATE;
    public static final int BUFFER_SIZE = SAMPLE_COUNT * SAMPLE_SIZE;
}
