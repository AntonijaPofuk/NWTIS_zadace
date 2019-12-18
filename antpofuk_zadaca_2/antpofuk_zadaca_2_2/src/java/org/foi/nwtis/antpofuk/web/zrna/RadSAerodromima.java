package org.foi.nwtis.antpofuk.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.xml.ws.WebServiceRef;
import org.foi.nwtis.antpofuk.ws.serveri.AIRP2WS_Service;
import org.foi.nwtis.antpofuk.ws.serveri.Aerodrom;

/**
 *
 * @author Antonija Pofuk
 */
@Named(value = "radSAerodromima")
@SessionScoped
public class RadSAerodromima implements Serializable {   

    @WebServiceRef(wsdlLocation = "WEB-INF/wsdl/localhost_8084/antpofuk_zadaca_2_1/AIRP2WS.wsdl")
    private AIRP2WS_Service ser;
    private AIRP2REST_JerseyClient cl;
   
    private String icao;
    private List<Aerodrom> aerodromi;
    private List<String> listaAerodroma;
    private String odabraniAerodrom;
    String podaci = "";
  

    public RadSAerodromima() {
        cl = new AIRP2REST_JerseyClient();
        ser = new AIRP2WS_Service();
        podaci = cl.getJson();
        odabraniAerodrom = "";
    }

       public List<Aerodrom> getAerodromi() {     
//        aerodromi = cl.getJson();
        return aerodromi;
    }
    
    public void setAerodromi(List<Aerodrom> aerodromi) {
        this.aerodromi = aerodromi;
    }

    public String getIcao() {
        return icao;
    }

    public void setIcao(String icao) {
        this.icao = icao;
    }

    public String getOdabraniAerodrom() {
        return odabraniAerodrom;
    }

    public void setOdabraniAerodrom(String odabraniAerodrom) {
        this.odabraniAerodrom = odabraniAerodrom;
    }
    
    public List<String> getListaAerodroma(){
    aerodromi = getAerodromi();
    listaAerodroma = new ArrayList<>();
    
    for (Aerodrom a: aerodromi){
    this.listaAerodroma.add(a.getIcao());
    }
    
    return listaAerodroma;}
    
      public void setListaAerodroma(List<String> listaAerodroma) {
        this.listaAerodroma = listaAerodroma;
    }
      /**
     * Metoda koristeći REST servis upisuje podatke o aerodromu
     *
     * @return String
     * @author Antonija Pofuk
     */
    public String dodajAerodromREST() {
        if (this.icao != null) {

            System.out.println("upisanREST");      
           
            return "";
        } else {
            stvoriPoruku("Unesite icao aerodroma!");
            return "";
    }

    }

    /**
     * Metoda koristeći SOAP servis upisuje podatke o aerodromu
     *
     * @return String
     * @author Antonija Pofuk
     */
    public String dodajAerodromSOAP() {
       if (this.icao != null) {
           Aerodrom aerodrom = new Aerodrom();
           aerodrom.setIcao(icao);
           System.out.println("Aerodrom: " + icao);
           cl.postJson(aerodrom);
            System.out.println("upisanSOAP-a");
            this.icao="";
            this.odabraniAerodrom = null;
        return "";
        } else {
            stvoriPoruku("Unesi icao aerodroma");
            return "";
           }
    }

    /**
     * Metoda koristeći REST servis brise podatke o odabranom aerodromu
     *
     * @return String
     * @author Antonija Pofuk
     */
    public String brisiREST() {
        System.out.println(odabraniAerodrom);
        
        stvoriPoruku("Morate odabrati aerodrom za brisanje!");
        return "";

    }

    /**
     * Metoda koristeći REST servis preuzima podatke o aerodromu
     *
     * @return String
     * @author Antonija Pofuk
     */
    public String preuzmiREST() {         

       

        return "";

    } 

    /**
     * Metoda vraca odgovarajuću poruku za prikaz na formi.
     *
     * @param message
     * @author Antonija Pofuk
     */
    private void stvoriPoruku(String message) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FacesMessage facesMessage = new FacesMessage(message);
        facesContext.addMessage(null, facesMessage);
    }

 

    static class AIRP2REST_JerseyClient {

        private WebTarget webTarget;
        private Client client;
        private static final String BASE_URI = "http://localhost:8084/antpofuk_zadaca_2_1/webresources";

        public AIRP2REST_JerseyClient() {
            client = javax.ws.rs.client.ClientBuilder.newClient();
            webTarget = client.target(BASE_URI).path("aerodromi");
        }

        public String putJson(Object requestEntity, String id) throws ClientErrorException {
            return webTarget.path(java.text.MessageFormat.format("{0}", new Object[]{id})).request(javax.ws.rs.core.MediaType.APPLICATION_JSON).put(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), String.class);
        }

        public String getJsonIdAvioni(String id, String doVremena, String odVremena) throws ClientErrorException {
            WebTarget resource = webTarget;
            if (doVremena != null) {
                resource = resource.queryParam("doVremena", doVremena);
            }
            if (odVremena != null) {
                resource = resource.queryParam("odVremena", odVremena);
            }
            resource = resource.path(java.text.MessageFormat.format("{0}/avioni", new Object[]{id}));
            return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        }

        public String postJson(Object requestEntity) throws ClientErrorException {
            return webTarget.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), String.class);
        }

        public String getJsonId(String id) throws ClientErrorException {
            WebTarget resource = webTarget;
            resource = resource.path(java.text.MessageFormat.format("{0}", new Object[]{id}));
            return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        }

        public String deleteJson(String id) throws ClientErrorException {
            return webTarget.path(java.text.MessageFormat.format("{0}", new Object[]{id})).request().delete(String.class);
        }

        public String getJson() throws ClientErrorException {
            WebTarget resource = webTarget;
            return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        }

        public void close() {
            client.close();
        }
    }

    private java.util.List<org.foi.nwtis.antpofuk.ws.serveri.Aerodrom> dajSveAerodrome() {
        // Note that the injected javax.xml.ws.Service reference as well as port objects are not thread safe.
        // If the calling of port operations may lead to race condition some synchronization is required.
        org.foi.nwtis.antpofuk.ws.serveri.AIRP2WS port = ser.getAIRP2WSPort();
        return port.dajSveAerodrome();
    }



}
