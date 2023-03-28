package pied;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.io.FileInputStream;
import java.io.File;

public class TestParser {
    
    @Test
    public void testParse() throws Exception {
        FileInputStream is = new FileInputStream(new File("src/test/resources/parser.test"));
        assertNotNull(Parser.parse(is));
    }
}
