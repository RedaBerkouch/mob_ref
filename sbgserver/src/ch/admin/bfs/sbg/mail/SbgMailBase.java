package ch.admin.bfs.sbg.mail;

import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.InternetAddress;

import ch.bfs.meb.sbg.server.configuration.ISbgServerConfiguration;
import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.security.idm.User;
import ch.bfs.meb.server.commons.mail.IMail;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.util.StringUtils;

public abstract class SbgMailBase implements IMail {
    protected final String userEmail;
    protected final IIdmUserService idmService;
    private final ISbgServerConfiguration configuration;
    public SbgMailBase(String userEmail, IIdmUserService idmService, ISbgServerConfiguration configuration) {
        this.userEmail = userEmail;
        this.idmService = idmService;
        this.configuration = configuration;
    }

    protected String getEvMail() {
        String firstEv = null;
        String ev = null;
        String evGuid;
        boolean evMemberIsEa;

        List<User> evGroup = idmService.getUsersForRole(SecurityConstants.ROLE_SBG_EV);
        if (evGroup != null && evGroup.size() > 0) {
            List<User> eaGroup = idmService.getUsersForRole(SecurityConstants.ROLE_SBG_EA);
            if (eaGroup != null) {
                for (User evMember : evGroup) {
                    if (evMember.isActive()) {
                        if (firstEv == null) {
                            firstEv = evMember.getUsername();
                        }
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
            }

            if (ev == null) {
                ev = firstEv;
            }
        }

        return ev;
    }

    public List<InternetAddress> getRecepients() {
        ArrayList<InternetAddress> recipients = new ArrayList<InternetAddress>();

        InternetAddress adr = new InternetAddress();
        adr.setAddress(userEmail);
        recipients.add(adr);
        String evMail = getEvMail();
        if (!StringUtils.isEmpty(evMail)) {
            adr = new InternetAddress();
            adr.setAddress(evMail);
            recipients.add(adr);
        }

        adr = new InternetAddress();
        adr.setAddress("meb-support@bfs.admin.ch");
        recipients.add(adr);

        return recipients;
    }

    public InternetAddress[] getRecepientsAsArray() {
        List<InternetAddress> recipients = getRecepients();
        return recipients.toArray(new InternetAddress[0]);
    }

    public InternetAddress getFrom() {
        InternetAddress adr = new InternetAddress();
        adr.setAddress(configuration.getConfigurationMailFrom());
        return adr;
    }

    public String getContent() {
        return "text/plain";
    }

    public String getSalutation() {
        return "Ciao";
    }
}
