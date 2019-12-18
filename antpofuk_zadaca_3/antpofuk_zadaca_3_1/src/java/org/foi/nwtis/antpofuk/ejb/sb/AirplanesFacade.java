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
import org.foi.nwtis.antpofuk.ejb.eb.Myairports;
import org.foi.nwtis.antpofuk.podaci.PodaciLeta;
import org.foi.nwtis.rest.podaci.AvionLeti;

/**
 * 
 * @author Antonija Pofuk
 */
@Stateless
public class AirplanesFacade extends AbstractFacade<Airplanes> {

    private final SimpleDateFormat simpleDateFormatHR = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    @PersistenceContext(unitName = "NWTiS_DZ3_PU")   

    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public AirplanesFacade() {
        super(Airplanes.class);
    }
    List<PodaciLeta> podaciLeta;
    PodaciLeta pl;

    /**
     * Metoda preuzima letove na temelju icao koda te vremenskog intervala te ih sprema u listu
     *
     * @author Antonija Pofuk
     * @param icao
     * @param odVremena
     * @param doVremena
     * @return lista podataka letova
     */
    public List<PodaciLeta> preuzmiLetove(String icao, int odVremena, int doVremena) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery();
        Root<Airplanes> sviLetovi = cq.from(Airplanes.class);
        Root<Myairports> sviAerodromiDolazak = cq.from(Myairports.class);        
        cq.multiselect(sviLetovi, sviAerodromiDolazak);
        List<Predicate> uvjeti = new ArrayList<>();
        uvjeti.add(cb.equal(sviLetovi.get("estarrivalairport"),
                sviAerodromiDolazak.get("ident")));
        uvjeti.add(cb.equal(sviLetovi.get("estdepartureairport"), icao));
        uvjeti.add(cb.between(sviLetovi.get("firstseen").as(int.class), odVremena, doVremena));
        cq.where(uvjeti.toArray(new Predicate[]{}));
        cq.orderBy(cb.asc(sviLetovi.get("firstseen")));
        Query q = em.createQuery(cq);
        List<Object[]> rezultat = q.getResultList();
        List<PodaciLeta> podaciLeta = new ArrayList<>();
        for (Object[] o : rezultat) {
            pl = new PodaciLeta();
            Airplanes a = (Airplanes) o[0];
            Myairports md = (Myairports) o[1];
            pl.setId(a.getId());
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

    public boolean provjeriDuplikate(AvionLeti al) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery();
        Root<Airplanes> sviLetovi = cq.from(Airplanes.class);
        cq.select(sviLetovi);
        List<Predicate> uvjeti = new ArrayList<>();
        uvjeti.add(cb.equal(sviLetovi.get("icao24"), al.getIcao24()));
        uvjeti.add(cb.equal(sviLetovi.get("firstseen"), al.getFirstSeen()));
        cq.where(uvjeti.toArray(new Predicate[]{}));
        Query q = em.createQuery(cq);
        List<Object> rezultat = q.getResultList();
        if (rezultat.isEmpty()) {
            System.out.println("Ima duplikata");
            return true;
        }
        System.out.println("Nema duplikata");
        return false;
    }
}
