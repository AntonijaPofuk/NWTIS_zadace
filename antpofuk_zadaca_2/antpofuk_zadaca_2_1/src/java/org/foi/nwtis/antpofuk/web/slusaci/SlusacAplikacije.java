package org.foi.nwtis.antpofuk.web.slusaci;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.foi.nwtis.antpofuk.konfiguracije.Konfiguracija;
import org.foi.nwtis.antpofuk.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.antpofuk.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.antpofuk.konfiguracije.NemaKonfiguracije;
import org.foi.nwtis.antpofuk.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.antpofuk.web.PreuzimanjeAviona;

/**
 * Klasa koja djeluje kao slusac konteksta servleta
 *
 * @author Antonija Pofuk
 */
@WebListener
public class SlusacAplikacije implements ServletContextListener {

    private static ServletContext sc;
    PreuzimanjeAviona preuzimanjeAviona;

    public static ServletContext getServletContext() {
        return sc;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sc = sce.getServletContext();
        String datoteka = sc.getInitParameter("konfiguracija");
        String putanja = sc.getRealPath("/WEB-INF") + java.io.File.separator;
        String puniNazivDatoteke = putanja + datoteka;
        try {
            BP_Konfiguracija bpk = new BP_Konfiguracija(puniNazivDatoteke);
            sc.setAttribute("BP_Konfig", bpk);
            Konfiguracija konf = KonfiguracijaApstraktna.preuzmiKonfiguraciju(puniNazivDatoteke);
            sc.setAttribute("Konfiguracija", konf);
            System.out.println("Učitana konfiguracija");
        } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
            Logger.getLogger(SlusacAplikacije.class.getName()).log(Level.SEVERE, null, ex);
        }
        preuzimanjeAviona = new PreuzimanjeAviona();
       
        preuzimanjeAviona.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (preuzimanjeAviona != null) {
            preuzimanjeAviona.interrupt();
        }
        sc = sce.getServletContext();
        sc.removeAttribute("BP_Konfig");
    }
}
