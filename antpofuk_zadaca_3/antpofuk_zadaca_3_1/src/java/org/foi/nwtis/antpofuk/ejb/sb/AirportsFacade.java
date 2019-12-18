
package org.foi.nwtis.antpofuk.ejb.sb;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.foi.nwtis.antpofuk.ejb.eb.Airports;

/**
 *
 * @author Anotnija Pofuk
 */
@Stateless
public class AirportsFacade extends AbstractFacade<Airports> {

    @PersistenceContext(unitName = "NWTiS_DZ3_PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public AirportsFacade() {
        super(Airports.class);
    }
    
}
