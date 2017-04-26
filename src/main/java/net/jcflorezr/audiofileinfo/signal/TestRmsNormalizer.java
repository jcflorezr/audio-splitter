package net.jcflorezr.audiofileinfo.signal;// Copyright 2014 Christian d'Heureuse, Inventec Informatik AG, Zurich, Switzerland
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

import biz.source_code.dsp.model.AudioSignal;
import biz.source_code.dsp.sound.AudioIo;
import net.jcflorezr.model.audioclips.AudioClipInfo;
import net.jcflorezr.model.audiocontent.signal.RmsSignal;
import org.apache.commons.lang3.ArrayUtils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.jcflorezr.util.AudioUtils.formatAudioTimeWithMilliseconds;
import static org.apache.commons.lang3.StringUtils.substringAfter;


// TODO this class cannot be referenced as Singleton
public class TestRmsNormalizer {

    private RmsNormalizer rmsNormalizer = new RmsNormalizer();

    private static final int GROUP_LENGTH = 13;
    private int groupNumber = 1;
    private float groupDurationInSeconds;

    private String audioDurationDigitsFormat;

    public static void main(String[] args) throws Exception {
        new TestRmsNormalizer().r();
    }

    public void r() throws IOException, UnsupportedAudioFileException {
        String inputFileName = "/Users/juancamiloroman/OneDrive/dsp-collection-maven copy/2016103017.wav";
//        String inputFileName = "/Users/juancamiloroman/OneDrive/dsp-collection-maven copy/3818-2_00.wav";
//        String inputFileName = "/Users/juancamiloroman/OneDrive/dsp-collection-maven copy/161-1_30.wav";


//        String outputFileName = "/Users/juancamiloroman/Desktop/2016103017_2_2.wav";
//        String outputFileName = "/Users/juancamiloroman/Desktop/3818-2_00_3_2.wav";
//        String outputFileName = "/Users/juancamiloroman/Desktop/161-1_30_2";

        List<float[][]> audioFileSignalData = new ArrayList<>();
        audioFileSignalData.add(new float[1][]);
        List<RmsSignal> rmsInfoList = new ArrayList<>();
        int samplingRate;
        int segmentSize;
        try(AudioInputStream stream = new AudioIo().getAudioInputStream(inputFileName)) {
            samplingRate = Math.round(stream.getFormat().getSampleRate());
            int duration = (int) stream.getFrameLength();
            int durationInSeconds = duration / samplingRate;
            segmentSize = samplingRate / 10; // 100ms

            for (int currentSecond = 0; currentSecond <= durationInSeconds; currentSecond++) {
                int nextPosition = samplingRate * currentSecond;
                stream.mark(nextPosition);

                int difference = duration - nextPosition;
                boolean isLastSecond = difference < samplingRate;
                int blockFrames = isLastSecond ? difference : samplingRate;
                int totalFrames = blockFrames;
                AudioSignal currentSecondSignal = new AudioIo().retrieveAudioSignalFromWavFile(stream, totalFrames, blockFrames);

                float[][] previousAndCurrentSignalData = joinAudioSignals(audioFileSignalData.get(0), currentSecondSignal.getData());
                audioFileSignalData.set(0, previousAndCurrentSignalData);

                List<RmsSignal> rmsList = rmsNormalizer.normalize(currentSecondSignal.getData(), segmentSize, samplingRate);

                System.out.println("=================================== " + currentSecond);

                for (RmsSignal rmsSignal : rmsList) {
                    rmsSignal.setPosition(samplingRate, currentSecond);
                }
                rmsInfoList.addAll(rmsList);
//            new AudioIo().saveAudioFile(outputFileName + "0" + currentSecond, ".wav", currentSecondSignal);
            }
        }

        AudioSignal audioFileSignal = new AudioSignal(samplingRate, audioFileSignalData.get(0));
        audioDurationDigitsFormat = getNumOfDigitsFormat(audioFileSignal);

        List<AudioClipInfo> audioClips = retrieveSoundZones(rmsInfoList, segmentSize, samplingRate);
        System.out.println();

//        new AudioIo().saveAudioFile(outputFileName+"TEST", ".wav", audioFileSignal);
    }

    private float[][] joinAudioSignals(float[][] audioClipSignal, float[][] groupSeparatorSignal) {
        float[][] jointSignalData = new float[audioClipSignal.length][];
        for (int i = 0; i < audioClipSignal.length; i++) {
            jointSignalData[i] = ArrayUtils.addAll(audioClipSignal[i], groupSeparatorSignal[i]);
        }
        return jointSignalData;
    }

    private List<AudioClipInfo> retrieveSoundZones(List<RmsSignal> rmsInfoList, int segmentSize, int samplingRate) {
        List<AudioClipInfo> audioClips = new ArrayList<>();
        int activeCounter = 0;
        int inactiveCounter = 0;
        int startActiveZonePosition = 0;
        int endActiveZonePosition;
        RmsSignal previousRmsInfo = new RmsSignal();
        for (RmsSignal rmsInfo : rmsInfoList) {
            if (rmsInfo.getPosition() % samplingRate == 0) {
                continue;
            }
            if (rmsInfo.isActive()) {
                if (++activeCounter == 3) {
                    startActiveZonePosition = previousRmsInfo.getPosition();
                    if (startActiveZonePosition > (segmentSize * 3)) {
                        startActiveZonePosition -= (segmentSize * 3);
                    }
                }
                inactiveCounter = 0;
            } else if (++inactiveCounter == 3) {
                if (activeCounter >= 3) {
                    endActiveZonePosition = previousRmsInfo.getPosition();
                    audioClips.add(generateSoundZoneInfo(startActiveZonePosition, endActiveZonePosition, samplingRate));
                }
                activeCounter = 0;
            }
            previousRmsInfo = rmsInfo;
        }
        return audioClips;
    }

    private AudioClipInfo generateSoundZoneInfo(int startPosition, int endPosition, int samplingRate) {
//        int startPosition = endPositionPreviousSoundZone;
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
//        endPositionPreviousSoundZone = endPosition;
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
