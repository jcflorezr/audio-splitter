package biz.source_code.dsp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Arrays;

/**
 * A class for storing an audio signal in memory.
 */
public class AudioSignal {

    /**
     * The sampling rate in Hz
     */
    private int samplingRate;

    /**
     * The audio signal sample values, per channel separately.
     * The normal value range is -1 .. 1.
     */
    private float[][] data;

    public AudioSignal() {
    }

    public AudioSignal(int samplingRate, float[][] data) {
        this.samplingRate = samplingRate;
        this.data = data;
    }

    public int getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(int samplingRate) {
        this.samplingRate = samplingRate;
    }

    public float[][] getData() {
        return data;
    }

    public void setData(float[][] data) {
        this.data = data;
    }

    /**
     * Returns the signal length in samples.
     */
    @JsonIgnore
    public int getLength() {
        return data[0].length;
    }

    /**
     * Returns the number of channels.
     */
    @JsonIgnore
    public int getChannels() {
        return data.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AudioSignal that = (AudioSignal) o;

        if (samplingRate != that.samplingRate) return false;
        return Arrays.deepEquals(data, that.data);
    }

    @Override
    public int hashCode() {
        int result = samplingRate;
        result = 31 * result + Arrays.deepHashCode(data);
        return result;
    }

    @Override
    public String toString() {
        return "AudioSignal{" +
                "samplingRate=" + samplingRate +
                ", data=" + Arrays.asList(data) +
                '}';
    }
}
