package org.foi.nwtis.antpofuk.zadaca_1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Antonija Pofuk
 *
 * Klasa je dretva koja provodi obradu zahtjeva
 *
 */
public class DretvaZahtjeva extends Thread {

    boolean kraj = false;
    private Socket socket = null;
    private ServisDretvi servis;
    private final List<Korisnik> korisnici;
    private final List<Aerodrom> aerodromi;

    /**
     * Konstruktor klase
     * @param grupa
     * @param name naziv dretve
     * @param korisnici Korisnici ucitani prilikom inicijalizacije servera
     * @param aerodromi Lista aerodroma koja se serijalizira u datoteku
     * @param servis Pomocu njega upravlja dretvama u slucaju kraja
     *
     */
    public DretvaZahtjeva(ThreadGroup grupa,String name, List<Korisnik> korisnici, List<Aerodrom> aerodromi, ServisDretvi servis) {
        super(grupa,name);
        this.korisnici = korisnici;
        this.aerodromi = aerodromi;
        this.servis = servis;
    }

    @Override
    public void interrupt() {
        kraj = true;
        super.interrupt();
    }

    @Override
    public void run() {
        while (!kraj) {
            try {
                String naredba = citajNaredbuSaSocketa();
                System.out.println("Dretva "+getName()+" obraduje zahtjev "+naredba);
                izvrsiNaredbu(naredba);
                if(!kraj){
                    synchronized(this){
                        wait();
                    }
                }
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(DretvaZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
                kraj = true;
            }
        }
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    
    public void setKraj(boolean kraj){
        this.kraj = kraj;
    }

    /**
     * Cita naredbu sa socketa koju je poslao korisnik
     *
     * @return Tekst dobiven putem socketa
     * @throws IOException Greska koja se moze javiti prilikom citanja
     */
    private String citajNaredbuSaSocketa() throws IOException {
        InputStream is = socket.getInputStream();
        int znak;
        StringBuilder stringBuilder = new StringBuilder();
        while ((znak = is.read()) != -1) {
            stringBuilder.append((char) znak);
        }
        socket.shutdownInput();
        return stringBuilder.toString();
    }

    /**
     * Poziva odgovarajucu metodu ovisno o naredbi
     *
     * @param naredba Naredba dobivena putem socketa
     */
    private void izvrsiNaredbu(String naredba) {
        Pattern pattern = Pattern.compile("^KORISNIK ([^\\s]+); LOZINKA ([^\\s]+); (.*);");
        Matcher matcher = pattern.matcher(naredba);
        if (!matcher.matches()) {
            posaljiOdgovor("ERROR 02; Sintaksa naredbe ne valja");
            return;
        }
        if (provjeriKorisnika(matcher)) {
            pozoviNaredbu(matcher.group(3));
        } else {
            posaljiOdgovor("ERROR 10; Korisnik nije pronaden");
        }
    }

    /**
     * Salje poruku preko socketa
     *
     * @param poruka koja se salje korisniku kao odgovor na naredbu
     */
    private void posaljiOdgovor(String poruka) {
        try {
            OutputStream os = socket.getOutputStream();
            os.write(poruka.getBytes());
            os.flush();
        } catch (IOException ex) {
            Logger.getLogger(DretvaZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Metoda koja provjerava jel postoji korisnik sa korisnickim imenom i
     * lozinkom u listi korisnika
     *
     * @param matcher Sadrzi ulazne podatke sa socketa
     * @return Vraca true ako korisnik sa zadanim podacima postoji, u suprotnom
     * false
     */
    private boolean provjeriKorisnika(Matcher matcher) {
        String korisnickoIme = matcher.group(1);
        String password = matcher.group(2);
        for (Korisnik korisnik : korisnici) {
            if (korisnickoIme.equals(korisnik.getKorisnickoIme()) && password.equals(korisnik.getLozinka())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Poziva odgovarajucu metodu prema naredbi korisnika
     *
     * @param naredba Naredba koja je dobivena putem mrezne uticnice
     */
    private void pozoviNaredbu(String naredba) {
        if (naredba.equals("STANJE")) {
            obaviNaredbuStanje();
        } else if (naredba.equals("KRAJ")) {
            obaviNaredbuKraj();
        } else if (naredba.startsWith("AERODROMI")) {
            obaviNaredbuAerodromi(naredba);
        } else if (naredba.startsWith("CEKAJ")) {
            obaviNaredbuCekaj(naredba);
        } else {
            Aerodrom aerodrom = Aerodrom.kreirajAerodrom(naredba);
            if (aerodrom == null) {
                posaljiOdgovor("ERROR 10; Naredba nije u zadanom formatu");
                zatvoriKonekciju();
            }else{
                obaviNaredbuAerodrom(aerodrom);
            }
        }
    }
    /**
     * Metoda koja se poziva kada dodje naredba KRAJ
     */
    private void obaviNaredbuKraj() {
        servis.zaustaviSveDretve();
        posaljiOdgovor("OK;");
        zatvoriKonekciju();
    }
    /**
     * Metoda koja se poziva kada dodje naredba STANJE
     */
    private void obaviNaredbuStanje() {
        posaljiOdgovor("OK;");
        zatvoriKonekciju();
    }
    /**
     * Metoda koja se poziva kada dodje naredba AERODROMI
     */
    private void obaviNaredbuAerodromi(String naredba) {
        String[] dijeloviNaredbe = naredba.split(" ");
        if(dijeloviNaredbe.length != 2){
            posaljiOdgovor("ERROR 12; Nema datoteke u koju ce se upisati aerodromi");
            zatvoriKonekciju();
            return;
        }
        String datoteka = dijeloviNaredbe[1].replace(";", "").trim();
        File file = new File(datoteka);
        StringBuilder stringBuilder = new StringBuilder();
        String odgovor = null;
        synchronized(this.aerodromi){
            for (Aerodrom aerodrom : aerodromi) {
                stringBuilder.append(aerodrom.toString()).append(System.lineSeparator());
            }
        }
        String formatiraniZapis = stringBuilder.toString();
        odgovor = String.format("OK; DUZINA %d %s %s", formatiraniZapis.getBytes().length,System.lineSeparator(),formatiraniZapis);
        zapisiUDatoteku(file, formatiraniZapis);
        posaljiOdgovor(odgovor);
        zatvoriKonekciju();
    }
    /**
     * Metoda koja se poziva kada dodje naredba CEKAJ N
     * @param naredba 
     */
    private void obaviNaredbuCekaj(String naredba) {
        String[] parts = naredba.split(" ");
        if (parts.length != 2) {
            posaljiOdgovor("ERROR 13; Neispravna naredba cekaj");
            return;
        }
        int intervalCekanja = Integer.parseInt(parts[1]);
        try {
            Thread.sleep(intervalCekanja*1000);
            posaljiOdgovor("OK;");
            zatvoriKonekciju();
        } catch (InterruptedException ex) {
            posaljiOdgovor("ERROR 13; Dretva prekinuta prilikom cekanja");
            zatvoriKonekciju();
        }
    }
    /**
     * Metoda koja se poziva kada dodje naredba  ICAO icao; IATA iata; NAZIV naziv; GRAD grad; DRŽAVA država; GŠ gš; GD gd;
     * @param aerodrom 
     */
    private void obaviNaredbuAerodrom(Aerodrom aerodrom){
        synchronized(this.aerodromi){
            for (Aerodrom aerodrom1 : this.aerodromi) {
                if(aerodrom1.getIata().equals(aerodrom.getIata()) || aerodrom1.getIcao().equals(aerodrom.getIcao())){
                    posaljiOdgovor("ERROR 14; ICAO ili IATA postoje u kolekciji");
                    zatvoriKonekciju();
                    return;
                }
            }
            this.aerodromi.add(aerodrom);
        }
        posaljiOdgovor("OK;");
        zatvoriKonekciju();
    }
    /**
     * Zatvara konekciju na mreznoj uticnici sa korisnikom
     */
    private void zatvoriKonekciju(){
        try {
            if(!socket.isInputShutdown()){
                socket.shutdownInput();
            }
            if(!socket.isOutputShutdown()){
                socket.shutdownOutput();
            }
            if(!socket.isClosed()){
                socket.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(DretvaZahtjeva.class.getName()).log(Level.SEVERE, "Pogreska prilikom zatvaranja konekcije", ex);
        }
    }
    
    private void zapisiUDatoteku(File file, String content){
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DretvaZahtjeva.class.getName()).log(Level.SEVERE, "Greska prilikom upisa u datoteku->datoteka nije pronadena", ex);
        } catch (IOException ex) {
            Logger.getLogger(DretvaZahtjeva.class.getName()).log(Level.SEVERE,"Greska prilikom upisa u datoteku", ex);
        }
    }
}
