package net.jcflorezr.audiofileinfo;

import net.jcflorezr.api.audiofileinfo.AudioFileInfoService;
import net.jcflorezr.api.audiofileinfo.signal.SoundZonesDetector;
import net.jcflorezr.audiofileinfo.signal.SoundZonesDetectorImpl;
import net.jcflorezr.model.audiocontent.AudioContent;
import net.jcflorezr.model.audiocontent.AudioFileInfo;
import net.jcflorezr.model.request.AudioFileLocation;
import org.springframework.stereotype.Service;

@Service
public class AudioFileInfoServiceImpl implements AudioFileInfoService {

    private AudioConverterService audioConverterService = new AudioConverterService();
    private AudioContentService audioContentService = new AudioContentService();
    private SoundZonesDetector soundZonesDetector = new SoundZonesDetectorImpl();

    @Override
    public AudioFileInfo generateAudioFileInfo(AudioFileLocation audioFileLocation, boolean grouped) throws Exception {
        String convertedAudioFileName = audioConverterService.convertFileToWavIfNeeded(audioFileLocation.getAudioFileName());
        audioFileLocation.setConvertedAudioFileName(convertedAudioFileName);
        AudioFileInfo audioFileInfo = new AudioFileInfo(audioFileLocation);
        AudioContent audioContent = audioContentService.retrieveAudioContent(audioFileInfo);
        audioFileInfo.setAudioContent(audioContent);
        audioFileInfo.setAudioClipsInfo(soundZonesDetector.retrieveAudioClipsInfo(audioContent.getOriginalAudioSignal()));
        return audioFileInfo;
    }
}
