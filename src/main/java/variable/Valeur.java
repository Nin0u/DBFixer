package variable;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/** 
 * Cette classe représente la valeur d'un élément d'un tuple 
 * 
 * L'élément a un type qui peut être NULL_qqchose
 * et une valeur qui dépend du type donc on utilise Object
 */
public class Valeur {
    private static String NULL = "NULL";

    private String type;
    private Object valeur;
    private boolean new_null;

    /** 
     * Constructeur
     *  Valeur par défaut : "NULL"
     */
    public Valeur(String type) {
        this.type = type;
        this.valeur = NULL;
        this.new_null = false;
    }

    /** Constructeur */
    public Valeur(String type, Object valeur, boolean new_null) {
        this.type = type;
        this.valeur = valeur;
        this.new_null = new_null;
    }

    /** Getter */
    public String getType() {
        return type;
    }

    /** Getter */
    public Object getValeur() {
        return valeur;
    }

    /** Setter */ 
    public void setValeur(Object v){
        this.valeur = v;
    }

    public String addStringReq(String type) {
        if(this.new_null) {
            return valeur.toString();
        }
        if(type.startsWith("null") && !this.type.startsWith("null")) {
            return "to_" + type + "(?)";
        }
        return "?";
    }

    public boolean addPreparedStatementReq(PreparedStatement pstmt, int i) throws SQLException {
        if(!new_null) {
            pstmt.setObject(i, valeur);
            return true;
        } else return false;
    } 

    /** 
     * Vérifies si deux valeurs sont égales i.e : 
     * Elles ont le même type au NULL près.
     * Elles ont la même valeur.
     */
    public boolean equals(Object o) {
        if(!(o instanceof Valeur)) return false;
        Valeur v = (Valeur)o;

        //if(this.new_null != v.new_null) return false;
        if (this.type.startsWith(NULL) && !v.type.startsWith(NULL)) {
            String s = this.valeur.toString();

            System.out.println("# " + this.type.substring(NULL.length() + 1) + " " + (v.type));
            System.out.println("# " + s.substring(3, s.length() - 1) + " " + v.valeur.toString());
         
            return (this.type.substring(NULL.length() + 1).equals(v.type))
                && s.substring(3, s.length() - 1).equals(v.valeur.toString());
        }

        else if (!this.type.startsWith(NULL) && v.type.startsWith(NULL)) {
            String s = v.toString();

            System.out.println("= " + v.type.substring(NULL.length() + 1) + " " + (this.type));
            System.out.println("= " + s.substring(3, s.length() - 1) + " " + this.valeur.toString());
         
            return (v.type.substring(NULL.length() + 1).equals(this.type))
                && s.substring(3, s.length() - 1).equals(this.valeur.toString());
        }

        else {
            return (this.type.equals(v.type)) 
                && this.valeur.equals(v.valeur);
        }
    }

    @Override
    public int hashCode() {
        return this.valeur.hashCode() + this.type.hashCode();
    }
}
