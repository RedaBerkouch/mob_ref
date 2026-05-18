package ch.bfs.meb.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Encrypts and decrypts the security token that is transferred from presentation layer to service layer.
 */
@Slf4j
public class MebSecurityTokenCrypter {

    /** Avoid construction. */
    private MebSecurityTokenCrypter() {}

    public static String encrypt(String userName, String email, Collection<? extends GrantedAuthority> authorities) {
        String mebToken = "";

        String token = userName;
        token += ",";
        token += email;

        for (GrantedAuthority authority : authorities) {
            token += ",";
            token += authority.getAuthority();
        }
        log.debug("Token: {}", token);
        try {
            Encrypter encrypter = new Encrypter(MebTokenConstants.KEYPHRASE);

            mebToken = encrypter.encrypt(token);
        } catch (EncryptionException e) {
            log.error("Could not add token due to an encryption problem", e);
        }
        return mebToken;
    }

    public static String getPrincipal(String encryptedSecurityToken) {
        String principal = null;

        try {
            Encrypter encrypter = new Encrypter(MebTokenConstants.KEYPHRASE);
            String header = encrypter.decrypt(encryptedSecurityToken);

            // gets the first value of the token with the users name
            StringTokenizer tokenizer = new StringTokenizer(header, ",");
            principal = (String) tokenizer.nextElement();

            if (log.isDebugEnabled()) {
                log.debug("Authorization header: " + principal);
            }
        } catch (EncryptionException e) {
            log.error("Could not determine principal due to an encryption problem", e);
        }

        return principal;
    }

    public static String getEmail(String encryptedSecurityToken) {
        String email = null;

        try {
            Encrypter encrypter = new Encrypter(MebTokenConstants.KEYPHRASE);
            String header = encrypter.decrypt(encryptedSecurityToken);

            // gets the first value of the token with the users name
            StringTokenizer tokenizer = new StringTokenizer(header, ",");
            tokenizer.nextElement(); // first element is user name, ignore
            email = (String) tokenizer.nextElement();

            if (log.isDebugEnabled()) {
                log.debug("Authorization header: " + email);
            }
        } catch (EncryptionException e) {
            log.error("Could not determine principal due to an encryption problem", e);
        }

        return email;
    }

    public static List<GrantedAuthority> getUserRoles(String encryptedSecurityToke) {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

        try {
            Encrypter encrypter = new Encrypter(MebTokenConstants.KEYPHRASE);
            String header = encrypter.decrypt(encryptedSecurityToke);

            if (log.isDebugEnabled()) {
                log.debug("Authorization header: " + header);
            }

            StringTokenizer t = new StringTokenizer(header, ",");
            t.nextElement(); // first element is user name, ignore
            t.nextElement(); // second element is email, ignore
            while (t.hasMoreElements()) {
                authorities.add(new SimpleGrantedAuthority((String) t.nextElement()));
            }
        } catch (EncryptionException e) {
            log.error("Could not determine authorities due to an encryption problem, no roles are added", e);
        }

        return authorities;
    }

}
