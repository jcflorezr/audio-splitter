package net.jcflorezr.api.audiofileinfo;

import net.jcflorezr.model.audiocontent.AudioFileCompleteInfo;
import net.jcflorezr.model.request.AudioFileBasicInfo;

public interface AudioFileInfoService {

    AudioFileCompleteInfo generateAudioFileInfo(AudioFileBasicInfo audioFileBasicInfo, boolean grouped) throws Exception;

}
