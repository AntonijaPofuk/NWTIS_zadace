package org.foi.nwtis.antpofuk.ws.serveri;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.servlet.ServletContext;
import org.foi.nwtis.antpofuk.konfiguracije.Konfiguracija;
import org.foi.nwtis.antpofuk.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.antpofuk.web.podaci.Aerodrom;
import org.foi.nwtis.antpofuk.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.antpofuk.zrna.ObradaAerodroma;
import org.foi.nwtis.rest.klijenti.LIQKlijent;
import org.foi.nwtis.rest.podaci.AvionLeti;
import org.foi.nwtis.rest.podaci.Lokacija;
import org.foi.nwtis.rest.podaci.MeteoPodaci;

/**
 * Web service klasa sa metodama za obradu zahtjeva
 *
 * @author Antonija Pofuk
 */
@WebService(serviceName = "AIRP2WS")
public class AIRP2WS {

    ServletContext sc;
    BP_Konfiguracija bp_konf;
    String server;
    String baza;
    String korisnik;
    String lozinka;
    String driver;
    Konfiguracija konf;
    boolean postoji = false;
    String apikey;
    String username;
    String password;
    int inicijalniPocetakIntervala;
    int pocetakIntervala;
    int krajIntervala;
    int trajanjeIntervala;
    int trajanjeCiklusaDretve;
    MeteoPodaci meteoPodaci;
    String icao;
    String token;
    Aerodrom aerodrom;

    Boolean ima = false;
    Lokacija lokacija;

    /**
     * Web servis operacija koja vraca sve aerodrome iz tablice MYAIRPORTS
     *
     * @return aerodromi
     */
    @WebMethod(operationName = "dajSveAerodrome")
    public List<Aerodrom> dajSveAerodrome() {
        List<Aerodrom> aerodromi = new ArrayList<>();
        ucitajKonfiguraciju();
        LIQKlijent lIQKlijent = new LIQKlijent(token);
        try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);) {
            String upit = "SELECT * FROM MYAIRPORTS";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(upit);
            while (rs.next()) {
                aerodromi.add(new Aerodrom(rs.getString("IDENT"), rs.getString("NAME"),
                        rs.getString("ISO_COUNTRY"), null));
                for (Aerodrom aerodrom : aerodromi){
                Lokacija geoLocation = lIQKlijent.getGeoLocation(aerodrom.getNaziv());
                aerodrom.setLokacija(geoLocation); }
            }
      } catch (SQLException ex) {
            System.out.println("GRESKA: " + ex);
        }
        return aerodromi;
    }

    /**
     * Web servis operacija koja vraca aerodrom na temelju icao koda
     *
     * @param icao
     * @return aerodrom
     */
    @WebMethod(operationName = "dajAerodrom")
    public Aerodrom dajAerodrom(@WebParam(name = "icao") String icao) {
        ucitajKonfiguraciju();
        LIQKlijent lIQKlijent = new LIQKlijent(token);
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ObradaAerodroma.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                Statement s = con.createStatement();
                ResultSet rs = s.executeQuery(String.format("SELECT IDENT, NAME, ISO_COUNTRY, "
                        + "COORDINATES FROM AIRPORTS  WHERE IDENT = '" + icao + "'"));) {
            while (rs.next()) {
                aerodrom = new Aerodrom(rs.getString("IDENT"), rs.getString("NAME"),
                        rs.getString("ISO_COUNTRY"), null);
                if (aerodrom != null) {
                    Lokacija geoLocation = lIQKlijent.getGeoLocation(aerodrom.getNaziv());
                    aerodrom.setLokacija(geoLocation);
                }
            }
        } catch (SQLException ex) {
            System.out.println("GRESKA: " + ex);
        }
        return aerodrom;
    }

    /**
     * Web servis dodaje aerodrom u tablicu MYAIRPORTS
     *
     * @param icao
     * @author Antonija Pofuk
     * @return Boolean
     */
    @WebMethod(operationName = "dodajAerodrom")
    public Boolean dodajAerodrom(@WebParam(name = "aerodrom") String icao) {
        ucitajKonfiguraciju();
        LIQKlijent lIQKlijent = new LIQKlijent(token);
        String select = "SELECT IDENT, NAME, ISO_COUNTRY, "
                + "COORDINATES FROM AIRPORTS  WHERE IDENT = '" + icao + "'";
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AIRP2WS.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(select)) {
            while (rs.next()) {
                aerodrom = new Aerodrom(rs.getString("IDENT"), rs.getString("NAME"),
                        rs.getString("ISO_COUNTRY"), null);
                if (aerodrom != null) {
                    Lokacija geoLocation = lIQKlijent.getGeoLocation(aerodrom.getNaziv());
                    aerodrom.setLokacija(geoLocation);
                }
            }
            //String koord = aerodrom.getLokacija().getLatitude() + "," + aerodrom.getLokacija().getLongitude();
            String upit = "INSERT INTO MYAIRPORTS (IDENT, NAME, ISO_COUNTRY, COORDINATES, STORED) VALUES "
                    + "('" + aerodrom.getIcao() + "','" + aerodrom.getNaziv() + "' ,' " + aerodrom.getDrzava()
                    + " ',' " + null + "', CURRENT_TIMESTAMP)";
            stmt.executeUpdate(upit);
            System.out.println("Dodan novi aerodrom u MYAIRPORTS");
            stvoriPoruku("Aerodrom je dodan");
            return true;
        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Metoda koja ucitava potrebne konfiguracijske parametre
     *
     * @author Antonija Pofuk
     */
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
        username = konf.dajPostavku("username");
        password = konf.dajPostavku("password");
    }

    /**
     * Metoda provjerava postoji li aerodrom pod tim ICAO-kodom
     *
     * @author Antonija Pofuk return Boolean
     */
    private Boolean provjeriJelPostojiId(String icao) {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AIRP2WS.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);) {
            String upit = "SELECT * FROM MYAIRPORTS WHERE IDENT = " + icao;
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(upit);
            while (rs.next()) {
                ima = true;
                System.out.println("ID postoji");
            }
        } catch (SQLException ex) {
            System.out.println("GRESKA: " + ex);
            return false;
        }
        return ima;
    }

    /**
     * Web servis koji vraca podatke za avione koji je poletio u određenom
     * intervalu
     *
     * @param icao
     * @param odVremena
     * @param doVremena
     * @return List
     */
    @WebMethod(operationName = "dajAvionePoletjeleSAerodroma")
    public List<AvionLeti> dajAvioneSAerodroma(@WebParam(name = "icao") String icao,
            @WebParam(name = "odVremena") int odVremena, @WebParam(name = "doVremena") int doVremena) {
        ucitajKonfiguraciju();
        String upit = "SELECT * FROM AIRPLANES WHERE ESTDEPARTUREAIRPORT != 'NULL' AND ESTDEPARTUREAIRPORT = '" + icao
                + "' AND LASTSEEN >= " + odVremena + "AND LASTSEEN <=" + doVremena;
        List<AvionLeti> departures = new ArrayList<>();
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ObradaAerodroma.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                Statement s = con.createStatement();
                ResultSet rs = s.executeQuery(upit)) {
            while (rs.next()) {
                AvionLeti avionLeti = new AvionLeti();
                avionLeti.setIcao24(rs.getString("ICAO24"));
                avionLeti.setFirstSeen(rs.getInt("FIRSTSEEN"));
                avionLeti.setEstDepartureAirport(rs.getString("EstDepartureAirport"));
                avionLeti.setLastSeen(rs.getInt("LASTSEEN"));
                avionLeti.setEstArrivalAirport(rs.getString("EstArrivalAirport"));
                avionLeti.setCallsign(rs.getString("Callsign"));
                avionLeti.setEstDepartureAirportHorizDistance(rs.getInt("EstDepartureAirportHorizDistance"));
                avionLeti.setEstDepartureAirportVertDistance(rs.getInt("EstDepartureAirportVertDistance"));
                avionLeti.setEstArrivalAirportHorizDistance(rs.getInt("EstArrivalAirportHorizDistance"));
                avionLeti.setEstArrivalAirportVertDistance(rs.getInt("EstArrivalAirportVertDistance"));
                avionLeti.setDepartureAirportCandidatesCount(rs.getInt("DepartureAirportCandidatesCount"));
                avionLeti.setArrivalAirportCandidatesCount(rs.getInt("ArrivalAirportCandidatesCount"));
                departures.add(avionLeti);
            }
        } catch (SQLException ex) {
            System.out.println("GRESKA: " + ex);
        }
        return departures;
    }

    /**
     * Web servis koji provjerava je li avion poletio s odabranog aerodroma
     *
     * @param icao24
     * @param icao
     * @param odVremena
     * @param doVremena
     * @return Boolean
     */
    @WebMethod(operationName = "provjeriAvionPoletioSAerodroma")
    public Boolean provjeriAvionPoletioSAerodroma(@WebParam(name = "icao24") String icao24,
            @WebParam(name = "icao") String icao, @WebParam(name = "odVremena") int odVremena,
            @WebParam(name = "doVremena") int doVremena) {
        ucitajKonfiguraciju();
        List<AvionLeti> avionLeti = new ArrayList<>();
        String upit = "SELECT * FROM AIRPLANES WHERE ICAO24 = '" + icao24 + "' AND EstDepartureAirport = '" + icao24
                + "' AND LASTSEEN >= " + odVremena + " AND LASTSEEN <= " + doVremena;
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ObradaAerodroma.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                Statement s = con.createStatement();
                ResultSet rs = s.executeQuery(upit)) {
            while (rs.next()) {

            }
        } catch (SQLException ex) {
            System.out.println("GRESKA: " + ex);
            return false;
        }

        return true;
    }

    /**
     * Web servis koji vraca broj redaka u izboniku
     *
     * @return int
     */
    @WebMethod(operationName = "izbornikBrojRedaka")
    public int izbornikBrojRedaka() {
        ucitajKonfiguraciju();
        int izbornikBrojRedaka = Integer.parseInt(konf.dajPostavku("izbornikBrojRedaka"));
        return izbornikBrojRedaka;
    }

    /**
     * Web servis koji vraca broj redaka u tablici
     *
     * @return int
     */
    @WebMethod(operationName = "tablicaBrojRedaka")
    public int tablicaBrojRedaka() {
        ucitajKonfiguraciju();
        int tablicaBrojRedaka = Integer.parseInt(konf.dajPostavku("tablicaBrojRedaka"));
        return tablicaBrojRedaka;
    }
    
      private void stvoriPoruku(String message) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FacesMessage facesMessage = new FacesMessage(message);
        facesContext.addMessage(null, facesMessage);
    }
}
