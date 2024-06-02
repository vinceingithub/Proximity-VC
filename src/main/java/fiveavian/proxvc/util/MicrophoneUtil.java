package fiveavian.proxvc.util;

import fiveavian.proxvc.ProxVCClient;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.OpenALException;

//Made this to make it easier to check for Microphone crap
public class MicrophoneUtil {
    public static synchronized String[] getSpecifiers(){
        String result = null;
        try {
            result = ALC10.alcGetString(null, ALC11.ALC_CAPTURE_DEVICE_SPECIFIER);
        } catch (OpenALException ignored) {
        }
        return result == null ? new String[0] : result.split("\0");
    }

    public static void updateMicrophone(int MicrophoneValue, ProxVCClient client){
        if (MicrophoneValue == -1){
            client.device.open("null");
        } else
        {
            //idk why I have to leave this in, maybe my janky code made it so that as it is read it gets deleted? so every time I need to refresh it??
            getSpecifiers();
            client.device.open(MicrophoneUtil.getSpecifiers()[MicrophoneValue]);}

        System.out.println(MicrophoneValue);
    }
}
