package net.jcflorezr.model.audiocontent.signal;

public class RmsSignalInfo {

    private double rms;
    private float positionInSeconds;
    private int position;
    private boolean possibleSilence;
    private boolean possibleActive;

    public RmsSignalInfo() {
    }

    public RmsSignalInfo(double rms, float positionInSeconds, int position, boolean possibleSilence, boolean possibleActive) {
        this.rms = rms;
        this.positionInSeconds = positionInSeconds;
        this.position = position;
        this.possibleSilence = possibleSilence;
        this.possibleActive = possibleActive;
    }

    public double getRms() {
        return rms;
    }

    public float getPositionInSeconds() {
        return positionInSeconds;
    }

    public int getPosition() {
        return position;
    }

    public boolean isPossibleSilence() {
        return possibleSilence;
    }

    public boolean isPossibleActive() {
        return possibleActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RmsSignalInfo that = (RmsSignalInfo) o;

        if (Double.compare(that.rms, rms) != 0) return false;
        if (Float.compare(that.positionInSeconds, positionInSeconds) != 0) return false;
        if (position != that.position) return false;
        if (possibleSilence != that.possibleSilence) return false;
        return possibleActive == that.possibleActive;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(rms);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + (positionInSeconds != +0.0f ? Float.floatToIntBits(positionInSeconds) : 0);
        result = 31 * result + position;
        result = 31 * result + (possibleSilence ? 1 : 0);
        result = 31 * result + (possibleActive ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RmsSignalInfo{" +
                "rms=" + rms +
                ", positionInSeconds=" + positionInSeconds +
                ", position=" + position +
                ", possibleActive=" + possibleActive +
                ", possibleSilence=" + possibleSilence +
                '}';
    }
}
