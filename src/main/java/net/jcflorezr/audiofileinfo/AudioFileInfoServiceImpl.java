package net.jcflorezr.audiofileinfo;

import net.jcflorezr.api.audiofileinfo.AudioFileInfoService;
import net.jcflorezr.audioclips.signal.SoundZonesDetector;
import net.jcflorezr.model.audioclips.GroupAudioClipInfo;
import net.jcflorezr.model.audiocontent.AudioContent;
import net.jcflorezr.model.audiocontent.AudioFileInfo;
import net.jcflorezr.model.request.AudioFileLocation;
import java.util.List;

public class AudioFileInfoServiceImpl implements AudioFileInfoService {

    private AudioConverterService audioConverterService = new AudioConverterService();
    private AudioContentService audioContentService = new AudioContentService();

    @Override
    public AudioFileInfo generateAudioFileInfo(AudioFileLocation audioFileLocation, boolean grouped) throws Exception {
        AudioFileInfo audioFileInfo = new AudioFileInfo(audioFileLocation);
        try {
            String convertedAudioFileName = audioConverterService.convertFileToWavIfNeeded(audioFileLocation.getAudioFileName());
            audioFileInfo.setConvertedAudioFileName(convertedAudioFileName);
            AudioContent audioContent = audioContentService.retrieveAudioContent(audioFileInfo, audioFileLocation);
            audioFileInfo.setAudioContent(audioContent);

            SoundZonesDetector soundZonesDetector = new SoundZonesDetector(audioContent.getOriginalAudioSignal());
            audioFileInfo.setSingleAudioSoundZones(soundZonesDetector.getAudioSoundZones());

            if (grouped) {
                List<GroupAudioClipInfo> groupedAudioFileSoundZones =
                        soundZonesDetector.retrieveGroupedAudioSoundZones(audioFileInfo.getSingleAudioSoundZones());
                audioFileInfo.setGroupedAudioFileSoundZones(groupedAudioFileSoundZones);
            }

            return audioFileInfo;
        } finally {

        }
    }
}
