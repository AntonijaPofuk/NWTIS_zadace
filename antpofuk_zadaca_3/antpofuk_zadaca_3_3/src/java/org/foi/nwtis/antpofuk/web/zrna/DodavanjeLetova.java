package org.foi.nwtis.antpofuk.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.foi.nwtis.antpofuk.ejb.sb.UpravljanjePutnicima;
import org.foi.nwtis.antpofuk.podaci.Aerodrom;
import org.foi.nwtis.antpofuk.podaci.PodaciLeta;
import org.foi.nwtis.antpofuk.podaci.PodaciPutnika;

/**
 * Metoda obuhvaca radnje nad JSF formom dodavanja letova
 *
 * @author Antonija Pofuk
 */
@Named(value = "dodavanjeLetova")
@SessionScoped
public class DodavanjeLetova implements Serializable {

    @EJB
    private UpravljanjePutnicima upravljanjePutnicima;
    String odVremena;
    String doVremena;
    List<PodaciPutnika> listaPutnika;
    int odabraniPutnik;
    List<Aerodrom> listaAerodroma;
    String odabraniAerodrom;
    List<PodaciLeta> listaLetova;
    int odabraniLet;
    Boolean preuzetiPodatke;
    String username;
    String password;
    private SimpleDateFormat simpleDateFormatHR;
    String message;
    private int i1;
    private int i2;

    public DodavanjeLetova() {
        listaAerodroma = new ArrayList<>();
        listaLetova = new ArrayList<>();
        simpleDateFormatHR = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    }

    @PostConstruct
    private void init() {
        dajAerodrome();
        username = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("os_korisnik");
        password = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("os_lozinka");
        System.out.println(username + password);

    }

    public List<PodaciPutnika> getListaPutnika() {
        listaPutnika = upravljanjePutnicima.dajeSvePutnike();
        return listaPutnika;
    }

    public List<Aerodrom> getListaAerodroma() {
        listaAerodroma = upravljanjePutnicima.dajeSveAerodrome();
        return listaAerodroma;
    }

    public List<PodaciLeta> getListaLetova() {
        return listaLetova;
    }

    public String getOdVremena() {
        return odVremena;
    }

    public void setOdVremena(String odVremena) {
        this.odVremena = odVremena;
    }

    public String getDoVremena() {
        return doVremena;
    }

    public void setDoVremena(String doVremena) {
        this.doVremena = doVremena;
    }

    public int getOdabraniPutnik() {
        return odabraniPutnik;
    }

    public void setOdabraniPutnik(int odabraniPutnik) {
        this.odabraniPutnik = odabraniPutnik;
    }

    public String getOdabraniAerodrom() {
        return odabraniAerodrom;
    }

    public void setOdabraniAerodrom(String odabraniAerodrom) {
        this.odabraniAerodrom = odabraniAerodrom;
    }

    public int getOdabraniLet() {
        return odabraniLet;
    }

    public void setOdabraniLet(int odabraniLet) {
        this.odabraniLet = odabraniLet;
    }

    public Boolean getPreuzetiPodatke() {
        return preuzetiPodatke;
    }

    public void setPreuzetiPodatke(Boolean preuzetiPodatke) {
        this.preuzetiPodatke = preuzetiPodatke;
    }

    /**
     * Metoda dohvaca podatke o putniku i dodaje u listu
     *
     * @author Antonija Pofuk
     */
    public void dajPutnike() {
        for (PodaciPutnika pp : upravljanjePutnicima.dajeSvePutnike()) {
            listaPutnika.add(pp);
            System.out.println(pp.getFirstname());
            String korisnik = pp.getFirstname() + " " + pp.getLastname();
        }
    }

    /**
     * Metoda dohvaca podatke o aerodromima i dodaje ih u listu
     *
     * @author Antonija Pofuk
     */
    public void dajAerodrome() {
        for (Aerodrom aa : upravljanjePutnicima.dajeSveAerodrome()) {
            listaAerodroma.add(aa);
            System.out.println(aa.getIcao());
            System.out.println(aa.getNaziv());
        }
    }

    /**
     * Metoda poziva metodu dajLetove
     *
     * @author Antonija Pofuk
     */
    public String preuzmiLetove() {
        dajLetove();
        return "";
    }

    /**
     * Metoda dohvaca podatke o letovima i prikazuje ih u tablici
     *
     * @author Antonija Pofuk *
     */
    private void dajLetove() {
        if (pretvoriVrijeme()) {

            System.out.println("Odabrani aerodrom je: " + odabraniAerodrom);
            if (odabraniAerodrom == null) {
                System.out.println("Odaberite aerodrom!");
                return;
            }
            if (preuzetiPodatke) {
                listaLetova = upravljanjePutnicima.preuzmiLetoveOpenSky(odabraniAerodrom, i1, i2);
            } else {
                listaLetova = upravljanjePutnicima.preuzmiLetoveBazePodataka(odabraniAerodrom, i1, i2);
            }
        }
    }

    /**
     * Metoda dodaje oznacene letove prema odabranom putniku
     *
     * @param id predstavlja id odabranog leta
     * @author Antonija Pofuk
     */
    public String dodajLet(int id) {
        if (odabraniPutnik != 0) {
            upravljanjePutnicima.dodajLet(odabraniPutnik, id);
            InformatorPutnika.saljiPoruku(String.valueOf(odabraniPutnik));
        } else {
            stvoriPoruku("Let nije uspjesno dodan, provjerite podatke!");
        }
        return "";
    }

    /**
     * Metoda pretvara uneseni datum u odredeni format
     *
     * @return true ako je vrijeme i provjera vremenskih intervala prihvatljiva
     * @author Antonija Pofuk
     */
    public boolean pretvoriVrijeme() {
        try {
            i1 = (int) (simpleDateFormatHR.parse(odVremena).getTime() / 1000);
            i2 = (int) (simpleDateFormatHR.parse(doVremena).getTime() / 1000);
        } catch (ParseException ex) {
            Logger.getLogger(PregledLetova.class.getName()).log(Level.SEVERE, null, ex);
            return false;

        }
        return provjeraVremena();
    }

    /**
     * Metoda pretvara vremenski razmak između unesenih vremena
     *
     * @return true ako je prihvatljivo vrijeme
     * @author Antonija Pofuk
     */
    public boolean provjeraVremena() {
        if (i1 != 0 && i2 != 0) {
            if (i1 < i2 && i2 - i1 <= 24 * 60 * 60) {
                System.out.println("Vremenski podaci su prihvatljivi!");
                return true;
            }
        }
        System.out.println("Vremenski podaci nisu prihvatljivi!");
        return false;
    }

    /**
     * Metoda šalje poruke koje se prikazuju unutar JSF-a
     *
     * @author Antonija Pofuk
     */
    private void stvoriPoruku(String message) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FacesMessage facesMessage = new FacesMessage(message);
        facesContext.addMessage(null, facesMessage);
    }
}
