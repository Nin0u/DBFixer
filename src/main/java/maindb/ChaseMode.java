package maindb;

public enum ChaseMode {
    STANDARD(0),
    OBLIVIOUS(1),
    SKOLEM(2),
    CORE(3);

    protected int i;
    private ChaseMode(int i) { this.i = i;}

    public static ChaseMode getMode(int i) {
        switch(i) {
            case 0 : return STANDARD;
            case 1 : return OBLIVIOUS;
            case 2 : return SKOLEM;
            case 3 : return CORE;
            default : return null;
        }
    }
}
