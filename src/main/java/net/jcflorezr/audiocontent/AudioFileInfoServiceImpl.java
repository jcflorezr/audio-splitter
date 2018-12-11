package net.jcflorezr.audiocontent;

import net.jcflorezr.api.audiocontent.AudioFileInfoService;
import net.jcflorezr.api.audiocontent.signal.SoundZonesDetector;
import net.jcflorezr.model.audiocontent.AudioContent;
import net.jcflorezr.model.audiocontent.AudioFileCompleteInfo;
import net.jcflorezr.model.endpoint.AudioFileBasicInfoEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AudioFileInfoServiceImpl implements AudioFileInfoService {

    @Autowired
    private AudioConverterService audioConverterService;

    @Autowired
    private AudioContentService audioContentService;

    @Autowired
    private SoundZonesDetector soundZonesDetector;

    @Override
    public AudioFileCompleteInfo generateAudioFileInfo(AudioFileBasicInfoEntity audioFileBasicInfoEntity, boolean grouped) throws Exception {
//        String convertedAudioFileName = audioConverterService.convertFileToWavIfNeeded(audioFileBasicInfoEntity.getAudioFileName());
//        audioFileBasicInfoEntity.setConvertedAudioFileName(convertedAudioFileName);
//        AudioFileCompleteInfo audioFileCompleteInfo = new AudioFileCompleteInfo(audioFileBasicInfoEntity);
//        AudioContent audioContent = audioContentService.retrieveAudioContent(audioFileCompleteInfo);
//        audioFileCompleteInfo.setAudioContent(audioContent);
//        audioFileCompleteInfo.setAudioClipsInfo(soundZonesDetector.retrieveAudioClipsInfo(audioFileBasicInfoEntity.getAudioFileName(), audioContent.getOriginalAudioSignal()));
//        return audioFileCompleteInfo;
        return null;
    }
}
