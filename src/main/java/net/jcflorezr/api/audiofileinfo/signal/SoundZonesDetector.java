package net.jcflorezr.api.audiofileinfo.signal;

import biz.source_code.dsp.model.AudioSignal;
import net.jcflorezr.model.audioclips.AudioFileClip;

import java.util.List;

public interface SoundZonesDetector {

    List<AudioFileClip> retrieveAudioClipsInfo(String audioFileName, AudioSignal audioSignal);

}
