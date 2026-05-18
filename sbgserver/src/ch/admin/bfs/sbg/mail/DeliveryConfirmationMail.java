package ch.admin.bfs.sbg.mail;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import ch.admin.bfs.sbg.business.BOBase;
import ch.bfs.meb.sbg.server.configuration.ISbgServerConfiguration;
import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.commons.mail.MailTemplate;
import ch.bfs.meb.util.CodegroupUtility;

public class DeliveryConfirmationMail extends SbgMailBase {
    private final String _locale;
    private final Date _deliveryDate;
    private final String _deliveryUser;
    private final Long _canton;
    private final Long _year;
    private final int _deliveryPersons;
    private final int _nrPersons;
    private final int _nrEvents;
    private final ICodegroupManager _codegroupManager;

    public DeliveryConfirmationMail(String userEmail, IIdmUserService idmService, ICodegroupManager codegroupManager, String locale, Date deliveryDate,
            String deliveryUser, Long canton, Long year, int deliveryPersons, int totalPersons, int totalEvents, ISbgServerConfiguration configuration) {
        super(userEmail, idmService, configuration);

        _locale = locale;
        _deliveryDate = deliveryDate;
        _deliveryUser = deliveryUser;
        _canton = canton;
        _year = year;
        _deliveryPersons = deliveryPersons;
        _nrPersons = totalPersons;
        _nrEvents = totalEvents;
        _codegroupManager = codegroupManager;
    }

    public String getSubject() {
        return "Delivery Confirmation";
    }

    public String getMailBody() {
        class MailParameter {
            private final Date deliveryDate = _deliveryDate;
            private final String deliveryUser = _deliveryUser;
            private final Long canton = _canton;
            private final Long deliveryVersion = _year;
            private final int deliveryPersons = _deliveryPersons;
            private final int nrPersons = _nrPersons;
            private final int nrEvents = _nrEvents;

            @SuppressWarnings("unused")
            public String getDeliveryDate() {
                return BOBase.dateToString(deliveryDate);
            }

            @SuppressWarnings("unused")
            public String getDeliveryUser() {
                return deliveryUser;
            }

            @SuppressWarnings("unused")
            public String getCantonString() {
                return _codegroupManager.getCode(CodegroupUtility.CANTON, canton, _locale, (long) new GregorianCalendar().get(Calendar.YEAR)).getCodeText(); // newest version
            }

            @SuppressWarnings("unused")
            public Long getDeliveryVersion() {
                return deliveryVersion;
            }

            @SuppressWarnings("unused")
            public int getDeliveryPersons() {
                return deliveryPersons;
            }

            @SuppressWarnings("unused")
            public int getNrEvents() {
                return nrEvents;
            }

            @SuppressWarnings("unused")
            public int getNrPersons() {
                return nrPersons;
            }
        }
        return new MailTemplate("deliveryConfirmation", MailTemplate.TEMPLATE_RESOURCE_LOCATION_SBG, new Locale(_locale), new MailParameter()).parse();
    }
}
