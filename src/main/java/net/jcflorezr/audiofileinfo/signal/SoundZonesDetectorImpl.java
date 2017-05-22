package net.jcflorezr.audiofileinfo.signal;

import biz.source_code.dsp.model.AudioSignal;
import net.jcflorezr.api.audiofileinfo.signal.SoundZonesDetector;
import net.jcflorezr.model.audioclips.AudioClipInfo;
import net.jcflorezr.model.audiocontent.signal.RmsSignalInfo;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static net.jcflorezr.util.AudioUtils.formatAudioTimeWithMilliseconds;
import static org.apache.commons.lang3.StringUtils.substringAfter;


@Service
@Scope("prototype")
public class SoundZonesDetectorImpl implements SoundZonesDetector {

    private static final int MAX_ACTIVE_COUNTER = 80;
    private static final int GROUP_LENGTH = 13;

    private int groupNumber = 1;
    private float groupDurationInSeconds;
    private String audioDurationDigitsFormat;

    private RmsCalculator rmsCalculator = new RmsCalculator();

    @Override
    public List<AudioClipInfo> retrieveAudioClipsInfo(AudioSignal audioSignal) {
        int samplingRate = audioSignal.getSamplingRate();
        int segmentSize = samplingRate / 10;
        List<RmsSignalInfo> rmsInfoList = rmsCalculator.retrieveRmsInfo(audioSignal.getData(), segmentSize, samplingRate);
        audioDurationDigitsFormat = getNumOfDigitsFormat(audioSignal);
        return retrieveSoundZonesBySilenceSegments(rmsInfoList, segmentSize, samplingRate);
    }

    private List<AudioClipInfo> retrieveSoundZonesBySilenceSegments(List<RmsSignalInfo> rmsInfoList, int segmentSize, int samplingRate) {
        List<AudioClipInfo> audioClips = new ArrayList<>();
        int silenceCounter = 0;
        int activeCounter = 0;
        int startActiveZonePosition = 0;
        int endActiveZonePosition;
        Iterator<RmsSignalInfo> rmsSignalIterator = rmsInfoList.listIterator();
        while (rmsSignalIterator.hasNext()) {
            RmsSignalInfo rmsInfo = rmsSignalIterator.next();
            boolean isLastSegment = !rmsSignalIterator.hasNext();
            if (rmsInfo.isPossibleSilence() || isLastSegment) {
                if (++silenceCounter == 2 || isLastSegment) {
                    if (activeCounter > 2) {
                        if (activeCounter >= MAX_ACTIVE_COUNTER) {
                            int from = startActiveZonePosition / segmentSize;
                            int to = rmsInfo.getPosition() / segmentSize;
                            audioClips.addAll(retrieveSoundZonesByActiveSegments(rmsInfoList.subList(from, to), segmentSize, samplingRate));
                        } else {
                            startActiveZonePosition = Math.max(startActiveZonePosition - (segmentSize * 2), 0);
                            endActiveZonePosition = rmsInfo.getPosition();
                            audioClips.add(generateSoundZoneInfo(startActiveZonePosition, endActiveZonePosition, samplingRate));
                        }
                    } else {
                        startActiveZonePosition = rmsInfo.getPosition();
                    }
                    activeCounter = 0;
                } else if (silenceCounter < 2) {
                    activeCounter++;
                }
            } else {
                if (++activeCounter == 1) {
                    startActiveZonePosition = rmsInfo.getPosition();
                }
                silenceCounter = 0;
            }
        }
        return audioClips;
    }

    private List<AudioClipInfo> retrieveSoundZonesByActiveSegments(List<RmsSignalInfo> rmsInfoList, int segmentSize, int samplingRate) {
        List<AudioClipInfo> audioClips = new ArrayList<>();
        int activeCounter = 0;
        int inactiveCounter = 0;
        int startActiveZonePosition = 0;
        int endActiveZonePosition;
        Iterator<RmsSignalInfo> rmsSignalIterator = rmsInfoList.listIterator();
        while (rmsSignalIterator.hasNext()) {
            RmsSignalInfo rmsInfo = rmsSignalIterator.next();
            boolean isLastSegment = !rmsSignalIterator.hasNext();
            if (rmsInfo.isPossibleActive() && !isLastSegment) {
                if (++activeCounter == 3) {
                    startActiveZonePosition = rmsInfo.getPosition();
                    if (startActiveZonePosition > (segmentSize * 4)) {
                        startActiveZonePosition -= (segmentSize * 4);
                    }
                }
                inactiveCounter = 0;
            } else if (++inactiveCounter == 3 || isLastSegment) {
                if (activeCounter >= 3) {
                    endActiveZonePosition = rmsInfo.getPosition();
                    audioClips.add(generateSoundZoneInfo(startActiveZonePosition, endActiveZonePosition, samplingRate));
                }
                activeCounter = 0;
            }
        }
        return audioClips;
    }

    private AudioClipInfo generateSoundZoneInfo(int startPosition, int endPosition, int samplingRate) {
        float startPositionInSeconds = formatAudioTimeWithMilliseconds((float) startPosition / samplingRate);
        float endPositionInSeconds = formatAudioTimeWithMilliseconds((float) endPosition / samplingRate);
        int startPositionInSecondsInt = (int) startPositionInSeconds;

        float audioClipDurationInSeconds = formatAudioTimeWithMilliseconds((float) (endPosition - startPosition) / samplingRate);
        groupDurationInSeconds += audioClipDurationInSeconds;

        String suggestedAudioClipName = getSuggestedAudioClipName(startPositionInSeconds);

        int hours = startPositionInSecondsInt / 3600;
        int minutes = (startPositionInSecondsInt % 3600) / 60;
        int seconds = startPositionInSecondsInt % 60;
        int milliseconds = Integer.parseInt(substringAfter(suggestedAudioClipName, "_"));

        AudioClipInfo singleAudioFileSoundZone = new AudioClipInfo.SingleAudioSoundZoneInfoBuilder()
                .groupNumber(groupNumber)
                .startPosition(startPosition)
                .startPositionInSeconds(startPositionInSeconds)
                .endPosition(endPosition)
                .endPositionInSeconds(endPositionInSeconds)
                .durationInSeconds(audioClipDurationInSeconds)
                .suggestedAudioClipName(suggestedAudioClipName)
                .hours(hours)
                .minutes(minutes)
                .seconds(seconds)
                .milliseconds(milliseconds)
                .build();

        if (groupDurationInSeconds >= GROUP_LENGTH) {
            groupDurationInSeconds = 0;
            groupNumber++;
        }
        return singleAudioFileSoundZone;
    }

    private String getSuggestedAudioClipName(float startPositionInSeconds) {
        String[] startPosInSecondsString = String.valueOf(startPositionInSeconds).split("\\.");
        startPosInSecondsString[0] = String.format(audioDurationDigitsFormat, Integer.parseInt(startPosInSecondsString[0]));
        return startPosInSecondsString[0] + "_" + startPosInSecondsString[1];
    }

    private String getNumOfDigitsFormat(AudioSignal audioSignal) {
        return "%0" + String.valueOf(Math.round(audioSignal.getLength() / audioSignal.getSamplingRate())).length() + "d";
    }

}
