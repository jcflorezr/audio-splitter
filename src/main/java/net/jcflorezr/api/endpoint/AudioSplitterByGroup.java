package net.jcflorezr.api.endpoint;

import net.jcflorezr.model.request.AudioFileBasicInfo;
import net.jcflorezr.model.response.AudioSplitterResponse;

public abstract class AudioSplitterByGroup extends AudioSplitter {

    protected abstract AudioSplitterResponse generateAudioClipsWithSeparator(AudioFileBasicInfo audioFileBasicInfo);

    protected abstract AudioSplitterResponse generateAudioMonoClipsWithSeparator(AudioFileBasicInfo audioFileBasicInfo);

}
