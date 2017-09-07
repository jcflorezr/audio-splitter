package net.jcflorezr.api.audiocontent.signal;

import biz.source_code.dsp.model.AudioSignal;
import net.jcflorezr.model.audioclips.AudioFileClipEntity;

import java.util.List;

public interface SoundZonesDetector {

    List<AudioFileClipEntity> retrieveAudioClipsInfo(String audioFileName, AudioSignal audioSignal);

}
