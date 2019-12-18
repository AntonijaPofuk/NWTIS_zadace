package org.foi.nwtis.antpofuk.zadaca_1;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Antonija Pofuk
 * 
 * Klasa za podatke o aerodromu
 * 
 */
public class Aerodrom implements Serializable{

    private String icao;
    private String iata;  
    private String naziv;
    private String grad;
    private String drzava;
    private float geoSirina;
    private float geoDuzina;

    public Aerodrom() {
    }
    
    

    public Aerodrom(String icao, String iata, String naziv, String grad, String drzava, float geoSirina, float geoDuzina) {
        this.icao = icao;
        this.iata = iata;
        this.naziv = naziv;
        this.grad = grad;
        this.drzava = drzava;
        this.geoSirina = geoSirina;
        this.geoDuzina = geoDuzina;
    }

    public float getGeoDuzina() {
        return geoDuzina;
    }

    public void setGeoDuzina(float geoDuzina) {
        this.geoDuzina = geoDuzina;
    }

    public String getIcao() {
        return icao;
    }

    public void setIcao(String icao) {
        this.icao = icao;
    }

    public String getIata() {
        return iata;
    }

    public void setIata(String iata) {
        this.iata = iata;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getGrad() {
        return grad;
    }

    public void setGrad(String grad) {
        this.grad = grad;
    }

    public String getDrzava() {
        return drzava;
    }

    public void setDrzava(String drzava) {
        this.drzava = drzava;
    }

    public float getGeoSirina() {
        return geoSirina;
    }

    public void setGeoSirina(float geoSirina) {
        this.geoSirina = geoSirina;
    } 

    @Override
    public String toString() {
        return "Aerodrom{" + "icao=" + icao + ", iata=" + iata + ", naziv=" + naziv + ", grad=" + grad + ", drzava=" + drzava + ", geoSirina=" + geoSirina + ", geoDuzina=" + geoDuzina + '}';
    }
    
    
    
    
    public static Aerodrom kreirajAerodrom(String input){
        Pattern p = Pattern.compile("ICAO (\\S*); IATA (\\S*); NAZIV (\\S*); GRAD (\\S*); DRZAVA (\\S*); GS (\\S*); GD (\\S*)");
        Matcher m = p.matcher(input);
        if(m.matches()){
            String icao = m.group(1);
            String iata = m.group(2);
            String naziv = m.group(3);
            String grad = m.group(4);
            String drzava = m.group(5);
            String gs = m.group(6);
            String gd = m.group(7);
            return new Aerodrom(icao, iata, naziv, grad, drzava, Float.parseFloat(gs), Float.parseFloat(gd));
        }
        return null;
    }
}
