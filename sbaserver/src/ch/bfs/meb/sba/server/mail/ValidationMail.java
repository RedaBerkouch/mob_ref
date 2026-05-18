/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: ValidationMail.java 892 2010-03-03 15:05:51Z dzw $
 */
package ch.bfs.meb.sba.server.mail;

import java.util.Date;
import java.util.Locale;

import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.business.BOBase;
import ch.bfs.meb.server.commons.mail.MailTemplate;

public class ValidationMail extends SbaMailBase {
    private static final String VALIDATION_MAIL = "validation";

    private final String _cantonName;
    private final Date _validationDate;
    private final String _validationUser;
    private final Long _nrPersons;
    private final String _locale;
    private final String _cantonText;
    private final String _mailMessage;

    public ValidationMail(String cantonName, Date validationDate, String validationUser, Long nrPersons, String locale, Long canton, Long version,
            String cantonText, IIdmUserService idmService) {
        super(validationUser, canton, version, idmService);

        _cantonName = cantonName;
        _validationDate = validationDate;
        _validationUser = validationUser;
        _nrPersons = nrPersons;
        _locale = locale;
        _cantonText = cantonText;
        _mailMessage = VALIDATION_MAIL;
    }

    public String getUser() {
        return _validationUser;
    }

    public String getSubject() {
        return _cantonText + ": SBA-Validation Confirmation";
    }

    public String getMailBody() {
        class MailParameter {
            private final String canton = _cantonName;
            private final Date validationDate = _validationDate;
            private final String validationUser = _validationUser;
            private final Long validPersons = _nrPersons;
            private final Long version = _version;

            @SuppressWarnings("unused")
            public String getCanton() {
                return canton;
            }

            @SuppressWarnings("unused")
            public String getValidationDate() {
                return BOBase.dateToString(validationDate);
            }

            @SuppressWarnings("unused")
            public String getValidationUser() {
                return validationUser;
            }

            @SuppressWarnings("unused")
            public Long getValidPersons() {
                return validPersons;
            }

            @SuppressWarnings("unused")
            public Long getVersion() {
                return version;
            }
        }

        return new MailTemplate(_mailMessage, MailTemplate.TEMPLATE_RESOURCE_LOCATION_SBA, new Locale(_locale), new MailParameter()).parse();
    }
}
