package net.jcflorezr.api.endpoint;

import net.jcflorezr.model.request.AudioFileLocation;
import net.jcflorezr.model.response.AudioSplitterResponse;

public abstract class AudioSplitterByGroup extends AudioSplitter {

    protected abstract AudioSplitterResponse generateAudioClipsWithSeparator(AudioFileLocation audioFileLocation);

    protected abstract AudioSplitterResponse generateAudioMonoClipsWithSeparator(AudioFileLocation audioFileLocation);

}
