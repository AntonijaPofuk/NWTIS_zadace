package org.foi.nwtis.antpofuk.zadaca_1;

/**
 * @author Antonija Pofuk
 */
public class UlazniParametriPom {
    public final static String TAG = "UlazniParametriPom";
    
    public UlazniParametriPom(){

    }
    /**
     * Spaja ulazne parametre u jedan string
     * @param args ulazni parametri
     * @return Ulazni parametri spojeni razmakom 
     */
    public static String pripremiParametre(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            sb.append(arg).append(" ");
        }
        String p = sb.toString().trim();
        return p;
    }
}
