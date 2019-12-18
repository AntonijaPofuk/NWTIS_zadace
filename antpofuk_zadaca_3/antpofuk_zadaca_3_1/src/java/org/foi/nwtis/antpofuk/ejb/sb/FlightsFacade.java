package org.foi.nwtis.antpofuk.ejb.sb;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.foi.nwtis.antpofuk.ejb.eb.Airplanes;
import org.foi.nwtis.antpofuk.ejb.eb.Airports;
import org.foi.nwtis.antpofuk.ejb.eb.Flights;
import org.foi.nwtis.antpofuk.podaci.PodaciLeta;

/**
 *
 * @author Antonija Pofuk
 */
@Stateless
public class FlightsFacade extends AbstractFacade<Flights> {

    @PersistenceContext(unitName = "NWTiS_DZ3_PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public FlightsFacade() {
        super(Flights.class);  
    }
    private final SimpleDateFormat simpleDateFormatHR = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
    
    /**
     * Metoda prevjerava preklapanje intervala
     * @author Antonija Pofuk
     * @param id
     * @param odVremena
     * @param doVremena
     * @return 
    */   
    public boolean provjeriPreklapanjeLetova(int id, int odVremena, int doVremena) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery();
        Root<Airplanes> sviAvionLeti = cq.from(Airplanes.class);
        Root<Flights> sviLetovi = cq.from(Flights.class);
        cq.multiselect(sviLetovi.get("airplane").get("firstseen"), sviLetovi.get("airplane").get("lastseen"));
        cq.where(cb.equal(sviLetovi.get("passanger").get("id"), id));
        Query q = em.createQuery(cq);
        List<Object[]> rezultat = q.getResultList();
         for (int i = 0; i < rezultat.size(); i++) {
            if (((int) rezultat.get(i)[1]) >= odVremena && ((int) rezultat.get(i)[0]) <= doVremena) {
                return false;
            }
        }
        return true;
        }
   
     /**
     * Metoda preuzima letove putnika na temelju id putnika u zadanom intervalu
     * @author Antonija Pofuk
     * @param id
     * @param odVremena
     * @param doVremena
     * @return 
    */  
    public List<PodaciLeta> preuzmiLetovePutnika(String id, int odVremena, int doVremena) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery();
        Root<Flights> sviLetovi = cq.from(Flights.class);
        Root<Airports> sviAerodromiDolazak = cq.from(Airports.class);        
        cq.multiselect(sviLetovi.get("id"), sviLetovi.get("airplane"), sviAerodromiDolazak);
        List<Predicate> uvjeti = new ArrayList<>();        
        uvjeti.add(cb.equal(sviLetovi.get("passanger").get("id"), id));
        uvjeti.add(cb.equal(sviLetovi.get("airplane").get("estarrivalairport"),
                sviAerodromiDolazak.get("ident")));
        uvjeti.add(cb.ge(sviLetovi.get("airplane").get("firstseen").as(int.class), odVremena));
        uvjeti.add(cb.le(sviLetovi.get("airplane").get("lastseen").as(int.class), doVremena));
        cq.where(uvjeti.toArray(new Predicate[]{}));
        cq.orderBy(cb.asc(sviLetovi.get("airplane").get("firstseen")));
        Query q = em.createQuery(cq);
        List<Object[]> rezultat = q.getResultList();
        List<PodaciLeta> podaciLeta = new ArrayList<>();

        for (Object[] o : rezultat) {
            int idFlight = (int) o[0];
            Airplanes a = (Airplanes) o[1];
            Airports md = (Airports) o[2];
            PodaciLeta pl = new PodaciLeta();
            pl.setId(idFlight);
            pl.setNazivOdredisnogAerodroma(md.getName());
            pl.setVrijemePoletanja(simpleDateFormatHR.format((long) a.getFirstseen() * 1000));
            pl.setFirstSeen(a.getFirstseen());
            pl.setVrijemeSletanja(simpleDateFormatHR.format((long) a.getLastseen() * 1000));
            pl.setLastSeen(a.getLastseen());
            pl.setIcao24(a.getIcao24());
            pl.setCallsign(a.getCallsign());
            pl.setEstDepartureAirport(a.getEstdepartureairport());
            pl.setEstArrivalAirport(a.getEstarrivalairport());
            podaciLeta.add(pl);
        }

        return podaciLeta;
    }


}
