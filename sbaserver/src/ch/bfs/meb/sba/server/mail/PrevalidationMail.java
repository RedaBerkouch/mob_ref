/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: PrevalidationMail.java 821 2010-02-24 07:05:54Z dzw $
 */
package ch.bfs.meb.sba.server.mail;

import java.util.Date;
import java.util.Locale;

import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.business.BOBase;
import ch.bfs.meb.server.commons.mail.MailTemplate;

public class PrevalidationMail extends SbaMailBase {
    private static final String PREVALIDATION_MAIL = "prevalidation";

    private final String _deliveryId;
    private final Date _prevalidationDate;
    private final String _prevalidationUser;
    private final Long _deliveryPersons;
    private final String _locale;
    private final String _cantonText;
    private final String _mailMessage;

    public PrevalidationMail(String deliveryId, Date prevalidationDate, String prevalidationUser, Long deliveryPersons, String locale, Long canton,
            Long version, String cantonText, IIdmUserService idmService) {
        super(prevalidationUser, canton, version, idmService);

        _deliveryId = deliveryId;
        _prevalidationDate = prevalidationDate;
        _prevalidationUser = prevalidationUser;
        _deliveryPersons = deliveryPersons;
        _locale = locale;
        _cantonText = cantonText;
        _mailMessage = PREVALIDATION_MAIL;
    }

    public String getUser() {
        return _prevalidationUser;
    }

    public String getSubject() {
        return _cantonText + ": SBA-Prevalidation Confirmation";
    }

    public String getMailBody() {
        class MailParameter {
            private final String deliveryId = _deliveryId;
            private final Date prevalidationDate = _prevalidationDate;
            private final String prevalidationUser = _prevalidationUser;
            private final Long deliveryPersons = _deliveryPersons;
            private final Long version = _version;

            @SuppressWarnings("unused")
            public String getDeliveryId() {
                return deliveryId;
            }

            @SuppressWarnings("unused")
            public String getPrevalidationDate() {
                return BOBase.dateToString(prevalidationDate);
            }

            @SuppressWarnings("unused")
            public String getPrevalidationUser() {
                return prevalidationUser;
            }

            @SuppressWarnings("unused")
            public Long getDeliveryPersons() {
                return deliveryPersons;
            }

            @SuppressWarnings("unused")
            public Long getVersion() {
                return version;
            }
        }

        return new MailTemplate(_mailMessage, MailTemplate.TEMPLATE_RESOURCE_LOCATION_SBA, new Locale(_locale), new MailParameter()).parse();
    }
}
