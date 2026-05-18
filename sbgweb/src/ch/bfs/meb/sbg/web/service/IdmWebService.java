/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2011 GLANCE AG, Switzerland
 * 
 * $Id: IdmWebServiceFacade.java 132 2011-01-21 10:21:02Z msc $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.bfs.meb.security.MebPreAuthUserDetailsService;
import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.security.idm.User;
import ch.bfs.meb.util.SecurityConstants;

/**
 * Facade to access IDM
 * 
 * @author $Author: msc $
 * @version $Revision: 132 $
 */
@Service("idmWebService")
public class IdmWebService implements IUserService {
    @Autowired
    private MebPreAuthUserDetailsService mebPreAuthUserDetailsService;

    public IdmWebService() {}

    protected IIdmUserService getIdmUserService() {
        return mebPreAuthUserDetailsService.getIdmUserService();
    }

    /**
     * Getting canton for a user by searching all authenticator providers of
     * weblogic
     * 
     * @param user
     * @return canton code value as Long if available, else null
     */
    public Long getCanton(String user) {
        String cantons = getIdmUserService().getCantons(user);
        if (cantons.equals("")) {
            return 0L;
        }
        return new Long(cantons);
    }

    public String getEv() {
        String ev = null;
        String evGuid;
        boolean evMemberIsEa;

        List<User> evGroup = getIdmUserService().getUsersForRole(SecurityConstants.ROLE_SBG_EV);
        if (evGroup != null && evGroup.size() > 0) {
            List<User> eaGroup = getIdmUserService().getUsersForRole(SecurityConstants.ROLE_SBG_EA);
            if (eaGroup != null) {
                for (User evMember : evGroup) {
                    evGuid = evMember.getGuid();
                    evMemberIsEa = false;
                    for (User eaMember : eaGroup) {
                        if (evGuid.equals(eaMember.getGuid())) {
                            evMemberIsEa = true;
                            break;
                        }
                    }
                    if (!evMemberIsEa) {
                        ev = evMember.getUsername();
                        break; // right EV who is not EA found
                    }
                }
            }

            if (ev == null) {
                ev = evGroup.get(0).getUsername();
            }
        }

        return ev;
    }
}
