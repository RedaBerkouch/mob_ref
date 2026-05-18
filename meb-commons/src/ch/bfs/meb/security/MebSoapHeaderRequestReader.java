package ch.bfs.meb.security;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import weblogic.utils.http.HttpChunkInputStream;

public class MebSoapHeaderRequestReader extends HttpServletRequestWrapper {
    private static final Logger LOG = LoggerFactory.getLogger(MebSoapHeaderRequestReader.class);

    private final static Pattern securityTokenPattern = Pattern.compile("<\\s*" + MebTokenConstants.TOKENNAMESPACEPREFIX + ":" + MebTokenConstants.TOKENNAME
            + "\\s+xmlns:" + MebTokenConstants.TOKENNAMESPACEPREFIX + "\\s*=\\s*\"" + MebTokenConstants.TOKENNAMESPACEURI + "\"\\s*>\\s*(.*?)\\s*<\\s*/\\s*"
            + MebTokenConstants.TOKENNAMESPACEPREFIX + ":" + MebTokenConstants.TOKENNAME + "\\s*>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    public MebSoapHeaderRequestReader(HttpServletRequest request) {
        super(request);
    }

    public String getPrincipal() {
        String principal = null;

        try {
            String secretHeader = getSecurityToken();
            principal = MebSecurityTokenCrypter.getPrincipal(secretHeader);
        } catch (MebSoapHeaderException e) {
            LOG.error("Could not determine security token due to an soap problem", e);
        }

        return principal;
    }

    public String getEmail() {
        String email = null;
        try {
            String secretHeader = getSecurityToken();
            email = MebSecurityTokenCrypter.getEmail(secretHeader);
        } catch (MebSoapHeaderException e) {
            LOG.error("Could not determine security token due to an soap problem", e);
        }
        return email;
    }

    public List<GrantedAuthority> getUserRoles() {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        try {
            String secretHeader = getSecurityToken();
            authorities = MebSecurityTokenCrypter.getUserRoles(secretHeader);
        } catch (MebSoapHeaderException e) {
            LOG.error("Could not determine security token due to an soap problem", e);
        }
        return authorities;
    }

    /**
     * get meb security token from soap header
     * 
     * @return
     * 
     * @throws MebSoapHeaderException
     */
    private String getSecurityToken() throws MebSoapHeaderException {
        String secretHeader = null;
        InputStream is;
        try {
            is = getInputStream();

            // only for debugging purpose of BIT OSB configuration 
            // logInputStream(is);

            if (is.markSupported()) {
                secretHeader = retrieveSecurityToken(is, 1024, 512);

                if (secretHeader == null) {
                    secretHeader = retrieveSecurityToken(is, 65536, 2048);
                }
            }
        } catch (IOException e) {
            throw new MebSoapHeaderException(e);
        }

        if (secretHeader == null) {
            throw new MebSoapHeaderException("Couldn't determine security token header");
        }

        return secretHeader;
    }

    private String retrieveSecurityToken(InputStream is, int readLimit, int bufferSize) throws IOException {
        String secretHeader = null;

        try {
            is.mark(readLimit);

            StringBuilder out = new StringBuilder();
            byte[] b = new byte[bufferSize];
            int readBytes = 0;
            while ((readBytes + bufferSize) <= readLimit) {
                int n = is.read(b);
                if (n == -1) {
                    break;
                } else {
                    readBytes += n;
                    out.append(new String(b, 0, n));

                    Matcher match = securityTokenPattern.matcher(out);
                    if (match.find()) {
                        secretHeader = match.group(1);
                        break;
                    }
                }
            }
        } finally {
            is.reset();
        }

        return secretHeader;
    }

    @SuppressWarnings({ "unchecked", "unused" })
    private void logInputStream(InputStream is) {
        String isInfo = this.getRequestURI() + ": ";

        try {
            ServletInputStream sis = (ServletInputStream) is;
            Object PostIs;
            // get the reflected object
            Field field = sis.getClass().getDeclaredField("in");
            field.setAccessible(true);
            Object unknownIs = field.get(sis);

            if (unknownIs instanceof HttpChunkInputStream) {
                isInfo += "ChunkInputStream ";
                FilterInputStream fis = (FilterInputStream) unknownIs;
                isInfo += "(" + String.valueOf(fis.available()) + "): ";
                Class<FilterInputStream> fisClass = (Class<FilterInputStream>) fis.getClass().getGenericSuperclass();
                field = fisClass.getDeclaredField("in");
                field.setAccessible(true);
                PostIs = field.get(fis);
            } else if (unknownIs.getClass().getName().contains("ServletInputStreamImpl")) {
                isInfo += "ServletInputStreamImpl ";
                isInfo += "(" + String.valueOf(sis.available()) + "): ";
                field = unknownIs.getClass().getDeclaredField("this$0");
                field.setAccessible(true);
                Object servletIsImpl = field.get(unknownIs);
                field = servletIsImpl.getClass().getDeclaredField("in");
                field.setAccessible(true);
                servletIsImpl = field.get(servletIsImpl);
                field = servletIsImpl.getClass().getDeclaredField("in");
                field.setAccessible(true);
                PostIs = field.get(servletIsImpl);
            } else {
                isInfo += "PostInputStream ";
                isInfo += "(" + String.valueOf(sis.available()) + "): ";
                PostIs = unknownIs;
            }

            // get the socket input stream
            field = PostIs.getClass().getDeclaredField("ms");
            field.setAccessible(true);
            Object ms = field.get(PostIs);

            isInfo += ms.toString();
        } catch (Exception e) {
            // do nothing
        }

        LOG.info(isInfo);
    }
}