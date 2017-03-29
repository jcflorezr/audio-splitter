package biz.source_code.dsp.model;

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
    public int getLength() {
        return data[0].length;
    }

    /**
     * Returns the number of channels.
     */
    public int getChannels() {
        return data.length;
    }

}
