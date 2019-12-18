package org.foi.nwtis.antpofuk.zadaca_1;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Antonija Pofuk
 *
 * Klasa je dretva koja provodi nadzor svih dretvi
 *
 */
public class ServisDretvi extends Thread {

    boolean kraj = false;
    private final String datEvidencije;
    private final int intervalNadzora;
    private final ServisAerodroma servis;
    private final List<DretvaZahtjeva> radneDretve;
    private final ServerAerodroma server;

    public ServisDretvi(ThreadGroup threadGroup, String datEvidencije, int intervalNadzora, ServisAerodroma servis, List<DretvaZahtjeva> radneDretve, ServerAerodroma server) {
        super(threadGroup, threadGroup.getName()+"_"+(threadGroup.activeCount()+1));
        this.datEvidencije = datEvidencije;
        this.intervalNadzora = intervalNadzora;
        this.servis = servis;
        this.radneDretve = radneDretve;
        this.server = server;
    }

    @Override
    public void interrupt() {
        kraj = true;
        super.interrupt();
    }

    @Override
    public void run() {
        FileOutputStream fos = null;
        try {
            File file = new File(datEvidencije);
            fos = new FileOutputStream(file, true);
            while (!this.kraj) {
                napraviEvidenciju(fos);
                //TODO: provediEvidenciju()
                try {
                    Thread.sleep(intervalNadzora*1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(this.getName() + " prekid");
                    kraj = true;
                }
                
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ServisDretvi.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(ServisDretvi.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public synchronized void start() {
        super.start();
    }
    /**
     * Zaustavlja rad dretvi koje su u cekanju sa naredbom, a ostale obavijestava da je rad gotov
     */
    public void zaustaviSveDretve(){
        servis.interrupt();
        for (DretvaZahtjeva dretvaZahtjeva : radneDretve) {
            if(null == dretvaZahtjeva.getState()){
                System.out.println("Thread state: "+dretvaZahtjeva.getState().name());
            }
            else switch (dretvaZahtjeva.getState()) {
                case WAITING:
                case TIMED_WAITING:
                    dretvaZahtjeva.interrupt();
                    break;
                case RUNNABLE:
                    synchronized(dretvaZahtjeva){
                        dretvaZahtjeva.setKraj(true);
                        dretvaZahtjeva.notify();
                    }   break;
                default:
                    System.out.println("Thread state: "+dretvaZahtjeva.getState().name());
                    break;
            }
        }
        try {
            server.zaustavi();
        } catch (IOException ex) {
            System.out.println("Greska prilikom zaustavljanja servera->" + ex.getMessage());
        }
        this.kraj = true;
    }
    /**
     * Radi ispis svih dretvi u sustavu
     * @param fos Stream vezan za datoteku u koju se evidencija upisuje
     */
    private void napraviEvidenciju(FileOutputStream fos) {
        try {
            String trenutnoVrijeme = ParserVremena.pretvoriUString(System.currentTimeMillis(), "HH:mm:ss dd-MM-yyyy");
            fos.write(trenutnoVrijeme.getBytes());
            System.out.println("Evidencija zapocela u => "+trenutnoVrijeme);
            ispisiPodatke(this,fos);
            ispisiPodatke(servis, fos);
            for (DretvaZahtjeva dretvaZahtjeva : this.radneDretve) {
                ispisiPodatke(dretvaZahtjeva, fos);
            }
        } catch (IOException ex) {
            System.out.println("Greska prilikom upisa evidencije");
        }
    }
    /**
     * Ispisuje evidenciju na ekran i upisuje u datoteku
     * @param dretva Dretva o kojoj se radi evidencija
     * @param datoteka Datoteka u koju se upisuje evidencija
     * @throws IOException Greska koja moze iskociti prilikom upisa u datoteku
     */
    private void ispisiPodatke(Thread dretva, FileOutputStream datoteka) throws IOException{
        String zapis = "--Zapis o dretvi-- \n Naziv grupe: %s \n Identifikator: %d\n Naziv: %s\n Status: %s\n Prioritet: %d\n";
        String zapisIspis = String.format(zapis, dretva.getThreadGroup().getName(),dretva.getId(),dretva.getName(),dretva.getState().name(),dretva.getPriority());
        System.out.println(zapisIspis);
        datoteka.write(zapisIspis.getBytes());
    }
}
