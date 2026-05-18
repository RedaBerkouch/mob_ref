/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$
 */
package ch.bfs.meb.sdl.server.mail;

import java.util.Date;
import java.util.Locale;

import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.business.BOBase;
import ch.bfs.meb.server.commons.mail.MailTemplate;

public class ValidationMail extends SdlMailBase {
    private static final String VALIDATION_MAIL = "validation";

    private final String _cantonName;
    private final Date _validationDate;
    private final String _validationUser;
    private final Long _nrSchools;
    private final String _locale;
    private final String _cantonText;
    private final String _mailMessage;

    public ValidationMail(String cantonName, Date validationDate, String validationUser, Long nrSchools, String locale, Long canton, Long version,
            String cantonText, IIdmUserService idmService) {
        super(validationUser, canton, version, idmService);

        _cantonName = cantonName;
        _validationDate = validationDate;
        _validationUser = validationUser;
        _nrSchools = nrSchools;
        _locale = locale;
        _cantonText = cantonText;
        _mailMessage = VALIDATION_MAIL;
    }

    public String getUser() {
        return _validationUser;
    }

    public String getSubject() {
        return _cantonText + ": SdL-Validation Confirmation";
    }

    public String getMailBody() {
        class MailParameter {
            private final String canton = _cantonName;
            private final Date validationDate = _validationDate;
            private final String validationUser = _validationUser;
            private final Long validSchools = _nrSchools;
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
            public Long getValidSchools() {
                return validSchools;
            }

            @SuppressWarnings("unused")
            public Long getVersion() {
                return version;
            }
        }

        return new MailTemplate(_mailMessage, MailTemplate.TEMPLATE_RESOURCE_LOCATION_SDL, new Locale(_locale), new MailParameter()).parse();
    }
}
