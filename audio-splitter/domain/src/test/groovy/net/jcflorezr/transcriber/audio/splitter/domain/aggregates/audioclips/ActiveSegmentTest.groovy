package net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo
import spock.lang.Specification
import spock.lang.Unroll

class ActiveSegmentTest extends Specification {

    private static Class<ActiveSegmentTest> thisClass = ActiveSegmentTest.class
    private static String testResourcesPath = thisClass.getResource("/source-file-info").getPath()
    private static String expectedAudioContentFileName = "audio-content-info.json"
    private static ObjectMapper mapper = new ObjectMapper().registerModule(new KotlinModule())

    @Unroll
    def "assert active segment"() {
        given:
        def audioContentInfo = getExpectedAudioContentInfo()

        when:
        def actualActiveSegment =
            new ActiveSegment.Companion().createNew("audio-file-name", segmentStart, segmentEnd, audioContentInfo)

        then:
        verifyAll {
            actualActiveSegment.segmentStartInSeconds == startInSeconds.toFloat()
            actualActiveSegment.segmentEndInSeconds == endInSeconds.toFloat()
            actualActiveSegment.hours == hours
            actualActiveSegment.minutes == minutes
            actualActiveSegment.seconds == seconds
            actualActiveSegment.tenthsOfSecond == tenths
            actualActiveSegment.duration == duration.toFloat()
        }

        where:
        segmentStart | segmentEnd | startInSeconds | endInSeconds | hours | minutes | seconds | tenths | duration
        2205         | 13230      | 0.1            | 0.6          | 0     | 0       | 0       | 1      | 0.5
        33075        | 46305      | 1.5            | 2.1          | 0     | 0       | 1       | 5      | 0.6
        13697460     | 13712895   | 621.2          | 621.9        | 0     | 10      | 21      | 2      | 0.7
        181594980    | 201607560  | 8235.6         | 9143.2       | 2     | 17      | 15      | 6      | 907.6
    }

    private static AudioContentInfo getExpectedAudioContentInfo() {
        String audioContentPath = "$testResourcesPath/$expectedAudioContentFileName"
        return mapper.readValue(new File(audioContentPath), AudioContentInfo.class)
    }
}
