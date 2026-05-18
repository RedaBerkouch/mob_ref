/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id$
 */
package ch.bfs.meb.server.commons.mail;

import java.util.List;

import javax.mail.internet.InternetAddress;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public interface IMail {

    public String getSubject();

    public List<InternetAddress> getRecepients();

    public InternetAddress[] getRecepientsAsArray();

    public InternetAddress getFrom();

    public String getContent();

    public String getMailBody();

    public String getSalutation();
}
