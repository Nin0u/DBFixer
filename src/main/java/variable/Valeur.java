package variable;

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

    /** 
     * Constructeur
     *  Valeur par défaut : "NULL"
     */
    public Valeur(String type) {
        this.type = type;
        this.valeur = NULL;
    }

    /** Constructeur */
    public Valeur(String type, Object valeur) {
        this.type = type;
        this.valeur = valeur;
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

    /** Convertit le type et la valeur en NULL_type */
    public void ToNull() {
        this.type = NULL + "_" + this.type;
        this.valeur = "TO_" + this.type + "(" + this.valeur + ")";
    }

    /** 
     * Vérifies si deux valeurs sont égales i.e : 
     * Elles ont le même type au NULL près.
     * Elles ont la même valeur.
     */
    public boolean equals(Valeur v) {
        if (this.type.startsWith(NULL) && !v.type.startsWith(NULL)) {
            String s = this.valeur.toString();
         
            return (this.type.substring(NULL.length() + 1).equals(v.type))
                && s.substring(s.length() - 5, s.length() - 1).equals(v.valeur.toString());
        }

        else if (!this.type.startsWith(NULL) && v.type.startsWith(NULL)) {
            String s = v.toString();
         
            return (v.type.substring(NULL.length() + 1).equals(this.type))
                && s.substring(s.length() - 5, s.length() - 1).equals(this.valeur.toString());
        }

        else {
            return (this.type.equals(v.type)) 
                && this.valeur.equals(v.valeur);
        }
    }
}
