package net.jcflorezr.api.endpoint;

import net.jcflorezr.model.endpoint.AudioFileBasicInfoEntity;
import net.jcflorezr.model.endpoint.AudioSplitterResponse;

public abstract class AudioSplitterByGroup extends AudioSplitter {

    protected abstract AudioSplitterResponse generateAudioClipsWithSeparator(AudioFileBasicInfoEntity audioFileBasicInfoEntity);

    protected abstract AudioSplitterResponse generateAudioMonoClipsWithSeparator(AudioFileBasicInfoEntity audioFileBasicInfoEntity);

}
