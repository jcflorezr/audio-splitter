package net.jcflorezr.audiofileinfo.signal;

import net.jcflorezr.model.audiocontent.signal.RmsSignal;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A simple signal level normalizer.
 * <p>
 * <p>
 * To determine the amplification factor, the signal is divided into segments and for each segment the
 * <a href="http://en.wikipedia.org/wiki/Root_mean_square">RMS</a> value is computed.
 * The maximum RMS value is used to adjust the amplitude of the signal.
 * For speech audio, a segment size of 100 ms might be reasonable.
 */
public class RmsNormalizer {

    private static final float TARGET_RMS = 0.5F;
    private static final double THRESHOLD = 0.03;

    public List<RmsSignal> normalize(float[][] signals, int segmentSize, int samplingRate) {
        int channel = 0;
        List<Double> rmsLevels = retrieveRmsLevels(signals[channel], segmentSize);
        double maxRms = rmsLevels.stream().max(Comparator.comparing(x -> x)).orElse(0.0);
        if (maxRms == 0) {
            return new ArrayList<>();
        }
        double factor = TARGET_RMS / maxRms;
        amplifySignal(signals, factor);
        return retrieveRmsInfo(signals[channel], segmentSize, samplingRate);
    }

    private List<Double> retrieveRmsLevels(float[] signal, int segmentSize) {
        List<Double> rmsSignals = new ArrayList<>();
        int pos = 0;
        while (pos < signal.length) {
            int endPos = (pos + segmentSize * 5 / 3 > signal.length) ? signal.length : pos + segmentSize;
            // If the last segment is less than 2/3 of the segment size, we include it in the previous segment.
            rmsSignals.add(computeRms(signal, pos, endPos - pos));
            pos = endPos;
        }
        return rmsSignals;
    }

    private List<RmsSignal> retrieveRmsInfo(float[] signal, int segmentSize, int samplingRate) {
        List<RmsSignal> rmsSignals = new ArrayList<>();
        int pos = 0;
        double prevRms = 0.0;
        double prevDiff = 0.0;
        while (pos < signal.length) {
            int endPos = (pos + segmentSize * 5 / 3 > signal.length) ? signal.length : pos + segmentSize;
            // If the last segment is less than 2/3 of the segment size, we include it in the previous segment.
            double rms = computeRms(signal, pos, endPos - pos);
            float positionInSeconds = (float) pos / samplingRate;
            int position = endPos;

            double diff = prevRms - rms;
            boolean active = Math.abs(prevDiff - diff) > THRESHOLD;

            DecimalFormat d = new DecimalFormat("#.###");
            float f = (float)pos/samplingRate;
            System.out.print("\nrms: " + rms + " - " + d.format(f) + " - " + pos + " [" + d.format(diff) + "]" + " (" + (active ? active : "") + ")");

            rmsSignals.add(new RmsSignal(rms, positionInSeconds, position, active));

            prevRms = rms;
            pos = endPos;
            prevDiff = diff;
        }
        return rmsSignals;
    }

    private double computeRms(float[] signal, int startPos, int len) {
        double a = 0;
        for (int p = startPos; p < startPos + len; p++) {
            a += signal[p] * signal[p];
        }
        return Math.sqrt(a / len);
    }

    private void amplifySignal(float[][] signals, double factor) {
        for (int channel = 0; channel < signals.length; channel++) {
            amplifySignal(signals[channel], factor);
        }
    }

    private void amplifySignal(float[] signal, double factor) {
        for (int p = 0; p < signal.length; p++) {
            signal[p] *= factor;
        }
    }

}
