package maindb;

import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.File;
import contrainte.*;

public class TestParser {
    public void testParse() throws Exception {
        FileInputStream is = new FileInputStream(new File("src/test/resources/df/parser.test"));
        ArrayList<Contrainte> contraintes = Parser.parse(is);

        for(Contrainte c : contraintes)
            c.affiche();
    }
}
