package maindb;

import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.File;
import contrainte.*;

public class TestObliviousChase {
    public void testObliviousChase() throws Exception {
        FileInputStream is = new FileInputStream(new File("src/test/resources/login/" + System.getProperty("user.home").substring(5) + ".login"));
        Database db = new Database(is);

        FileInputStream df = new FileInputStream(new File("src/test/resources/df/oblivious.test"));
        ArrayList<Contrainte> contraintes = Parser.parse(df);

        if (contraintes == null) System.out.println("Contraintes null");
        
        db.connect();
        Chase.chase(ChaseMode.OBLIVIOUS, db, contraintes);
        db.close();
    }
}
