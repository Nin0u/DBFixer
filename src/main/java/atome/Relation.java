package atome;
import java.util.ArrayList;

import variable.Variable;

public class Relation extends Atome {
    private ArrayList<Variable> w;

    public Relation(){
        this.w = new ArrayList<Variable>();
    }
}
