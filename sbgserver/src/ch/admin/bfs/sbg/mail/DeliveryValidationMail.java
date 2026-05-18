package ch.admin.bfs.sbg.mail;

import java.util.Locale;

import ch.bfs.meb.sbg.server.configuration.ISbgServerConfiguration;
import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.mail.MailTemplate;

public class DeliveryValidationMail extends SbgMailBase {
    protected final String locale;

    public DeliveryValidationMail(String userEmail, IIdmUserService idmService, String locale, ISbgServerConfiguration configuration) {
        super(userEmail, idmService, configuration);
        this.locale = locale;
    }

    public String getSubject() {
        return "Delivery Validation";
    }

    public String getMailBody() {
        return new MailTemplate("deliveryValidation", MailTemplate.TEMPLATE_RESOURCE_LOCATION_SBG, new Locale(locale), null).parse();
    }
}
