
package org.foi.nwtis.antpofuk.ejb.sb;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.foi.nwtis.antpofuk.ejb.eb.Passangers;

/**
 *
 * @author Antonija Pofuk
 */
@Stateless
public class PassangersFacade extends AbstractFacade<Passangers> {

    @PersistenceContext(unitName = "NWTiS_DZ3_PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public PassangersFacade() {
        super(Passangers.class);
    }
    
}
