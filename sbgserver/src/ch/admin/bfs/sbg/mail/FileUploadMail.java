package ch.admin.bfs.sbg.mail;

import ch.bfs.meb.sbg.server.configuration.ISbgServerConfiguration;
import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.business.BOBase;
import ch.bfs.meb.server.commons.mail.MailTemplate;

import java.util.Date;
import java.util.Locale;

public class FileUploadMail extends SbgMailBase{
    private static final String NOTIFCQTION_MAIL = "notification";
    private final String _uploadFileName;
    private final Long _version;
    private final String _username;
    private final String _cantonText;
    private final String _locale;
    private final String _mailMessage;

    public FileUploadMail(String username, String cantonText, Long version, String locale, IIdmUserService idmService, String uploadFileName, ISbgServerConfiguration configuration) {
        super(username, idmService,configuration);
        _uploadFileName = uploadFileName;
        _username = username;
        _locale = locale;
        _mailMessage = NOTIFCQTION_MAIL;
        _version = version;
        _cantonText = cantonText;
    }


    @Override
    public String getSubject() {
        return "SBG-Notification";
    }

    @Override
    public String getMailBody() {
        class MailParameter {
            private final String uploadFileName = _uploadFileName;
            private final Long version = _version;
            private final String username = _username;
            private final String cantonText = _cantonText;

            public String getUsername() {
                return username;
            }

            public String getCantonText() {
                return cantonText;
            }

            public String getUploadFileName() {
                return uploadFileName;
            }

            public Long getVersion() {
                return version;
            }
        }
        return new MailTemplate(_mailMessage, MailTemplate.TEMPLATE_RESOURCE_LOCATION_SBG, new Locale(_locale), new MailParameter()).parse();
    }
}

