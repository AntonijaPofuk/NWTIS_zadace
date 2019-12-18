package org.foi.nwtis.antpofuk.zadaca_1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.foi.nwtis.matnovak.konfiguracije.Konfiguracija;
import org.foi.nwtis.matnovak.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.matnovak.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.matnovak.konfiguracije.NemaKonfiguracije;

public class ServerAerodroma {

    List<DretvaZahtjeva> radneDretve = null;
    ServisAerodroma servisAerodroma = null;
    ServisDretvi servisDretvi = null;
    ThreadGroup threadGroupServisneDretve = null;
    ThreadGroup threadGroupKorisnickDretvea = null;
    private List<Aerodrom> aerodromi;
    private List<Korisnik> korisnici;
    private Konfiguracija konfiguracija;
    private ServerSocket serverSocket;
    private boolean cekajKorisnika = true;

    public ServerAerodroma(Konfiguracija konfiguracija) {
        radneDretve = Collections.synchronizedList(new ArrayList<>());
        this.konfiguracija = konfiguracija;
    }

    public static void main(String[] args) {
        if (args == null || args.length != 1) {
            System.out.println("Krivi je broj argumenata!");
            return;
        }
        String parametri = UlazniParametriPom.pripremiParametre(args);
        if (provjeriParametre(parametri)) {
            Konfiguracija konfiguracija = ucitajKonfiguraciju(args[0]);
            if (konfiguracija != null) {
                ServerAerodroma sa = new ServerAerodroma(konfiguracija);
                pokreniServerAerodroma(sa);
            } else {
                System.out.println("Neispravna datoteka");
            }
        } else {
            System.out.println("Uneseni parametri ne odgovaraju!");
        }
        System.out.println("Kraj rada!");
    }

    /**
     * Staticna metoda koja izvrsava ServerAerodroma
     *
     * @param sa Objekt koji izvrsava program
     */
    private static void pokreniServerAerodroma(ServerAerodroma sa) {
        if (!sa.pokreniServer()) {
            System.out.println("Greska prilikom pokretanja servera!");
            return;
        }
        sa.ucitajAerodrome();
        sa.ucitajKorisnike();
        sa.inicijaliziraj();
        sa.cekajKorisnike();
    }

    /**
     * Metoda koja kreira ServerSocket
     *
     * @return Ako je port slobodan vraca @{link ServerSocket} u suprotnom null
     */
    private boolean pokreniServer() {
        int port = Integer.parseInt(konfiguracija.dajPostavku("port"));
        int maksCekaca = Integer.parseInt(konfiguracija.dajPostavku("maks.cekaca"));
        try {
            serverSocket = new ServerSocket(port, maksCekaca);
            return true;
        } catch (IOException ex) {
            System.out.println("Greska: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Ucitavanje aerodroma ukoliko datoteka postoji
     *
     * @param datEvidencije Datoteka iz koje se ucitavaju aerodromi
     */
    private void ucitajAerodrome() {
        String datEvidencije = konfiguracija.dajPostavku("datoteka.aerodroma");
        if (datEvidencije == null || datEvidencije.length() == 0) {
            System.out.println("Ne postoji datoteka.aerodrom u konfiguraciji!");
            return;
        }
        File dat = new File(datEvidencije);
        if (!dat.exists()) {
            System.out.println("Datoteka serijalizacije ne postoji! \n Kreiram ju sada");
            aerodromi = new ArrayList<>();
            return;
        }
        try {
            FileInputStream fis = new FileInputStream(dat);
            try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                aerodromi = (List<Aerodrom>) ois.readObject();
            }
            System.out.println("Broj ucitanih aerodroma: " + aerodromi.size());
        } catch (IOException | ClassNotFoundException i) {
            System.out.println("ERROR; Problem kod učitavanja datoteke serijalizacije aerodroma!" + i);
        }
    }

    /**
     * Ucitava korisnike iz konfiguracije
     */
    private void ucitajKorisnike() {
        Properties postavke = konfiguracija.dajSvePostavke();
        Set<Object> keySet = postavke.keySet();
        Pattern pattern = Pattern.compile("^korisnik[\\.\\-+_](\\S+).*");
        korisnici = new ArrayList<>();
        for (Object object : keySet) {
            String key = (String) object;
            Matcher m = pattern.matcher(key);
            if (m.matches()) {
                String korisnickoIme = m.group(1).trim();
                String password = postavke.getProperty(key);
                Korisnik korisnik = new Korisnik(korisnickoIme, password);
                korisnici.add(korisnik);
            }
        }
    }

    /**
     * Postavlja pocetno stanje servera
     */
    private void inicijaliziraj() {
        this.threadGroupServisneDretve = new ThreadGroup("antpofuk_SD");
        this.threadGroupKorisnickDretvea = new ThreadGroup("antpofuk_KD");
        String datAerodromi = konfiguracija.dajPostavku("datoteka.aerodroma");
        int intervalSerijalizacije = Integer.parseInt(konfiguracija.dajPostavku("interval.serijalizacije"));
        servisAerodroma = new ServisAerodroma(threadGroupServisneDretve, aerodromi, intervalSerijalizacije, datAerodromi);
        servisAerodroma.start();

        int intervalNadzora = Integer.parseInt(konfiguracija.dajPostavku("interval.nadzora"));
        String datEvidencije = konfiguracija.dajPostavku("datoteka.nadzora");
        servisDretvi = new ServisDretvi(threadGroupServisneDretve, datEvidencije, intervalNadzora, servisAerodroma, radneDretve, this);
        inicijalizirajDretveZahtjeva();
        servisDretvi.start();
    }

    /**
     * Server pocinje cekati korisnike na socketu
     */
    private void cekajKorisnike() {
        try {
            while (cekajKorisnika) {
                Socket s = serverSocket.accept();
                novaKonekcija(s);
            }
        } catch (IOException ex) {
            System.out.println("Server ugasen, ne prima vise nove zahtjeve.");
        }
    }

    /**
     * Kreira sve dretve, ali ih ne pokrece
     */
    private void inicijalizirajDretveZahtjeva() {
        int maksDretvi = Integer.parseInt(konfiguracija.dajPostavku("maks.dretvi"));
        for (int i = 0; i < maksDretvi; i++) {
            DretvaZahtjeva dretva = new DretvaZahtjeva(threadGroupKorisnickDretvea,"DretvaZahtjeva_" + (i + 1), korisnici, aerodromi, servisDretvi);
            radneDretve.add(dretva);
        }
    }

    /**
     * Trazi prvu slobodnu dretvu u kolekciji
     *
     * @return Ako postoji dretva koja je slobodna vraca nju, u suprotnom null
     */
    private DretvaZahtjeva pronadiSlobodnuDretvu() {
        for (DretvaZahtjeva dretvaZahtjeva : radneDretve) {
            if (dretvaZahtjeva.getState() == Thread.State.WAITING || dretvaZahtjeva.getState() == Thread.State.NEW) {
                return dretvaZahtjeva;
            }
        }
        return null;
    }

    /**
     * Vraca korisniku odgovor da nema slobodnih dretvi
     *
     * @param socket
     */
    private void nemaSlobodnihDretvi(Socket socket) {
        try (OutputStream os = socket.getOutputStream()) {
            os.write(" ERROR 01: Nema slobodnih dretvi".getBytes());
            os.flush();
            socket.shutdownInput();
            socket.shutdownOutput();
            socket.close();

        } catch (IOException ex) {
            System.out.println("Greska prilikom pisanja na socket");
        }

    }

    /**
     * Zaustavlja primanje novih zahtjeva
     *
     * @throws IOException
     */
    public void zaustavi() throws IOException {
        cekajKorisnika = false;
        serverSocket.close();
    }
    /**
     * Kada se novi korisnik spoji preko mrezne uticnice/socketa ova metoda prihvaca konekciju
     * @param s 
     */
    private void novaKonekcija(Socket s) {
        System.out.println("Korisnik se prikljucio");
        if (cekajKorisnika) {
            DretvaZahtjeva dz = pronadiSlobodnuDretvu();
            if (dz != null) {
                dz.setSocket(s);
                if (dz.getState() == Thread.State.NEW) {
                    dz.start();
                } else {
                    synchronized (dz) {
                        dz.notify();
                    }
                }
            } else {
                nemaSlobodnihDretvi(s);
            }
        } else {
            System.out.println("Server zavrsio sa radom");
            posaljiOdgovor("ERROR 10; Server je zavrsio sa radom", s);
        }
    }

    private void posaljiOdgovor(String poruka, Socket socket) {
        try (socket) {
            OutputStream os = socket.getOutputStream();
            os.write(poruka.getBytes());
            os.flush();
            socket.shutdownInput();
            socket.shutdownOutput();

        } catch (IOException ex) {
            Logger.getLogger(DretvaZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//rjeseno
    public static boolean provjeriParametre(String parametri) {
        String sintaksa = "^([^\\s]+\\.(?i)txt|xml|bin|json)$";

        Pattern pattern = Pattern.compile(sintaksa);
        Matcher m = pattern.matcher(parametri);

        boolean status = m.matches();
        return status;
    }

//rjeseno
    public static Konfiguracija ucitajKonfiguraciju(String nazivDatoteke) {
        try {
            Konfiguracija konfiguracija
                    = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
            return konfiguracija;
        } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
            System.out.println("Datoteka konfiguracije ne postoji");
        }
        return null;
    }
}
