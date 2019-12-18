package org.foi.nwtis.antpofuk.zadaca_1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Antonija Pofuk
 */
public class ServisAerodroma extends Thread {

    boolean kraj = false;
    private final int intervalDretve;
    final List<Aerodrom> aerodromi;
    final String datAerodromi;

    public ServisAerodroma(ThreadGroup threadGroupServisneDretve, List<Aerodrom> aerodromi, int intervalDretve, String datAerodromi) {
        super(threadGroupServisneDretve, threadGroupServisneDretve.getName() + "_" + threadGroupServisneDretve.activeCount() + 1);
        this.aerodromi = aerodromi;
        this.intervalDretve = intervalDretve;
        this.datAerodromi = datAerodromi;
    }

    @Override
    public void interrupt() {
        kraj = true;
        super.interrupt();
    }

    @Override
    public void run() {
        ObjectOutputStream oos = null;
        try {
            File datoAerodromi = new File(this.datAerodromi);
            oos = new ObjectOutputStream(new FileOutputStream(datoAerodromi));
            while (!kraj) {
                try {
                    Thread.sleep(intervalDretve * 1000);
                    System.out.println("Zapocinjem serijalizaciju liste aerodroma...");
                    serialize(oos);
                    System.out.println("Serijalizacija gotova.");
                } catch (InterruptedException ex) {
                    Logger.getLogger(this.getName() + " prekid");
                    kraj = true;
                }

            }
            serialize(oos);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ServisAerodroma.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ServisAerodroma.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ServisAerodroma.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private void serialize(ObjectOutputStream oos) throws IOException {
        if(this.aerodromi != null) {
            synchronized (this.aerodromi) {
                oos.writeObject(this.aerodromi);
                oos.flush();
            }
        }
    }

    @Override
    public synchronized void start() {
        super.start();
    }
}
