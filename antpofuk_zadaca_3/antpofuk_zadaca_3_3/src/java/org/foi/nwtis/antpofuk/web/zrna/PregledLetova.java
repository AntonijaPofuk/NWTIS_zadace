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
import org.foi.nwtis.antpofuk.podaci.PodaciLeta;
import org.foi.nwtis.antpofuk.podaci.PodaciPutnika;

/**
 * Metoda sluzi za prikaz podataka na JSF stranici
 *
 * @author Antonija Pofuk
 */
@Named(value = "pregledLetova")
@SessionScoped
public class PregledLetova implements Serializable {

    @EJB
    private UpravljanjePutnicima upravljanjePutnicima;

    String odVremena;
    String doVremena;
    List<PodaciPutnika> listaPutnika;
    int odabraniPutnik;
    List<PodaciLeta> listaLetova;
    int odabraniLet;
    private SimpleDateFormat simpleDateFormatHR;
    String msg;
    private int i1;
    private int i2;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<PodaciLeta> getListaLetova() {
        return listaLetova;
    }

    public void setListaLetova(List<PodaciLeta> listaLetova) {
        this.listaLetova = listaLetova;
    }

    public PregledLetova() {
        simpleDateFormatHR = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        listaPutnika = new ArrayList<>();
    }

    @PostConstruct
    private void init() {
        dajPutnike();
    }

    /**
     * Metoda ispisuje podatke putnika u listu
     *
     * @author Antonija Pofuk
     */
    private void dajPutnike() {
        for (PodaciPutnika pp : upravljanjePutnicima.dajeSvePutnike()) {
            listaPutnika.add(pp);
            System.out.println(pp.getFirstname());
            String korisnik = pp.getFirstname() + " " + pp.getLastname();
        }     
    }

    /**
     * Metoda ispisuje letove na temelju intervala i odabranog putnika
     *
     * @author Antonija Pofuk
     */
    public String preuzmiLetove() {
        if (pretvoriVrijemeZaPreuzimanje()) {
            listaLetova = upravljanjePutnicima.preuzmiLetovePutnika(odabraniPutnik, i1, i2);
        }
       
        return "";
    }

    public List<PodaciPutnika> getListaPutnika() {
        return listaPutnika;
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

    public int getOdabraniLet() {
        return odabraniLet;
    }

    public void setOdabraniLet(int odabraniLet) {
        this.odabraniLet = odabraniLet;
    }

    /**
     * Metoda brise let na temelju identifikacijske oznake leta
     *
     * @param id je id leta
     * @author Antonija Pofuk
     * @return true ako je obrisan let
     */
    public boolean brisiLet(int id) {
        if (upravljanjePutnicima.brisiLet(id)) {       
            return true;
        } else {
            return false;
        }
    }

    /**
     * Metoda pretvara uneseno vrijeme u loong tip podataka kako se kasnije može raditi sa njime
     * Provjerava se vremenski razmak
     *
     * @author Antonija Pofuk
     * @return true ako je tocno pretvoreno vrijeme
     */
    public boolean pretvoriVrijeme() {
        try {
            i1 = (int) (simpleDateFormatHR.parse(odVremena).getTime() / 1000);
            i2 = (int) (simpleDateFormatHR.parse(doVremena).getTime() / 1000);
        } catch (ParseException ex) {
            Logger.getLogger(PregledLetova.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        if (i1 != 0 && i2 != 0) {
            if (i1 < i2 && i2 - i1 <= 24 * 60 * 60) {
                System.out.println("Vremenski podaci su prihvatljivi!");                
            } else {
                System.out.println("Vremenski podaci nisu prihvatljivi!");               
                return false;                
            }
        }
        return true;
    }

    /**
     * Metoda pretvara vrijeme u long
     *
     * @author Antonija Pofuka
     * @return true ako je tocno pretvoreno vrijeme
     */
    public boolean pretvoriVrijemeZaPreuzimanje() {
        try {
            i1 = (int) (simpleDateFormatHR.parse(odVremena).getTime() / 1000);
            i2 = (int) (simpleDateFormatHR.parse(doVremena).getTime() / 1000);
        } catch (ParseException ex) {
            Logger.getLogger(PregledLetova.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        if (i1 != 0 && i2 != 0) {
            if (i1 < i2) {
                System.out.println("Vremenski podaci su prihvatljivi!");
                
            } else {
                System.out.println("Vremenski podaci nisu prihvatljivi!");
                stvoriPoruku("Vremenski podaci nisu prihvatljivi!");
                return false;
            }
        }
        return true;
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
