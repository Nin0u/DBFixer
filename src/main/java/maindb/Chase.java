package maindb;
import java.sql.SQLException;
import java.util.ArrayList;
import contrainte.*;

public class Chase {
    public static void standardChase(Database db, ArrayList<Contrainte> sigma) throws SQLException{
        boolean end = false;
        while(! end) {
            end = true;
            for(Contrainte c : sigma) {
                System.out.println("DEBUT REPAIR");
                c.repairType(db);
                System.out.println("FIN REPAIR");
                int ret = 0;
                if(c instanceof EGD) {
                    while(true) {
                        ret = c.action(c.executeCorps(db), db);

                        if(ret == -1) return;
                        if(ret == 1) end = false;
                        if(ret == 0) break;
                    }
                } else {
                    ret = c.action(c.executeCorps(db), db);
                    if(ret == -1) return;
                    if(ret == 1) end = false;
                }
                System.out.println();
            }

        }
    }
}
