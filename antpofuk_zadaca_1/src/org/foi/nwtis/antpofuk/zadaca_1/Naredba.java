package org.foi.nwtis.antpofuk.zadaca_1;

/**
 * @author Antonija Pofuk
 */
public class Naredba {
    private final String naziv;
    private String podaci = null;

    public Naredba(String naziv) {
        this.naziv = naziv;
    }
    
    public Naredba(String naziv, String podaci){
        this.naziv = naziv;
        this.podaci = podaci;
    }
    
    public void setPodaci(String podaci){
        this.podaci = podaci;
    }
    
    public String kreiraj(Korisnik korisnik){
        return String.format("KORISNIK %s; LOZINKA %s; %s;", korisnik.getKorisnickoIme(),korisnik.getLozinka(),pretvoriNaredbu());
    }
    private String pretvoriNaredbu(){
        if(podaci != null){
            if(naziv.equals("cekaj")){
                return String.format("%s %s", naziv.toUpperCase(),podaci);
            }else if(naziv.equals("aerodrom")){
                String[] parts = podaci.split(";");
                if(parts.length != 7){
                    return "Nepravilan broj parametara za aerodrom";
                }else{
                    return String.format("ICAO %s; IATA %s; NAZIV %s; GRAD %s; DRZAVA %s; GS %s; GD %s", parts[0],parts[1],parts[2],parts[3],parts[4],parts[5], parts[6]);
                }
            }else{
                return String.format("%s %s", naziv.toUpperCase(),podaci);
            }
        }else{
            return String.format("%s", naziv.toUpperCase());
        }
    }
}
