// Copyright 2013 Christian d'Heureuse, Inventec Informatik AG, Zurich, Switzerland
// www.source-code.biz, www.inventec.ch/chdh
//
// This module is multi-licensed and may be used under the terms
// of any of the following licenses:
//
//  EPL, Eclipse Public License, V1.0 or later, http://www.eclipse.org/legal
//  LGPL, GNU Lesser General Public License, V2.1 or later, http://www.gnu.org/licenses/lgpl.html
//
// Please contact the author if you need another license.
// This module is provided "as is", without warranties of any kind.

package net.jcflorezr.audiofileinfo.signal;

import biz.source_code.dsp.model.AudioSignal;
import biz.source_code.dsp.signal.EnvelopeDetector;
import net.jcflorezr.model.audioclips.GroupAudioClipInfo;
import net.jcflorezr.model.audioclips.SingleAudioClipInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.jcflorezr.util.AudioUtils.formatAudioTimeWithMilliseconds;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.substringAfter;

/**
 * An activity detector that can be used to subdivide an audio signal into zones with sound and zones with SILENCE.
 * The difference between this class and ActivityDetector.java is that this class does not discard the SILENCE gaps
 * between activity zones, each audio clip generated will keep the SILENCE gaps both at the beginning and at the end
 * as it was recorded in the original audio file.
 * <p>
 * <p>
 * <p>
 * The following model is used to find ACTIVE and silent zones:
 * The input signal is divided into segments according to whether the signal envelope level is above or
 * below the threshold level.
 * These segments are classified into the following types:
 * <ul>
 * <li>ACTIVE: The envelope level is above the threshold and the zone is equal or longer than the minimum for activity.
 * <li>SILENCE: The envelope level is below the threshold and the zone is equal or longer than the minimum for SILENCE.
 * <li>UNDEF: The zone is too small. The envelope level of the zone may be above or below the threshold (but not mixed).
 * </ul>
 * <p>
 * "UNDEF" segments are then eliminated using the following rules:
 * <ul>
 * <li>Adjacent "UNDEF" segments are combined.
 * <li>An "UNDEF" segment that is adjacent to an "ACTIVE" segment is converted to "ACTIVE".
 * <li>An "UNDEF" segment that lies between two "SILENCE" segments is converted to "SILENCE".
 * <li>The start and the end of the signal are regarded as "SILENCE".
 * </ul>
 * <p>
 * The resulting "ACTIVE" segments are combined into the ACTIVE zones.
 */
public class SoundZonesDetector {

    private static final int GROUP_LENGTH = 13;
    private int groupNumber = 1;
    private int groupDurationInSeconds;

    private float thresholdLevel = 0.01F;    // sound envelope threshold
    private float minActivityTime = 0.25F;   // minimum activity segment time in seconds
    private float minSilenceTime = 0.1F;    // minimum SILENCE segment time in seconds

    private int minActivityLen;
    private int minSilenceLen;
    private int samplingRate;
    private String audioDurationDigitsFormat;
    private int endPositionPreviousSoundZone;
    private float[] signalEnvelope;
    private int pos;                          // current position in signal

    private void init(AudioSignal audioSignal) {
        this.samplingRate = audioSignal.getSamplingRate();
        this.minActivityLen = Math.round(minActivityTime * samplingRate);
        this.minSilenceLen = Math.round(minSilenceTime * samplingRate);
        this.signalEnvelope = getSignalEnvelope(audioSignal);
        this.audioDurationDigitsFormat = getDigitsFormatOfAudioDuration();
    }

    private float[] getSignalEnvelope(AudioSignal audioSignal) {
        EnvelopeDetector envelopeDetector = new EnvelopeDetector(samplingRate);
        return envelopeDetector.process(audioSignal.getData()[0]);
    }

    private String getDigitsFormatOfAudioDuration() {
        return getNumOfDigitsFormat(Math.round(signalEnvelope.length / samplingRate));
    }

    private String getNumOfDigitsFormat(int number) {
        return "%0" + String.valueOf(number).length() + "d";
    }

    /**
     * Processes the signal envelope and returns the positions of the sound zones.
     *
     * @return a list with the start position of the sound zones.
     */
    public List<SingleAudioClipInfo> getAudioSoundZones(AudioSignal audioSignal) {
        init(audioSignal);
        List<SingleAudioClipInfo> singleAudioFileSoundZones = new ArrayList<>();
        pos = 0;
        int activeStartPos = -1;                                // start position of ACTIVE zone or -1
        int undefStartPos = -1;                                 // start position of undefined zone or -1
        while (pos < signalEnvelope.length) {
            int segmentStartPos = pos;                           // start position of current segment
            SegmentType segmentType = scanSegment();
            switch (segmentType) {
                case SILENCE: {
                    if (activeStartPos != -1) {
                        singleAudioFileSoundZones.add(addAudioFileSoundZone(segmentStartPos));
                        activeStartPos = -1;
                    }
                    undefStartPos = -1;
                    break;
                }
                case ACTIVE: {
                    if (activeStartPos == -1) {
                        activeStartPos = (undefStartPos != -1) ? undefStartPos : segmentStartPos;
                    }
                    break;
                }
                case UNDEF: {
                    if (undefStartPos == -1) {
                        undefStartPos = segmentStartPos;
                    }
                    break;
                }
                default:
                    throw new AssertionError();
            }
        }
        if (activeStartPos != -1) {
            singleAudioFileSoundZones.add(addAudioFileSoundZone(pos));
        }
        return singleAudioFileSoundZones;
    }

    public List<GroupAudioClipInfo> retrieveGroupedAudioSoundZones(List<SingleAudioClipInfo> singleAudioFileSoundZones) {
        Map<Integer, List<SingleAudioClipInfo>> groupedSoundZones = singleAudioFileSoundZones.stream()
                .collect(Collectors.groupingBy(SingleAudioClipInfo::getGroupNumber));
        return groupedSoundZones.entrySet().stream()
                .map((groupedSoundZone) -> getAudioFileSoundZoneGroup(groupedSoundZone.getValue()))
                .collect(toList());
    }

    private enum SegmentType {ACTIVE, SILENCE, UNDEF}

    private SegmentType scanSegment() {
        int startPos = pos;
        if (pos >= signalEnvelope.length) {
            throw new AssertionError();
        }
        boolean active = signalEnvelope[pos++] >= thresholdLevel;
        while (pos < signalEnvelope.length && (signalEnvelope[pos] >= thresholdLevel) == active) {
            pos++;
        }
        int minLen = active ? minActivityLen : minSilenceLen;
        if (pos - startPos < minLen) {
            return SegmentType.UNDEF;
        }
        return active ? SegmentType.ACTIVE : SegmentType.SILENCE;
    }

    private SingleAudioClipInfo addAudioFileSoundZone(int endPosition) {
        int startPosition = endPositionPreviousSoundZone;
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

        SingleAudioClipInfo singleAudioFileSoundZone = new SingleAudioClipInfo.SingleAudioSoundZoneInfoBuilder()
                .groupNumber(groupNumber)
                .startPosition(startPosition)
                .startPositionInSeconds(startPositionInSeconds)
                .endPosition(endPosition)
                .endPositionInSeconds(endPositionInSeconds)
                .durationInSeconds(audioClipDurationInSeconds)
                .suggestedAudioFileName(suggestedAudioClipName)
                .hours(hours)
                .minutes(minutes)
                .seconds(seconds)
                .milliseconds(milliseconds)
                .build();

        if (groupDurationInSeconds >= GROUP_LENGTH) {
            groupDurationInSeconds = 0;
            groupNumber++;
        }
        endPositionPreviousSoundZone = endPosition;
        return singleAudioFileSoundZone;
    }

    private String getSuggestedAudioClipName(float startPositionInSeconds) {
        String[] startPosInSecondsString = String.valueOf(startPositionInSeconds).split("\\.");
        startPosInSecondsString[0] = String.format(audioDurationDigitsFormat, Integer.parseInt(startPosInSecondsString[0]));
        return startPosInSecondsString[0] + "_" + startPosInSecondsString[1];
    }

    private GroupAudioClipInfo getAudioFileSoundZoneGroup(List<SingleAudioClipInfo> soundZoneList) {
        float groupDurationInSeconds = (float) soundZoneList.stream()
                .mapToDouble(soundZone -> soundZone.getDurationInSeconds())
                .sum();
        SingleAudioClipInfo firstSoundZone = soundZoneList.get(0);
        return new GroupAudioClipInfo.GroupAudioSoundZonesInfoBuilder()
                .suggestedAudioFileName(firstSoundZone.getSuggestedAudioFileName())
                .startPositionInSeconds(firstSoundZone.getStartPositionInSeconds())
                .durationInSeconds(groupDurationInSeconds)
                .hours(firstSoundZone.getHours())
                .minutes(firstSoundZone.getMinutes())
                .seconds(firstSoundZone.getSeconds())
                .milliseconds(firstSoundZone.getMilliseconds())
                .numSoundZones(soundZoneList.size())
                .singleAudioSoundZonesInfo(soundZoneList)
                .build();
    }

}
