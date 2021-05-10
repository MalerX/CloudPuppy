package lupa;

public final class SignalBytes {
    public static final int LENGTH_SIG_BYTE = 1;
    public final static int LENGTH_INT = 4;

    public static final byte AUTH = 2;
    public static final byte AUTH_OK = 3;
    public static final byte AUTH_FAIL = 5;
    public static final byte REG = 7;
    public static final byte REG_OK = 11;
    public static final byte REG_FAIL = 13;
    public static final byte UPLOAD = 17;
    public static final byte DOWNLOAD = 19;
    public static final byte MKDIR = 23;
    public static final byte UP = 29;
    public static final byte RM = 31;
    public static final byte JOIN = 37;
    public static final byte BACK = 41;
    public static final byte REFRESH = 43;
    public static final byte ERR = 47;
}