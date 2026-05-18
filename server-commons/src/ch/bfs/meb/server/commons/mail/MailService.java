/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id$
 */
package ch.bfs.meb.server.commons.mail;

import java.util.Date;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.InitialContext;
import javax.naming.NamingException;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Mail Service
 * @author $Author$
 * @version $Revision$
 */
public class MailService {
    private static final Log LOG = LogFactory.getLog(MailService.class);
    private static MailService _instance;
    private static final String JNDI_MAIL = "mail/meb";

    private MailService() {}

    /**
     * Returns the one and only instance.
     * @return the one and only instance
     */
    public static synchronized MailService getInstance() {
        if (_instance == null) {
            _instance = new MailService();
        }
        return _instance;
    }

    /** @return <code>true</code> if the mail was sent successfully, <code>false</code> otherwise. */
    public boolean sendMail(IMail mail) {
        try {
            String mailSessionName = System.getProperty("MAILSESSION_JNDI", JNDI_MAIL);

            InitialContext ctx = new InitialContext();
            Session session = (Session) ctx.lookup(mailSessionName);
            Message msg = new MimeMessage(session);
            msg.setHeader( "MIME-Version" , "1.0" );
            msg.setHeader( "Content-Type" , "text/plain; charset=\"UTF-8\"" );

            msg.setFrom(mail.getFrom());

            msg.setRecipients(Message.RecipientType.TO, mail.getRecepientsAsArray());
            msg.addRecipients(Message.RecipientType.BCC, new InternetAddress[]{new InternetAddress("meb-support@bfs.admin.ch")});
            String subject = mail.getSubject();
            msg.setSubject(subject == null ? "" : subject);
            msg.setSentDate(new Date());

            // Content is stored in a MIME multi-part message with one body part
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setContent( mail.getMailBody() , "text/plain; charset=\"UTF-8\"");
            mbp.setHeader( "MIME-Version" , "1.0" );
            mbp.setHeader( "Content-Type" , "text/plain; charset=\"UTF-8\"" );

            Multipart mp = new MimeMultipart();
            mp.addBodyPart(mbp);
            msg.setContent(mp);

            // Send the message.
            Transport.send(msg);

            return true;
        } catch (NamingException | MessagingException e) {
            LOG.error("Could not send mail", e);
        }

        return false;
    }
}