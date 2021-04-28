package toba.coven;

public class Gatekeeper {
    private static int begin = 0;

    public Gatekeeper() {
        begin++;
    }

    public String work() {
        return String.format("%s: %d", getClass().getSimpleName(), begin);
    }
}
