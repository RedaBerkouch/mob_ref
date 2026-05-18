/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: SbaMailBase.java 821 2010-02-24 07:05:54Z dzw $
 */
package ch.bfs.meb.sba.server.mail;

import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.InternetAddress;

import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.mail.IMail;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.MebUtils;
import ch.bfs.meb.util.SecurityConstants;

/**
 * Base class for mails from the MEB portal application.
 * 
 * @author $Author: dzw $
 * @version $Revision: 821 $
 */
public abstract class SbaMailBase implements IMail {
    private final String _username;
    private final Long _canton;
    final Long _version;
    private final IIdmUserService _idmService;

    SbaMailBase(String username, Long canton, Long version, IIdmUserService idmService) {
        _username = username;
        _canton = canton;
        _version = version;
        _idmService = idmService;
    }

    public List<InternetAddress> getRecepients() {
        List<InternetAddress> recipients;

        if (!_idmService.isUserInRole(_username, SecurityConstants.ROLE_SBA_EA) && !_idmService.isUserInRole(_username, SecurityConstants.ROLE_SBA_EV)
                && !_idmService.isUserInRole(_username, SecurityConstants.ROLE_SBA_DV)) {
            // case ROLE_SBA_DL --> prevalidation
            recipients = new ArrayList<>(_idmService.getDVMailAddresses(CodegroupUtility.MEB_APPLICATION_SSP, _canton));
        } else {
            // --> validation
            //			recipients = _idmService.getEVMailAddresses (CodegroupUtility.MEB_APPLICATION_SBA);
            recipients = new ArrayList<>();
            if (!_idmService.isUserInRole(_username, SecurityConstants.ROLE_SBA_EA) && !_idmService.isUserInRole(_username, SecurityConstants.ROLE_SBA_EV)) {
                InternetAddress adr = new InternetAddress();
                adr.setAddress(MebUtils.getMebSupportMailAddress());
                recipients.add(adr);
            }
        }
        InternetAddress adr = new InternetAddress();
        adr.setAddress(_username);
        recipients.add(adr);

        // Ajout SBA upload notifications
        InternetAddress supportAdr = new InternetAddress();
        supportAdr.setAddress("meb-support@bfs.admin.ch");
        recipients.add(supportAdr);


        return recipients;
    }

    public InternetAddress[] getRecepientsAsArray() {

        return getRecepients().toArray(new InternetAddress[0]);
    }

    public InternetAddress getFrom() {
        InternetAddress adr = new InternetAddress();
        adr.setAddress("Noreply-bild@bfs.admin.ch");
        return adr;
    }

    public String getContent() {
        return "text/plain";
    }

    public String getSalutation() {
        return "Ciao";
    }
}
