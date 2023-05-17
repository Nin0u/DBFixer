package maindb;

/**
 * Mode de Chase
 * 0 (par défaut) : Standard Chase
 * 1 : Oblivious Chase
 * 2 : Skolem Chase
 * 3 : Core Chase
 */
public enum ChaseMode {
    STANDARD,
    OBLIVIOUS,
    SKOLEM,
    CORE;

    private ChaseMode() {}

    /**
     * Renvoie le mode associé à l'entier i
     * 
     * On n'a pas besoin d'associer un nombre aux valeurs de ChaseMode car ils seront toujours
     * dans le même ordre dans ChaseMode.values() qui est l'ordre de déclaration.
     */
    public static ChaseMode getMode(int i) {
        if(i < 0 || i > 4) return null;
        return ChaseMode.values()[i];
    }
}
