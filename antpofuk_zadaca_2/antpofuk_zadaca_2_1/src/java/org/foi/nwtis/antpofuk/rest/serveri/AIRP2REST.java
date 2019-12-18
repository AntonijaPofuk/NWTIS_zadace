package org.foi.nwtis.antpofuk.rest.serveri;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.servlet.ServletContext;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.foi.nwtis.antpofuk.konfiguracije.Konfiguracija;
import org.foi.nwtis.antpofuk.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.antpofuk.web.podaci.Aerodrom;
import org.foi.nwtis.antpofuk.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.rest.klijenti.LIQKlijent;
import org.foi.nwtis.rest.klijenti.OSKlijent;
import org.foi.nwtis.rest.podaci.AvionLeti;
import org.foi.nwtis.rest.podaci.Lokacija;

/**
 * Klasa sluzi za izvršavanje AIRP"REST servisa
 *
 * @author Antonija Pofuk
 */
@Path("aerodromi")
public class AIRP2REST {

    private UriInfo context;
    private static ServletContext sc;
    BP_Konfiguracija bp_konf;
    String server;
    String baza;
    String korisnik;
    String lozinka;
    String driver;
    Konfiguracija konf;
    String apikey;
    Aerodrom aerodrom;
    String token;
    String username;
    String password;

    public AIRP2REST() {
    }

    /**
     * Metoda provjerava postoji li vec aerodrom s tim ICAO-kodom Ako postoji
     * vraca se true u suprotnom false
     *
     * @return Boolean
     * @author Antonija Pofuk
     */
    private boolean provjeraAerodroma(String id) {
        boolean postoji = false;
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AIRP2REST.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);) {
            String upit = "SELECT * FROM MYAIRPORTS WHERE IDENT = '" + id + "'";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(upit);
            while (rs.next()) {
                postoji = true;
                System.out.println("Aerodrom postoji");
            }
        } catch (SQLException ex) {
            System.out.println("GRESKA: " + ex);
            postoji = false;
        }
        return postoji;
    }

    private void ucitajKonfiguraciju() {
        sc = SlusacAplikacije.getServletContext();
        bp_konf = (BP_Konfiguracija) sc.getAttribute("BP_Konfig");
        server = bp_konf.getServerDatabase();
        baza = server + bp_konf.getUserDatabase();
        korisnik = bp_konf.getUserUsername();
        lozinka = bp_konf.getUserPassword();
        driver = bp_konf.getDriverDatabase();
        konf = (Konfiguracija) sc.getAttribute("Konfiguracija");
        apikey = konf.dajPostavku("apikey");
        token = konf.dajPostavku("token");
        username = konf.dajPostavku("username");
        password = konf.dajPostavku("password");
    }

    /**
     * Metoda vraca podatke iz tablice MYAIRPORTS
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJson() {
        ucitajKonfiguraciju();
        Gson gson = new Gson();
        String odgovor = null;
        List<Aerodrom> aerodromi = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);) {
            String upit = "SELECT IDENT,NAME,ISO_COUNTRY FROM MYAIRPORTS";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(upit);
            while (rs.next()) {
                aerodromi.add(new Aerodrom(rs.getString("IDENT"), rs.getString("NAME"),
                        rs.getString("ISO_COUNTRY"), null));
            }
            odgovor = gson.toJson(aerodromi);
            if (odgovor != null) {
                odgovor = Json.createObjectBuilder()
                        .add("odgovor", odgovor)
                        .add("status", "OK")
                        .build().toString();
            } else {
                odgovor = Json.createObjectBuilder()
                        .add("odgovor", Json.createArrayBuilder())
                        .add("status", "ERR")
                        .add("poruka", "Nema podataka!")
                        .build().toString();
            }
        } catch (SQLException ex) {
            System.out.println("GRESKA: " + ex);
        }
        return odgovor;
    }

    /**
     * Metoda ažurira podatke o odabranom aerodromu
     *
     * @param podaci String
     * @return String
     * @author Antonija Pofuk
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public String putJson(@PathParam("id") String id, String podaci) {
        ucitajKonfiguraciju();
        boolean ima = false;
        String odgovor = "";
        String naziv, adresa;
        try {
            JsonObject jsonObject = new JsonParser().parse(podaci).getAsJsonObject();
            naziv = jsonObject.get("naziv").getAsString();
            adresa = jsonObject.get("adresa").getAsString();
            System.out.println("Aerodrom postoji i moze se azurirati!");
            ima = azurirajAerodrom(id, naziv, adresa);

        } catch (JsonSyntaxException e) {
            System.out.println("GRESKA: " + e);
        }
        if (ima) {
            odgovor = Json.createObjectBuilder()
                    .add("odgovor", Json.createArrayBuilder())
                    .add("status", "OK")
                    .build()
                    .toString();
        } else {
            odgovor = Json.createObjectBuilder()
                    .add("odgovor", Json.createArrayBuilder())
                    .add("status", "ERR")
                    .add("poruka", "Pogreska kod azuriranja podataka u bazi! Provjerite ICAO.")
                    .build()
                    .toString();
        }
        return odgovor;
    }

    private boolean azurirajAerodrom(String id, String naziv, String adresa) {
        boolean update = false;
        ucitajKonfiguraciju();
        LIQKlijent lIQKlijent = new LIQKlijent(token);
        Lokacija geoLocation = lIQKlijent.getGeoLocation(id);
        String upit = "UPDATE MYAIRPORTS SET NAME ='" + naziv + "', ISO_COUNTRY ='"
                + adresa + "', COORDINATES =" + geoLocation + ", STORED = CURRENT_TIMESTAMP "
                + "  WHERE IDENT = " + id;
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AIRP2REST.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                Statement stmt = con.createStatement();) {
            stmt.execute(upit);
            update = true;
            stmt.close();
            con.close();
        } catch (Exception e) {
            System.out.println("GRESKA: " + e.getMessage());
            update = false;
        } finally {
            return update;
        }
    }

    /**
     * GET čita podatke iz tablice MYAIRPORTS s odgovarajucim ICAO-om
     *
     * @param id
     * @return
     */
    @Path("{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJsonId(@PathParam("id") String id) {
        JsonArrayBuilder json = Json.createArrayBuilder();
        ucitajKonfiguraciju();
        String odgovor = null;
        if (provjeraAerodroma(id)) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(AIRP2REST.class.getName()).log(Level.SEVERE, null, ex);
            }
            try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);) {
                String upit = "SELECT IDENT, NAME, ISO_COUNTRY FROM MYAIRPORTS WHERE IDENT = '" + id + "'";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(upit);
                if (rs != null) {
                    while (rs.next()) {
                        json.add(Json.createObjectBuilder()
                                .add("icao", rs.getString("IDENT"))
                                .add("naziv", rs.getString("NAME"))
                                .add("iso_country", rs.getString("iso_country"))
                        );
                    }
                    odgovor = Json.createObjectBuilder().add("odgovor", json).add("status", "OK").build().toString();
                }
            } catch (SQLException ex) {
                System.out.println("GRESKA: " + ex);
                odgovor = Json.createObjectBuilder().add("odgovor", Json.createArrayBuilder())
                        .add("status", "ERR")
                        .add("poruka", "Pogreška kod dohvaćanja podataka!")
                        .build().toString();
            }
        } else {
            System.out.println("Nema zapisa pod tim id-om u bazi!");
            odgovor = Json.createObjectBuilder().add("odgovor", Json.createArrayBuilder())
                    .add("status", "ERR")
                    .add("poruka", "Nema podataka za taj aerodrom!")
                    .build().toString();
        }
        return odgovor;
    }

    @Path("{id}/avioni")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJsonIdAvioni(@PathParam("id") String id,
            @QueryParam("odVremena") int odVremena, @QueryParam("doVremena") int doVremena) {
        OSKlijent oSKlijent = new OSKlijent(username, password);
        List<AvionLeti> avioni = oSKlijent.getDepartures(id, odVremena, doVremena);
        JsonArrayBuilder json = Json.createArrayBuilder();
        ucitajKonfiguraciju();
        String odgovor = null;
        if (provjeraAerodroma(id)) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(AIRP2REST.class.getName()).log(Level.SEVERE, null, ex);
            }
            try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);) {
                String upit = "SELECT * FROM AIRPLANES WHERE ESTDEPARTUREAIRPORT ='" + id + "' AND LASTSEEN "
                        + " BETWEEN " + odVremena + " AND " + doVremena;
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(upit);
                if (rs != null) {
                    while (rs.next()) {
                        json.add(Json.createObjectBuilder()
                                .add("icao", rs.getString("ESTDEPARTUREAIRPORT"))
                                .add("LASTSEEN", rs.getString("LASTSEEN"))
                        );
                    }
                    odgovor = Json.createObjectBuilder().add("odgovor", json).add("status", "OK").build().toString();
                }
            } catch (SQLException ex) {
                System.out.println("GRESKA: " + ex);
                odgovor = Json.createObjectBuilder().add("odgovor", Json.createArrayBuilder())
                        .add("status", "ERR")
                        .add("poruka", "Pogreška kod dohvaćanja podataka!")
                        .build().toString();
            }
        } else {
            System.out.println("Nema zapisa pod tim id-om u bazi!");
            odgovor = Json.createObjectBuilder().add("odgovor", Json.createArrayBuilder())
                    .add("status", "ERR")
                    .add("poruka", "Nema podataka za taj aerodrom!")
                    .build().toString();
        }
        return odgovor;
    }

    /**
     * Metoda upisuje podatke o aerodromu u bazu Vraca true ako je aerodrom
     * dodan odnosno false ako nije
     *
     * @param naziv
     * @return boolean true/false
     * @author Antonija Pofuk
     */
    private boolean dodajAerodrom(String icao) {
        boolean add = false;
        LIQKlijent lIQKlijent = new LIQKlijent(apikey);
        Lokacija lokacija = lIQKlijent.getGeoLocation(icao);
        List<Aerodrom> aerodromi = new ArrayList<>();
        String select = "SELECT * FROM AIRPORTS"
                + "  WHERE IDENT = '" + icao + "'";
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AIRP2REST.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                Statement s = con.createStatement();
                ResultSet rs = s.executeQuery((select));) {
            while (rs.next()) {
                aerodromi.add(new Aerodrom(rs.getString("IDENT"), rs.getString("NAME"), rs.getString("ISO_COUNTRY"), null));
            }
            for (Aerodrom a : aerodromi) {
                String insert = "INSERT INTO MYAIRPORTS (IDENT, NAME, ISO_COUNTRY, COORDINATES, STORED)"
                        + "VALUES ('" + icao + "' , '" + a.getNaziv() + "', '" + a.getDrzava() + "', '" + null + "', CURRENT_TIMESTAMP)";
                s.executeUpdate(insert);
                s.close();
                con.close();
                System.out.println("Aerodrom '" + icao + "' je dodan u MYAIRPORTS");
                add = true;
            }
        } catch (Exception e) {
            System.out.println("GRESKA: " + e.getMessage());
        }
        return add;
    }

    /**
     * Metoda upisuje novi aerodrom
     *
     * @param podaci
     * @author Antonija Pofuk
     * @return String
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String postJson(String podaci) {
        ucitajKonfiguraciju();
        boolean dodan = false;
        String odgovor = "";
        String icao;
        try {
            JsonObject jsonObject = new Gson().fromJson(podaci, JsonObject.class);
            icao = jsonObject.get("icao").getAsString();
            System.out.println(icao);
            if (provjeraAerodroma(icao) == false) {
                List<Aerodrom> aerodromi = new ArrayList<>();
                String select = "SELECT * FROM AIRPORTS WHERE IDENT = '" + icao + "'";
                LIQKlijent lIQKlijent = new LIQKlijent(token);
                try {
                    Class.forName(driver);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(AIRP2REST.class.getName()).log(Level.SEVERE, null, ex);
                }
                try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                        Statement s = con.createStatement();
                        ResultSet rs = s.executeQuery((select));) {
                    while (rs.next()) {
                        aerodromi.add(new Aerodrom(rs.getString("IDENT"), rs.getString("NAME"), rs.getString("ISO_COUNTRY"), null));
                    }
                    for (Aerodrom a : aerodromi) {
                        String insert = "INSERT INTO MYAIRPORTS (IDENT, NAME, ISO_COUNTRY, COORDINATES, STORED)"
                                + "VALUES ('" + icao + "' , '" + a.getNaziv() + "', '" + a.getDrzava() + "', '" + null + "',CURRENT_TIMESTAMP)";
                        s.executeUpdate(insert);
                        stvoriPoruku("Aerodrom je dodan");
                        System.out.println("Aerodrom je dodan u MYAIRPORTS");
                        dodan = true;
                    }
                } catch (Exception e) {
                    System.out.println("GRESKA: " + e);
                    stvoriPoruku("Aerodrom nije dodan");
                    dodan = false;
                }
            } else {
                System.out.println("Aerodrom postoji vec");
                stvoriPoruku("Aerodrom vec postoji");
                dodan = false;
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Aerodrom nije moguce dodat: " + e);
            stvoriPoruku("Aerodrom nije dodan");
            dodan = false;
        }
        if (dodan) {
            odgovor = Json.createObjectBuilder()
                    .add("odgovor", Json.createArrayBuilder())
                    .add("status", "OK")
                    .build()
                    .toString();
        } else {
            odgovor = Json.createObjectBuilder()
                    .add("odgovor", Json.createArrayBuilder())
                    .add("status", "ERR")
                    .add("poruka", "Pogreska kod upisa aerodroma podataka u bazu! Provjerite naziv.")
                    .build()
                    .toString();
        }
        return odgovor;
    }

    public boolean obrisiAerodrom(String icao) {
        String upit = "DELETE from MYAIRPORTS WHERE IDENT = '" + icao + "'";
        boolean del = false;
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AIRP2REST.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                Statement stmt = con.createStatement();) {
            stmt.execute(upit);
            del = true;
            stmt.close();
            con.close();
            stvoriPoruku("Aerodromje obrisan");
        } catch (Exception e) {
            System.out.println("GRESKA: " + e.getMessage());
            stvoriPoruku("Aerodrom nije obrisan");
        } finally {
            return del;
        }
    }

    /**
     * Metoda briše aerodrome pod zadanim ICAO kodom
     *
     * @param id
     * @return String
     * @author Antonija Pofuk
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public String deleteJson(@PathParam("id") String id) {
        ucitajKonfiguraciju();
        boolean del = false;
        String odgovor = "";
        try {
            del = obrisiAerodrom(id);

        } catch (NumberFormatException e) {
            del = false;
        }
        if (del) {
            odgovor = Json.createObjectBuilder()
                    .add("odgovor", Json.createArrayBuilder())
                    .add("status", "OK")
                    .build()
                    .toString();
        } else {
            odgovor = Json.createObjectBuilder()
                    .add("odgovor", Json.createArrayBuilder())
                    .add("status", "ERR")
                    .add("poruka", "Pogreska kod brisanja podataka! Provjerite ICAO.")
                    .build()
                    .toString();
        }
        return odgovor;
    }
  private void stvoriPoruku(String message) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FacesMessage facesMessage = new FacesMessage(message);
        facesContext.addMessage(null, facesMessage);
    }
}
