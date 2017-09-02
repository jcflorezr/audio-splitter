package net.jcflorezr.audiofileinfo;

import net.jcflorezr.api.audiofileinfo.AudioFileInfoService;
import net.jcflorezr.api.audiofileinfo.signal.SoundZonesDetector;
import net.jcflorezr.model.audiocontent.AudioContent;
import net.jcflorezr.model.audiocontent.AudioFileCompleteInfo;
import net.jcflorezr.model.request.AudioFileBasicInfo;
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
    public AudioFileCompleteInfo generateAudioFileInfo(AudioFileBasicInfo audioFileBasicInfo, boolean grouped) throws Exception {
        String convertedAudioFileName = audioConverterService.convertFileToWavIfNeeded(audioFileBasicInfo.getAudioFileName());
        audioFileBasicInfo.setConvertedAudioFileName(convertedAudioFileName);
        AudioFileCompleteInfo audioFileCompleteInfo = new AudioFileCompleteInfo(audioFileBasicInfo);
        AudioContent audioContent = audioContentService.retrieveAudioContent(audioFileCompleteInfo);
        audioFileCompleteInfo.setAudioContent(audioContent);
        audioFileCompleteInfo.setAudioClipsInfo(soundZonesDetector.retrieveAudioClipsInfo(audioFileBasicInfo.getAudioFileName(), audioContent.getOriginalAudioSignal()));
        return audioFileCompleteInfo;
    }
}
