package maindb;

public enum ChaseMode {
    STANDARD(0),
    OBLIVIOUS(1),
    SKOLEM(2),
    OBEGD(3),
    CORE(4);

    protected int i;
    private ChaseMode(int i) { this.i = i;}

    public static ChaseMode getMode(int i){
        switch(i) {
            case 0 : return STANDARD;
            case 1 : return OBLIVIOUS;
            case 2 : return SKOLEM;
            case 3 : return OBEGD;
            case 4 : return CORE;   
            default : return null;
        }
    }
}
