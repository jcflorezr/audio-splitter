package net.jcflorezr.api.audiocontent;

import net.jcflorezr.model.audiocontent.AudioFileCompleteInfo;
import net.jcflorezr.model.endpoint.AudioFileBasicInfoEntity;

public interface AudioFileInfoService {

    AudioFileCompleteInfo generateAudioFileInfo(AudioFileBasicInfoEntity audioFileBasicInfoEntity, boolean grouped) throws Exception;

}
