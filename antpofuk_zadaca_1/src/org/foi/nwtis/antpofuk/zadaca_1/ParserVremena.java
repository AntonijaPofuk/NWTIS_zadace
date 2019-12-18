package org.foi.nwtis.antpofuk.zadaca_1;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Antonija Pofuk
 */
public class ParserVremena {
    public static String pretvoriUString(long timestamp, String format){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(timestamp));
    }
    
    public static long pretvoriUTimestamp(String vrijeme, String trenutniFormat) throws ParseException{
        SimpleDateFormat sdf = new SimpleDateFormat(trenutniFormat);
        return ((Date)sdf.parse(vrijeme)).getTime();
    }
}
