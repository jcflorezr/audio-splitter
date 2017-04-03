package net.jcflorezr.api.audiofileinfo;

import net.jcflorezr.model.AudioFileInfo;
import net.jcflorezr.model.AudioFileLocation;

public interface AudioFileInfoService {

    AudioFileInfo generateAudioFileInfo(AudioFileLocation audioFileLocation, boolean grouped) throws Exception;

}
