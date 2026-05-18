package ch.bfs.meb.sba.server.mail;

import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.business.BOBase;
import ch.bfs.meb.server.commons.mail.MailTemplate;

import java.util.Date;
import java.util.Locale;
public class FileUploadMail extends SbaMailBase{
    private static final String NOTIFCATION_MAIL = "notification";
    private final String _uploadFileName;
    private final String _cantonText;
    private final String _username;
    private final String _locale;
    private final String _mailMessage;

    public FileUploadMail(String username, Long canton, Long version, String locale, IIdmUserService idmService, String uploadFileName, String cantonText) {
        super(username, canton, version, idmService);
        _uploadFileName = uploadFileName;
        _username = username;
        _locale = locale;
        _cantonText = cantonText;
        _mailMessage = NOTIFCATION_MAIL;

    }


    @Override
    public String getSubject() {
        return "SBA-Notification";
    }

    @Override
    public String getMailBody() {
        class MailParameter {
            private final String uploadFileName = _uploadFileName;
            private final Long version = _version;
            private final String username = _username;
            private final String cantonText = _cantonText;

            @SuppressWarnings("unused")
            public String getUsername() {
                return username;
            }

            public String getUploadFileName() {
                return String.valueOf(uploadFileName);
            }
            @SuppressWarnings("unused")
            public Long getVersion() {
                return version;
            }

            public String getCantonText() { return cantonText;}
        }
        return new MailTemplate(_mailMessage, MailTemplate.TEMPLATE_RESOURCE_LOCATION_SBA, new Locale(_locale), new MailParameter()).parse();
    }
}
