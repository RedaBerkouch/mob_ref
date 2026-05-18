/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$
 */
package ch.bfs.meb.sdl.web.dhtmlx.taglib;

import ch.bfs.meb.configuration.ConfigurationBase;
import ch.bfs.meb.sdl.web.configuration.ISdlWebConfiguration;
import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.util.ApplicationContextProvider;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.taglib.DhtmlxTagBase;
import ch.bfs.meb.web.commons.dhtmlx.taglib.DhtmlxTagException;

/**
 * Get the mail address of the user with EV role from IDM service.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class EvMailTag extends DhtmlxTagBase {
    private static final long serialVersionUID = 1278469983762454859L;

    public void doTag() throws DhtmlxTagException {
        try {
            ISdlWebConfiguration configuration = (ISdlWebConfiguration) ApplicationContextProvider.getApplicationContext().getBean(ConfigurationBase.BEAN_NAME);
            String beanName = configuration.getIdmImplementation();
            IIdmUserService idmUserService = (IIdmUserService) ApplicationContextProvider.getApplicationContext().getBean(beanName);
            pageContext.getOut().print(idmUserService.getEVMailAddresses(CodegroupUtility.MEB_APPLICATION_SDL).get(0));
            pageContext.getOut().flush();
        } catch (Exception e) {
            try {
                pageContext.getOut().print(0);
            } catch (Exception exception) {
                // do nothing
            }
        }
    }
}
