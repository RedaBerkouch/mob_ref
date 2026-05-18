/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$

 */
package ch.bfs.meb.security;

import java.util.Collection;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.extern.slf4j.Slf4j;

/**
 * Handler to add the security token to all outgoing service calls
 * 
 */
@Slf4j
public class MebTokenHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Logger LOG = LoggerFactory.getLogger(MebTokenHandler.class);

    public boolean handleMessage(SOAPMessageContext messageContext) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities(); // best guess for old identity management or local development environment
        String email = userDetails.getUsername(); // best guess for old identity management or local development environment

        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof MebUser) {
            MebUser mebUser = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            email = mebUser.getEmail();
            authorities = mebUser.getAuthorities();
        }

        try {
            String mebToken = MebSecurityTokenCrypter.encrypt(userDetails.getUsername(), email, authorities);

            // write security token to soap header
            SOAPMessage message = messageContext.getMessage();

            SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();

            SOAPHeader header = message.getSOAPHeader();
            if (header == null)
                header = envelope.addHeader();

            SOAPElement securityToken = header.addChildElement(MebTokenConstants.TOKENNAME, MebTokenConstants.TOKENNAMESPACEPREFIX,
                    MebTokenConstants.TOKENNAMESPACEURI);
            securityToken.addTextNode(mebToken);
        } catch (SOAPException e) {
            LOG.error("Could not add token due to an SOAP problem", e);
        }

        return true;
    }

    /**
     * @see javax.xml.ws.handler.Handler#close(javax.xml.ws.handler.MessageContext
     *      )
     */
    @Override
    public void close(MessageContext context) {
        return;
    }

    /**
     * @seejavax.xml.ws.handler.Handler#handleFault(javax.xml.ws.handler. 
     *                                                                    MessageContext
     *                                                                    )
     */
    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return false;
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }
}