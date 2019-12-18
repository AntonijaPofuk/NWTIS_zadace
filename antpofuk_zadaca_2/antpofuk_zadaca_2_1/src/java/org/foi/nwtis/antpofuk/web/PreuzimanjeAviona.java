package org.foi.nwtis.antpofuk.web;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.foi.nwtis.antpofuk.konfiguracije.Konfiguracija;
import org.foi.nwtis.antpofuk.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.antpofuk.web.podaci.Aerodrom;
import org.foi.nwtis.antpofuk.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.antpofuk.zrna.ObradaAerodroma;
import org.foi.nwtis.rest.klijenti.OSKlijent;
import org.foi.nwtis.rest.podaci.AvionLeti;
import org.foi.nwtis.rest.podaci.Lokacija;

/**
 * @author Antonija Pofuk
 *
 * Dretva koja u pozadini preuzima podatke u avionima te ih sprema u AIRPORTS
 */
public class PreuzimanjeAviona extends Thread {

    boolean kraj = false;
    String username;
    String password;
    int pocetak;
    int pocetakIntervala;
    int krajIntervala;
    int trajanje;
    int ciklus;
    ServletContext sc;
    BP_Konfiguracija bp_konf;
    String server;
    String baza;
    String korisnik;
    String lozinka;
    String driver;
    Konfiguracija konf;
    String token;
    

    @Override
    public void interrupt() {
        kraj = true;
        super.interrupt();
    }

    @Override
    public void run() {
        ucitajKonfiguraciju();
        while (!kraj) {
            pocetakIntervala = (int) (new Date().getTime() / 1000) - (pocetak * 60 * 60); //1558015871
            krajIntervala = pocetakIntervala + (trajanje * 60 * 60); //1558037471
            System.out.println("podaci za dretvu su: pocetak" + (new Date().getTime() / 1000)+"pocetakIntervala: " + pocetakIntervala+"krajIntervala: "+ krajIntervala);
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException ex) {                
                Logger.getLogger(PreuzimanjeAviona.class.getName()).log(Level.SEVERE, null, ex);
            }
            try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);){               
                List<Aerodrom> aerodromi = new ArrayList<>();
                OSKlijent oSKlijent = new OSKlijent(username, password);
                String select = "SELECT * FROM MYAIRPORTS";
                String select2 = "SELECT * FROM AIRPLANES";
                Statement stmt = con.createStatement();
                Statement stmt2 = con.createStatement();
                ResultSet rs = stmt.executeQuery(select);
                ResultSet rs2 = stmt2.executeQuery(select2);
                if (rs2.next() == false) {
                    System.out.println("Nema spremljenih nikakvih aviona u bazi");
                    while (rs.next()) {
                        aerodromi.add(new Aerodrom(rs.getString("IDENT"), rs.getString("NAME"), rs.getString("ISO_COUNTRY"), null));
                        for (Aerodrom aerodrom : aerodromi) {
                            List<AvionLeti> departures = oSKlijent.getDepartures(aerodrom.getIcao(), pocetakIntervala, krajIntervala);
                            for (AvionLeti avionLeti : departures) {
                                System.out.println(" podaci aviona:  " + aerodrom.getIcao() + " k: " + avionLeti.getEstArrivalAirport());
                                String upit = "INSERT INTO AIRPLANES (ICAO24, FIRSTSEEN, ESTDEPARTUREAIRPORT, LASTSEEN, ESTARRIVALAIRPORT, CALLSIGN, "
                                        + "ESTDEPARTUREAIRPORTHORIZDISTANCE, ESTDEPARTUREAIRPORTVERTDISTANCE, ESTARRIVALAIRPORTHORIZDISTANCE, "
                                        + "ESTARRIVALAIRPORTVERTDISTANCE, DEPARTUREAIRPORTCANDIDATESCOUNT," + " ARRIVALAIRPORTCANDIDATESCOUNT , STORED) VALUES " + "('"
                                        + avionLeti.getIcao24() + "' , " + avionLeti.getFirstSeen() + " , '" + avionLeti.getEstDepartureAirport()
                                        + "' , " + avionLeti.getLastSeen() + " , '" + avionLeti.getEstArrivalAirport() + "' , '" + avionLeti.getCallsign()
                                        + "' , " + avionLeti.getEstDepartureAirportHorizDistance() + " , " + avionLeti.getEstDepartureAirportVertDistance()
                                        + " , " + avionLeti.getEstArrivalAirportHorizDistance() + " , " + avionLeti.getEstArrivalAirportVertDistance()
                                        + " , " + avionLeti.getDepartureAirportCandidatesCount() + " , " + avionLeti.getArrivalAirportCandidatesCount() + ",CURRENT_TIMESTAMP)";
                                Statement stmt3 = con.createStatement();
                                stmt3.executeUpdate(upit);
                                System.out.println("Spremljeni podaci za " + aerodrom.getIcao());
                            }
                        }
                    }
                } else {
                    System.out.println("Postoje zapisi za avione u bazi.");
                    while (rs.next()) { 
                         aerodromi.add(new Aerodrom(rs.getString("IDENT"), rs.getString("NAME"), rs.getString("ISO_COUNTRY"), null));
                        for (Aerodrom aerodrom : aerodromi) {
                            List<AvionLeti> departures = oSKlijent.getDepartures(aerodrom.getIcao(), pocetakIntervala, krajIntervala);
                            for (AvionLeti avionLeti : departures) {
                                System.out.println(" podaci aviona:  " + aerodrom.getIcao() + " k: " + avionLeti.getEstArrivalAirport());
                                String select3 = "SELECT * FROM AIRPLANES WHERE ICAO24='" + avionLeti.getIcao24() + "' AND LASTSEEN = " + avionLeti.getLastSeen();
                                Statement stmt3 = con.createStatement();
                                ResultSet rs3 = stmt3.executeQuery(select3);
                                if(rs3.next()==false){
                                     System.out.println(" podaci aviona:  " + aerodrom.getIcao() + " k: " + avionLeti.getEstArrivalAirport());
                                String upit = "INSERT INTO AIRPLANES (ICAO24, FIRSTSEEN, ESTDEPARTUREAIRPORT, LASTSEEN, ESTARRIVALAIRPORT, CALLSIGN, "
                                        + "ESTDEPARTUREAIRPORTHORIZDISTANCE, ESTDEPARTUREAIRPORTVERTDISTANCE, ESTARRIVALAIRPORTHORIZDISTANCE, "
                                        + "ESTARRIVALAIRPORTVERTDISTANCE, DEPARTUREAIRPORTCANDIDATESCOUNT," + " ARRIVALAIRPORTCANDIDATESCOUNT , STORED) VALUES " + "('" + avionLeti.getIcao24() + "' , " + avionLeti.getFirstSeen() + " , '" + avionLeti.getEstDepartureAirport()
                                        + "' , " + avionLeti.getLastSeen() + " , '" + avionLeti.getEstArrivalAirport() + "' , '" + avionLeti.getCallsign()
                                        + "' , " + avionLeti.getEstDepartureAirportHorizDistance() + " , " + avionLeti.getEstDepartureAirportVertDistance()
                                        + " , " + avionLeti.getEstArrivalAirportHorizDistance() + " , " + avionLeti.getEstArrivalAirportVertDistance()
                                        + " , " + avionLeti.getDepartureAirportCandidatesCount() + " , " + avionLeti.getArrivalAirportCandidatesCount() + ",CURRENT_TIMESTAMP)";
                                Statement stmt4 = con.createStatement();
                                stmt4.executeUpdate(upit);
                                System.out.println("Spremljeni podaci za " + aerodrom.getIcao());
                                }
                                else{System.out.println("Podaci su identicni. Prekidam unos...");}
                            }
                        } 
                    }
                }
                pocetakIntervala = krajIntervala;
                krajIntervala = pocetakIntervala + (trajanje * 60 * 60); 
                Thread.sleep(ciklus * 60 * 1000);    
                   
            } catch (InterruptedException | SQLException ex) {
                Logger.getLogger(PreuzimanjeAviona.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public synchronized void start() {
        super.start();       
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
        ciklus = Integer.parseInt(konf.dajPostavku("ciklus"));
        pocetak = Integer.parseInt(konf.dajPostavku("pocetak"));
        trajanje = Integer.parseInt(konf.dajPostavku("trajanje"));
        token = konf.dajPostavku("token");
    }
}
