/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$

 */
package ch.bfs.meb.security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import weblogic.utils.encoders.BASE64Decoder;
import weblogic.utils.encoders.BASE64Encoder;

/**
 * Symetric keyphrase decrypter and encrypter for strings. This Encrypter uses
 * the PBEWithMD5AndDES encryption method. The secret keyphrase is set using the
 * constructor.
 * 
 * Usage: Encrypter encrypter = new Encrypter("secret");
 * 
 * String mySecretString = "secret string to encrypt";
 * 
 * 
 * // encrypt a string String encryptedString =
 * encrypter.encrypt(mySecretString);
 * 
 * // decrypt a string String decryptedString =
 * encrypter.decrypt(encryptedString);
 */
public class Encrypter {
    Cipher ecipher;
    Cipher dcipher;

    /**
     * 8-byte Salt
     */
    final byte[] salt = { (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32, (byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03 };

    /**
     * Iteration count
     */
    final int iterationCount = 19;

    /**
     * Initallyses the encrypter with a symetric key phrase
     * 
     * @param keyPhrase
     *            The key phrase to use
     */
    public Encrypter(String keyPhrase) throws EncryptionException {
        try {
            // Create the key
            KeySpec keySpec = new PBEKeySpec(keyPhrase.toCharArray(), salt, iterationCount);
            SecretKey skey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
            ecipher = Cipher.getInstance(skey.getAlgorithm());
            dcipher = Cipher.getInstance(skey.getAlgorithm());

            // Prepare the parameter to the ciphers
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);

            ecipher.init(Cipher.ENCRYPT_MODE, skey, paramSpec);
            dcipher.init(Cipher.DECRYPT_MODE, skey, paramSpec);

        } catch (javax.crypto.NoSuchPaddingException e) {
            throw new EncryptionException("Could not initialize encrypter", e);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new EncryptionException("Could not initialize encrypter", e);
        } catch (java.security.InvalidKeyException e) {
            throw new EncryptionException("Could not initialize encrypter", e);
        } catch (InvalidKeySpecException e) {
            throw new EncryptionException("Could not initialize encrypter", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new EncryptionException("Could not initialize encrypter", e);
        }
    }

    /**
     * Gets an encrypter with the given keyhrase
     * 
     * @param keyPhrase
     *            The key phrase to use
     * @return an initialized encrypter
     */
    public static Encrypter getEncrypter(String keyPhrase) throws EncryptionException {
        return new Encrypter(keyPhrase);
    }

    /**
     * Encrypts a string using the keyphrase set in the constructor. AS a
     * result, a base64 encoded string is returned.
     * 
     * @param str
     *            The string to encrypt
     * @return a base64 encoded and encrypted string
     */
    public String encrypt(String str) throws EncryptionException {
        try {
            // Encode the string into bytes using utf-8
            byte[] utf8 = str.getBytes("UTF8");

            // Encrypt
            byte[] enc = ecipher.doFinal(utf8);

            // Encode bytes to base64 to get a string
            return new BASE64Encoder().encodeBuffer(enc);
        } catch (javax.crypto.BadPaddingException e) {
            throw new EncryptionException("Could not encrypt string '" + str + "'", e);
        } catch (IllegalBlockSizeException e) {
            throw new EncryptionException("Could not encrypt string '" + str + "'", e);
        } catch (UnsupportedEncodingException e) {
            throw new EncryptionException("Could not encrypt string '" + str + "'", e);
        }
    }

    /**
     * Decrypts a base64 encoded string using the keyphrase set in the
     * constructor.
     * 
     * @param str
     *            The base64 decrypted string
     * @return The original string
     */
    public String decrypt(String str) throws EncryptionException {
        try {
            // Decode base64 to get bytes
            byte[] dec = new BASE64Decoder().decodeBuffer(str);

            // Decrypt
            byte[] utf8 = dcipher.doFinal(dec);

            // Decode using utf-8
            return new String(utf8, "UTF8");
        } catch (javax.crypto.BadPaddingException e) {
            throw new EncryptionException("Could not decrypt string '" + str + "'", e);
        } catch (IllegalBlockSizeException e) {
            throw new EncryptionException("Could not decrypt string '" + str + "'", e);
        } catch (UnsupportedEncodingException e) {
            throw new EncryptionException("Could not decrypt string '" + str + "'", e);
        } catch (java.io.IOException e) {
            throw new EncryptionException("Could not decrypt string '" + str + "'", e);
        }
    }
}
