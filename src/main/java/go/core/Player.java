package go.core;

public class Player {
    private final int identifier;
    private int capturedStones;

    public Player(int identifier) {
        this.identifier = identifier;
        this.capturedStones = 0;
    }

    public int getIdentifier() {
        return identifier;
    }

    public void addCapturedStones(int nb) {
        capturedStones += nb;
    }

    public void removeCapturedStones(int nb) {
        capturedStones -= nb;
    }

    @Override
    public String toString() {
        return "Player " + identifier + " " + capturedStones;
    }
}
