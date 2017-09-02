package net.jcflorezr.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"net.jcflorezr.api", "net.jcflorezr.audioclips", "net.jcflorezr.audiofileinfo", "net.jcflorezr.persistence", "biz.source_code.dsp.sound"})
public class RootConfig {
}
