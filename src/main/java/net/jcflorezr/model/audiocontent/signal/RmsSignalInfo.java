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
