package net.jcflorezr.audiofileinfo;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.jcflorezr.util.AudioUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FilenameUtils.class, AudioUtils.class})
public class AudioConverterServiceTest {

    @Test
    public void shouldConvertFileToWav() {

    }

    private void createFile(String audioFileName) {

    }


}