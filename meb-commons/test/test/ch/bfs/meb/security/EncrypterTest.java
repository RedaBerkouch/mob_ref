/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id$

 */
package test.ch.bfs.meb.security;

import org.junit.Assert;
import org.junit.Test;

import ch.bfs.meb.security.Encrypter;
import ch.bfs.meb.security.EncryptionException;

/**
 * TODO Document this class
 *
 */
public class EncrypterTest {

    @Test
    public void encryptPositive() throws EncryptionException {
        Encrypter encrypter = new Encrypter("mysecret");

        String testString = "TEST";

        String encryptedString = encrypter.encrypt(testString);

        Assert.assertNotNull(encryptedString, "encryption failed");

        String result = encrypter.decrypt(encryptedString);

        Assert.assertEquals(result, testString);
    }

    @Test(expected = EncryptionException.class)
    public void encryptNegative() throws EncryptionException {
        Encrypter encrypter = new Encrypter("secret1");
        Encrypter decrypter = new Encrypter("secret2");

        String testString = "TEST";

        String encryptedString = encrypter.encrypt(testString);

        Assert.assertNotNull(encryptedString, "encryption failed");

        decrypter.decrypt(encryptedString);
    }

    @Test(expected = EncryptionException.class)
    public void encryptNegative2() throws EncryptionException {
        Encrypter decrypter = new Encrypter("secret");

        String testString = "TEST";

        decrypter.decrypt(testString);
    }
}
