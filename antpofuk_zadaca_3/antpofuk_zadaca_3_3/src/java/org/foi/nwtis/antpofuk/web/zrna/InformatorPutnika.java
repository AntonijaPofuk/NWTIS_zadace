package org.foi.nwtis.antpofuk.web.zrna;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * Metoda sluzi za rad s web socketom
 *
 * @author Antonija Pofuk
 */
@ServerEndpoint("/infoPutnik")
public class InformatorPutnika {

    static List<Session> sjednice = new ArrayList<>();

    /**
     * Metoda sluzi za rad s web socketom nakon stvaranja sjednice
     *
     * @author Antonija Pofuk
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig conf) {
        sjednice.add(session);
        System.out.println("WS open: " + session.getId());
    }

    /**
     * Metoda sluzi za rad s web socketom nakon zatvaranja sjednice
     *
     * @author Antonija Pofuk
     */
    @OnClose
    public void onClose(Session session, EndpointConfig conf) {
        System.out.println("WS close: " + session.getId());
        sjednice.remove(session);
    }

    /**
     * Metoda sluzi za rad s web socketom nakon primanja poruke
     *
     * @author Antonija Pofuk
     */
    @OnMessage
    public void onMessage(String message) {
        for (Session s : sjednice) {
            if (s.isOpen()) {
                try {
                    s.getBasicRemote().sendText(message);
                } catch (IOException ex) {
                    Logger.getLogger(InformatorPutnika.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Metoda šalje poruke svim sjednicama koje su otvorene u aplikaciji
     *
     * @author Antonija Pofuk
     */
    public static void saljiPoruku(String poruka) {
        for (Session s : sjednice) {
            if (s.isOpen()) {
                try {
                    s.getBasicRemote().sendText(poruka);
                } catch (IOException ex) {
                    Logger.getLogger(InformatorPutnika.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }
       
}
