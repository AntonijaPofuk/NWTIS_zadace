
package org.foi.nwtis.antpofuk.zadaca_1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Antonija Pofuk
 */
public class KorisnikAerodroma {
    private Korisnik korisnik;
    private String server;
    private int port;
    private Naredba naredba;

    public KorisnikAerodroma(Korisnik korisnik, String server, int port, Naredba naredba) {
        this.korisnik = korisnik;
        this.server = server;
        this.port = port;
        this.naredba = naredba;
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if(args.length < 7){
            System.out.println("Premali broj parametara");
            return;
        }else{
            KorisnikAerodroma ka = kreirajKorisnika(args);
            if(ka== null){
                System.out.println("Argumenti prilikom pokretanja nisu ispravni");
            }else{
                ka.komuniciraj();
            }
        }
    }
    
    /**
     * Metoda sa kojom citamo ulazne argumente i kreiramo korisnika
     * @param args Ulazni argumenti
     * @return Ako su argumenti u zadanom formatu vraca @{link KorisnikAerodroma}, ako nisu vraca null
     */
    private static KorisnikAerodroma kreirajKorisnika(String[] args){
        Pattern uzorak = Pattern.compile("^-k ([^\\s]+) -l ([^\\s]+) -s ([^\\s]+) -p ([^\\s]+) \\-\\-(\\S+)( .*)*");
        String parametri = UlazniParametriPom.pripremiParametre(args);
        Matcher matcher = uzorak.matcher(parametri);
        if(matcher.matches() && matcher.groupCount() >= 5){
            Korisnik korisnik = dohvatiKorisnika(matcher);
            String server = matcher.group(3);
            int port = Integer.parseInt(matcher.group(4));
            Naredba naredba = new Naredba(matcher.group(5));
            String g6 = matcher.group(6);
            if(g6 != null){
                String podaci = g6.replace(" ", "").replace("\"", "");
                naredba.setPodaci(podaci);
            }
            return new KorisnikAerodroma(korisnik,server,port,naredba);
            
        }
        return null;
    }
    /**
     * Kreira korisnika iz ulazne naredbe
     * @param matcher Objekt u kojem se nalaze provjereni ulazni podaci
     * @return Vraca objekt @{link Korisnik} iz dobivenih ulaznih podataka
     */
    private static Korisnik dohvatiKorisnika(Matcher matcher) {
        String userName = matcher.group(1);
        String lozinka = matcher.group(2);
        return new Korisnik(userName, lozinka);
    }
    /**
     * Metoda u kojoj se spaja na @{link ServerAerodroma} i salje mu zadanu naredbu
     */
    private void komuniciraj(){
        try {
            Socket socket = new Socket(server, port);
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            String poruka = naredba.kreiraj(korisnik);
            outputStream.write(poruka.getBytes());
            outputStream.flush();
            socket.shutdownOutput();
            int znak;
            StringBuilder stringBuilder = new StringBuilder();
            while ((znak = inputStream.read()) != -1) {
                stringBuilder.append((char) znak);
            }
            System.out.println(stringBuilder.toString());
            socket.shutdownInput();
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(ex.getMessage());
        }
    }
}
