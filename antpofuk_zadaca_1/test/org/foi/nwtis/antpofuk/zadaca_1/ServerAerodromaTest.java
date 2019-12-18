
package org.foi.nwtis.antpofuk.zadaca_1;

import org.foi.nwtis.matnovak.konfiguracije.Konfiguracija;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Antonija Pofuk
 */
public class ServerAerodromaTest {
    
    public ServerAerodromaTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of main method, of class ServerAerodroma.
     */
    @Test
    public void testMain() {
        String[] args = null;
        String[] args1 = new String[]{
            "antpofuk.txt"
        };
        String[] args2 = new String[]{
            "antpofuk.json"
        };
        String[] args3 = new String[]{
            "antpofuk.xml"
        };     
        try{
            ServerAerodroma.main(args);
            ServerAerodroma.main(args1);
            ServerAerodroma.main(args2);
            ServerAerodroma.main(args3);
        }catch(Exception exc){
            fail("Test nije prosao "+exc.getMessage());
        }
        
    }

    /**
     * Test of zaustavi method, of class ServerAerodroma.
     */
    @Test
    public void testZaustavi() throws Exception {
        System.out.println("zaustavi");
        ServerAerodroma instance = null;
        instance.zaustavi();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of provjeriParametre method, of class ServerAerodroma.
     */
    @Test
    public void testProvjeriParametre() {
        String parametri = "antpofuk.txt";
        String parametri1 = "antpofuk.jso.n";
        String parametri2 = "antpofuk.tx.t";
        String p1 = "nekadatoteka.png";
        String p2 = "nekadatoteka.json";
        String p3 = "nekadatoteka.xml";
        assertTrue(ServerAerodroma.provjeriParametre(parametri));
        assertFalse(ServerAerodroma.provjeriParametre(parametri1));
        assertFalse(ServerAerodroma.provjeriParametre(parametri2));
        assertFalse(ServerAerodroma.provjeriParametre(p1));
        assertTrue(ServerAerodroma.provjeriParametre(p2));
        assertTrue(ServerAerodroma.provjeriParametre(p3));
    }

    /**
     * Test of ucitajKonfiguraciju method, of class ServerAerodroma.
     */
    @Test
    public void testUcitajKonfiguracijuTXT() {
        String nazivDatoteke = "antpofuk.txt";
        Konfiguracija result = ServerAerodroma.ucitajKonfiguraciju(nazivDatoteke);
        assertNotNull(nazivDatoteke, result);
    }
    /**
     * Test of ucitajKonfiguraciju method, of class ServerAerodroma.
     */
    @Test
    public void testUcitajKonfiguracijuXML() {
        String nazivDatoteke = "antpofuk.xml";
        Konfiguracija result = ServerAerodroma.ucitajKonfiguraciju(nazivDatoteke);
        assertNotNull(nazivDatoteke, result);
    }
    /**
     * Test of ucitajKonfiguraciju method, of class ServerAerodroma.
     */
    @Test
    public void testUcitajKonfiguracijuJSON() {
        String nazivDatoteke = "antpofuk.json";
        Konfiguracija result = ServerAerodroma.ucitajKonfiguraciju(nazivDatoteke);
        assertNotNull(nazivDatoteke, result);
    }
}
