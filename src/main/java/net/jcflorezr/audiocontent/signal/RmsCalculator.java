package net.jcflorezr.audiocontent.signal;

import net.jcflorezr.model.audiocontent.signal.RmsSignalInfo;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * The signal is divided into segments and for each segment the
 * <a href="http://en.wikipedia.org/wiki/Root_mean_square">RMS</a> value is computed.
 * For speech audio, a segment size of 100 ms might be reasonable.
 */
@Service
public class RmsCalculator {

    private static final double SILENCE_THRESHOLD = 0.001;
    private static final double ACTIVE_THRESHOLD = 0.03;

    public List<RmsSignalInfo> retrieveRmsInfo(float[][] signals, int segmentSize, int samplingRate) {
        int channel = 0;
        return retrieveRmsInfo(signals[channel], segmentSize, samplingRate);
    }

    private List<RmsSignalInfo> retrieveRmsInfo(float[] signal, int segmentSize, int samplingRate) {
        List<RmsSignalInfo> rmsSignalInfo = new ArrayList<>();
        int pos = 0;
        double prevRms = 0.0;
        double prevDiff = 0.0;
        while (pos < signal.length) {
            int endPos = (pos + segmentSize * 5 / 3 > signal.length) ? signal.length : pos + segmentSize;
            // If the last segment is less than 2/3 of the segment size, we include it in the previous segment.
            DecimalFormat df = new DecimalFormat("#.###");
            double rms = Double.parseDouble(df.format(computeRms(signal, pos, endPos - pos)));
            float positionInSeconds = (float) pos / samplingRate;

            double diff = Double.parseDouble(df.format(prevRms - rms));
            boolean silence = Math.abs(diff) <= SILENCE_THRESHOLD;
            double deepDiff = Double.parseDouble(df.format(prevDiff - diff));
            boolean active = Math.abs(deepDiff) >= ACTIVE_THRESHOLD;

            rmsSignalInfo.add(new RmsSignalInfo(rms, positionInSeconds, pos, silence, active));

            prevRms = rms;
            pos = endPos;
            prevDiff = diff;
        }
        return rmsSignalInfo;
    }

    private double computeRms(float[] signal, int startPos, int len) {
        double a = 0;
        for (int p = startPos; p < startPos + len; p++) {
            a += signal[p] * signal[p];
        }
        return Math.sqrt(a / len);
    }

}
