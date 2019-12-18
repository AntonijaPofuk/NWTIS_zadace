package org.foi.nwtis.antpofuk.ejb.sb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.faces.context.FacesContext;
import org.foi.nwtis.antpofuk.ejb.eb.Airplanes;
import org.foi.nwtis.antpofuk.ejb.eb.Flights;
import org.foi.nwtis.antpofuk.ejb.eb.Myairports;
import org.foi.nwtis.antpofuk.ejb.eb.Passangers;
import org.foi.nwtis.antpofuk.podaci.Aerodrom;
import org.foi.nwtis.antpofuk.podaci.PodaciLeta;
import org.foi.nwtis.antpofuk.podaci.PodaciPutnika;
import org.foi.nwtis.rest.klijenti.OSKlijent;
import org.foi.nwtis.rest.podaci.AvionLeti;
import org.foi.nwtis.rest.podaci.Lokacija;

/**
 * Klasa predstavlja spojnicu između rada s podacima i njihovog prizivanja
 * @author Antonija Pofuk
 */
@Stateless
@LocalBean
public class UpravljanjePutnicima {

    String username;
    String password;
    @EJB
    private PassangersFacade passangersFacade;
    @EJB
    private MyairportsFacade myairportsFacade;
    @EJB
    private AirplanesFacade airplanesFacade;
    @EJB
    private FlightsFacade flightsFacade;

    @PostConstruct
    private void init() {
        username = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("os_korisnik");
        password = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("os_lozinka");
        System.out.println(username + password);

    }
    
     /**
     * Metoda dohvaca podatke o putniku iz tablice PASSANGERS i prikazuje u listi
     * @author Antonija Pofuk  
     * @return lista putnika
    */  
    public List<PodaciPutnika> dajeSvePutnike() {
        List<Passangers> putnici = passangersFacade.findAll();
        List<PodaciPutnika> listaPutnika = new ArrayList<>();
        for (Passangers p : putnici) {
            PodaciPutnika putnik = new PodaciPutnika();
            putnik.setId(p.getId());
            putnik.setFirstname(p.getFirstname());
            putnik.setLastname(p.getLastname());
            putnik.setUsername(p.getUsername());
            listaPutnika.add(putnik);
        }
        return listaPutnika;
    }

      /**
     * Metoda dohvaca podatke o aerodromima iz tablice MYAIRPORTS i prikazuje u listi
     * @author Antonija Pofuk  
     * @return lista aerodroma
    */  
    public List<Aerodrom> dajeSveAerodrome() {
        List<Myairports> aerodromi = myairportsFacade.findAll();
        List<Aerodrom> listaAerodroma = new ArrayList<>();
        for (Myairports a : aerodromi) {
            Aerodrom aerodrom = new Aerodrom();
            aerodrom.setIcao(a.getIdent());
            aerodrom.setDrzava(a.getIsoCountry());
            aerodrom.setNaziv(a.getName());
            String strKoordinate = a.getCoordinates();
            String[] koordinate = strKoordinate.split(",");
            Lokacija lokacija = new Lokacija();
            lokacija.setLatitude(koordinate[0]);
            lokacija.setLongitude(koordinate[1]);
            aerodrom.setLokacija(lokacija);
            listaAerodroma.add(aerodrom);
        }
        return listaAerodroma;
    }
    
      /**
     * Metoda dohvaca podatke o letovima iz baze
     * @author Antonija Pofuk  
     * @param icao  
     * @param odVremena  
     * @param doVremena  
     * @return lista letova
    */  
    public List<PodaciLeta> preuzmiLetoveBazePodataka(String icao, int odVremena, int doVremena){
        List<PodaciLeta> lista = airplanesFacade.preuzmiLetove(icao, odVremena, doVremena);
        return lista;
    }
    
      /**
     * Metoda dohvaca podatke o letovima sa OpenSky-a
     * @author Antonija Pofuk  
     * @param icao  
     * @param odVremena  
     * @param doVremena  
     * 
    */  
    public List<PodaciLeta> preuzmiLetoveOpenSky(String icao, int odVremena, int doVremena) {
        OSKlijent oSKlijent = new OSKlijent(username, password);
        List<AvionLeti> avioniPolazak = oSKlijent.getDepartures(icao, odVremena, doVremena);
        for (AvionLeti al : avioniPolazak) {
            if(airplanesFacade.provjeriDuplikate(al)){
            Airplanes letAviona = new Airplanes();
            letAviona.setIcao24(al.getIcao24());
            letAviona.setCallsign(al.getCallsign());
            letAviona.setEstdepartureairport(al.getEstDepartureAirport());
            letAviona.setEstarrivalairport(al.getEstArrivalAirport());
            letAviona.setFirstseen(al.getFirstSeen());
            letAviona.setLastseen(al.getLastSeen());
            letAviona.setFlightsList(new ArrayList<Flights>());
            letAviona.setStored(new Date());
            letAviona.setDepartureairportcandidatescount(al.getDepartureAirportCandidatesCount());
            letAviona.setArrivalairportcandidatescount(al.getArrivalAirportCandidatesCount());
            if (al.getIcao24() != null && al.getFirstSeen() > 0 && al.getEstDepartureAirport() != null && al.getLastSeen() > 0
                    && al.getEstArrivalAirport() != null && al.getCallsign() != null) {
                airplanesFacade.create(letAviona);
                System.out.println("Prosli su podaci za " + letAviona.getEstdepartureairport());
            } else {
                System.out.println("Nema podataka!");
            }
        }
        }
        return preuzmiLetoveBazePodataka(icao, odVremena, doVremena);
    }

      /**
     * Metoda dodaje let i sprema ga u tablicu FLIGHTS
     * @author Antonija Pofuk  
     * @param putnikID  
     * @param letAvionaID  
     * @return true ako se može dodati let
    */  
    public boolean dodajLet(int putnikID, int letAvionaID) {
        Airplanes letAviona = airplanesFacade.find(letAvionaID);
        if (letAviona != null) {
            if (flightsFacade.provjeriPreklapanjeLetova(putnikID, letAviona.getFirstseen(), letAviona.getLastseen())) {
                Passangers putnik = passangersFacade.find(putnikID);
                if (putnik != null) {
                    Flights flights = new Flights();
                    flights.setAirplane(letAviona);
                    flights.setPassanger(putnik);
                    flights.setStored(new Date());
                    flightsFacade.create(flights);
                    return true;
                }
            } else {
                System.out.println("Nemoze se dodati let, dogodilo se preklapanje");
                return false;
            }
        }
        System.out.println("Nemoze se dodati let, dogodila se pogreska");
        return false;
    }
    
    
      /**
     * Metoda brise podatke o odabranom avionu iz baze
     * @author Antonija Pofuk  
     * @param letAvionaID  
     * @return 
    */  
    public boolean brisiLet(int letAvionaID) {
        if (letAvionaID != 0) {
            flightsFacade.remove(flightsFacade.find(letAvionaID));
            return true;
        } else {
            return false;
        }

    }

      /**
     * Metoda dohvaca podatke o letovima odabranog putnika pozivajuci FlightsFacase
     * @author Antonija Pofuk  
     * @param putnikID  
     * @param odVremena  
     * @param doVremena  
     * 
    */  
    public List<PodaciLeta> preuzmiLetovePutnika(int putnikID, int odVremena, int doVremena) {
        return flightsFacade.preuzmiLetovePutnika(String.valueOf(putnikID), odVremena, doVremena);
    }

}
