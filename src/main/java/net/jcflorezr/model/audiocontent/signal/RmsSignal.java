package net.jcflorezr.model.audiocontent.signal;

public class RmsSignal {

    private double rms;
    private float positionInSeconds;
    private int position;
    private boolean active;


    public RmsSignal() {
    }

    public RmsSignal(double rms, float positionInSeconds, int position, boolean active) {
        this.rms = rms;
        this.positionInSeconds = positionInSeconds;
        this.position = position;
        this.active = active;
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

    public boolean isActive() {
        return active;
    }

    public void setPosition(int samplingRate, int second) {
        this.position += samplingRate * second;
        this.positionInSeconds += second;
    }

    @Override
    public String toString() {
        return "RmsSignal{" +
                "rms=" + rms +
                ", positionInSeconds=" + positionInSeconds +
                ", position=" + position +
                ", active=" + active +
                '}';
    }
}
