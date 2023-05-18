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
    private static String NULL = "null";

    private String type;
    private Object valeur;
    private boolean new_null;

    /** 
     * Constructeur
     * Valeur par défaut : "NULL"
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
        if(this.new_null)
            return valeur.toString();

        if(type.startsWith("null") && !this.type.startsWith("null"))
            return "to_" + type + "(?)";
        return "?";
    }

    public boolean addPreparedStatementReq(PreparedStatement pstmt, int i) throws SQLException {
        if(!new_null) {
            pstmt.setObject(i, valeur);
            return true;
        } else return false;
    } 

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Valeur)) return false;
        Valeur v = (Valeur)o;

        if (this.type.startsWith(NULL) && !v.type.startsWith(NULL)) {
            String[] s = this.valeur.toString().split(",");
            s[0] = s[0].substring(1).trim(); 
            s[1] = s[1].substring(0, s[1].length() - 1).trim();

            // Si le numéro de null est 0 on compare les valeurs
            if (s[0].equals("0")) {
                return (this.type.substring(NULL.length() + 1).equals(v.type))
                && s[1].equals(v.valeur.toString());
            } 

            else return false;
        }

        else if (!this.type.startsWith(NULL) && v.type.startsWith(NULL)) {
            String[] s = v.toString().split(",");
            s[0] = s[0].substring(1).trim(); 
            s[1] = s[1].substring(0, s[1].length() - 1).trim();

            // Si le numéro de null est 0 on compare les valeurs
            if (s[0].equals("0")) {
                return (this.type.substring(NULL.length() + 1).equals(v.type))
                && s[1].equals(v.valeur.toString());
            } 
            else return false;
        }

        else
            return (this.type.equals(v.type)) && this.valeur.equals(v.valeur);
    }

    @Override
    public int hashCode() {
        int v1 = 0;

        if(this.type.startsWith(NULL)) {
            String[] s = this.valeur.toString().split(",");
            s[0] = s[0].substring(1).trim(); 
            s[1] = s[1].substring(0, s[1].length() - 1).trim();
            v1 = s[1].hashCode();
        }
        else v1 = this.valeur.toString().hashCode();

        return v1;
    }
}
