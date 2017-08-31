package net.jcflorezr.api.audiofileinfo.signal;

import biz.source_code.dsp.model.AudioSignal;
import net.jcflorezr.model.audioclips.AudioClipInfo;

import java.util.List;

public interface SoundZonesDetector {

    List<AudioClipInfo> retrieveAudioClipsInfo(String audioFileName, AudioSignal audioSignal);

}
