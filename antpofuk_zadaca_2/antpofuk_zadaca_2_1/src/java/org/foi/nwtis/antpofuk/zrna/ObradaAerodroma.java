package org.foi.nwtis.antpofuk.zrna;

import com.sun.javafx.logging.PulseLogger;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.foi.nwtis.antpofuk.konfiguracije.Konfiguracija;
import org.foi.nwtis.antpofuk.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.antpofuk.web.podaci.Aerodrom;
import org.foi.nwtis.antpofuk.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.rest.klijenti.LIQKlijent;
import org.foi.nwtis.rest.klijenti.OWMKlijent;
import org.foi.nwtis.rest.podaci.Lokacija;
import org.foi.nwtis.rest.podaci.MeteoPodaci;

/**
 * Klasa koja sluzi za manipulaciju podacima koji se prikazuju na formi
 *
 * @author Antonija Pofuk
 */
@Named(value = "obradaAerodroma")
@SessionScoped
public class ObradaAerodroma implements Serializable {

    boolean kraj = false;
    static Boolean postoji = false;
    String naziv;
    static ServletContext sc;
    BP_Konfiguracija bp_konf;
    String server;
    String driver;
    String baza;
    String korisnik;
    String lozinka;
    Konfiguracija konf;
    String username;
    String password;
    MeteoPodaci meteoPodaci;
    String icao;
    Aerodrom aerodrom;
    String apikey;
    String token;
    String drzava;
    private String jezik = "hr";
    String message;
    int pocetak;
    int trajanje;
    int pocetakIntervala;
    int krajIntervala;
    

    public String getJezik() {
        return jezik;
    }

    public void setJezik(String jezik) {
        this.jezik = jezik;
    }

    public Object odaberiJezik() {
        return "jezik";
    }

    public ObradaAerodroma() {
    }

    public String getIcao() {
        return icao;
    }

    public void setIcao(String icao) {
        this.icao = icao;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public Aerodrom getAerodrom() {
        return aerodrom;
    }

    public MeteoPodaci getMeteoPodaci() {
        return meteoPodaci;
    }

    /**
     * Metoda preuzima potrebne podatke iz tablice AIRPORTS
     *
     * @author Antonija Pofuk
     * @return
     */
    public String preuzmiNazivAerodroma() {
        List<Aerodrom> aerodromi = new ArrayList<>();
        ucitajKonfiguraciju();
        if(icao==null){
        stvoriPoruku("Unesite podatke!");
        }
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ObradaAerodroma.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                Statement s = con.createStatement();
                ResultSet rs = s.executeQuery(String.format("SELECT IDENT, NAME, ISO_COUNTRY, COORDINATES FROM AIRPORTS"
                        + "  WHERE IDENT = '" + icao + "'"));) {
            while (rs.next()) {
                aerodromi.add(new Aerodrom(rs.getString("IDENT"), rs.getString("NAME"), rs.getString("ISO_COUNTRY"), null));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ObradaAerodroma.class.getName()).log(Level.SEVERE, null, ex);
             stvoriPoruku("Aerodroma nema");
        }
        for (Aerodrom a1 : aerodromi) {
            if (a1.getIcao().compareTo(icao) == 0) {
                aerodrom = a1;
            }  stvoriPoruku("Aerodrom je prikazan.");
        }
        pocetakIntervala = (int) (new Date().getTime() / 1000) - (pocetak * 60 * 60); //1558015871
        krajIntervala = pocetakIntervala + (trajanje * 60 * 60); //1558037471
        System.out.println("podaci su" + (new Date().getTime() / 1000)+" " + pocetakIntervala+" "+ krajIntervala);
        return "";
    }

    /**
     * Metoda preuzima lokaciju sa web servisa za zadani aerodrom
     *
     * @author Antonija Pofuk
     * @return
     */
    public String preuzmiGPSLokaciju() {
        LIQKlijent lIQKlijent = new LIQKlijent(token);
        if (aerodrom != null && aerodrom.getIcao().length() > 0
                && !aerodrom.getNaziv().isEmpty()) {
            Lokacija geoLocation = lIQKlijent.getGeoLocation(aerodrom.getNaziv());
            aerodrom.setLokacija(geoLocation);
        }
        return "";
    }

    /**
     * Metoda preuzima meteopodatke sa web servisa za zadani aerodrom
     *
     * @author Antonija Pofuk
     * @return
     */
    public String preuzmiMeteoPodatke() {
        OWMKlijent oWMKlijent = new OWMKlijent(apikey);
        if (aerodrom != null && aerodrom.getLokacija() != null) {
            meteoPodaci = oWMKlijent.getRealTimeWeather(aerodrom.getLokacija().getLatitude(),
                    aerodrom.getLokacija().getLongitude());
        }
        return "";
    }

    /**
     * Metoda sprema podatke u tablicu MYAIRPORTS
     *
     * @author Antonija Pofuk
     * @return
     */
    public String spremiAerodrom() {
        String koord = aerodrom.getLokacija().getLatitude() + "," + aerodrom.getLokacija().getLongitude();
        ucitajKonfiguraciju();
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ObradaAerodroma.class.getName()).log(Level.SEVERE, null, ex);
        }
        String upit = "INSERT INTO MYAIRPORTS (IDENT, NAME, ISO_COUNTRY, COORDINATES, STORED) VALUES "
                + "('" + aerodrom.getIcao() + "', '" + aerodrom.getNaziv() + " ',' " + aerodrom.getDrzava()
                + "' , '" + koord + "', CURRENT_TIMESTAMP)";
        try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                Statement stmt = con.createStatement();) {
            stmt.executeUpdate(upit);
            stvoriPoruku("Aerodrom je dodan");
        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
            stvoriPoruku("Aerodrom nije dodan");
        }
        return "";
    }

    public void ucitajKonfiguraciju() {
        sc = SlusacAplikacije.getServletContext();
        bp_konf = (BP_Konfiguracija) sc.getAttribute("BP_Konfig");
        server = bp_konf.getServerDatabase();
        baza = server + bp_konf.getUserDatabase();
        korisnik = bp_konf.getUserUsername();
        lozinka = bp_konf.getUserPassword();
        driver = bp_konf.getDriverDatabase();
        konf = (Konfiguracija) sc.getAttribute("Konfiguracija");
        apikey = konf.dajPostavku("apikey");
        token = konf.dajPostavku("token");
        pocetak = Integer.parseInt(konf.dajPostavku("pocetak"));
        trajanje = Integer.parseInt(konf.dajPostavku("trajanje"));
    }
  private void stvoriPoruku(String message) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FacesMessage facesMessage = new FacesMessage(message);
        facesContext.addMessage(null, facesMessage);
    }
}
