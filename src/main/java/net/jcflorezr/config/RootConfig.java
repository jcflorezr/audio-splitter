package net.jcflorezr.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"net.jcflorezr.api.endpoint", "net.jcflorezr.audioclips", "net.jcflorezr.audiocontent", "net.jcflorezr.persistence", "biz.source_code.dsp.sound"})
public class RootConfig {
}
