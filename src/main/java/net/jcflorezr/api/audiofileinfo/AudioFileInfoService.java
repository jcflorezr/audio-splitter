package net.jcflorezr.api.audiofileinfo;

import net.jcflorezr.model.audiocontent.AudioFileInfo;
import net.jcflorezr.model.request.AudioFileLocation;

public interface AudioFileInfoService {

    AudioFileInfo generateAudioFileInfo(AudioFileLocation audioFileLocation, boolean grouped) throws Exception;

}
