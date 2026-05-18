/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb

  $Id: UploadManager.java 985 2010-03-10 09:27:04Z dzw $
 */
package ch.bfs.meb.ssp.web.frontend.manager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import ch.bfs.meb.ssp.web.service.IBurSchoolService;
import ch.bfs.meb.ssp.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.ssp.web.ws.sspburschool.BurSchool;
import ch.bfs.meb.ssp.web.ws.sspburschool.BurSchoolResult;
import ch.bfs.meb.ssp.web.ws.sspupload.UploadResult;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.util.StringUtils;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxManagerBase;
import ch.bfs.meb.web.commons.dhtmlx.DojoIframeIOHTML;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Manager class for upload data
 * 
 * @author $Author: dzw $
 * @version $Revision: 985 $
 */
@Scope(
        value = "session",
        proxyMode = ScopedProxyMode.TARGET_CLASS
)
@Component("uploadManager")
public class UploadManager extends DhtmlxManagerBase {
    private final static Logger LOGGER = LoggerFactory.getLogger(UploadManager.class);

    private static final String UPLOAD_WIZARDDELIVERY_UNCONFIGUREDSCHOOL = "upload.wizarddelivery.unconfiguredschool";
    private static final String UPLOAD_WIZARDDELIVERY_UNKNOWN = "upload.wizarddelivery.unknown";
    private static final String UPLOAD_WIZARDDELIVERY_PLEASECONTACT = "upload.wizarddelivery.pleasecontact";
    private static final String UPLOAD_WIZARDDELIVERY_ONCANTONPERDELIVERY = "upload.wizarddelivery.onecantonperdelivery";

    @Autowired
    private IBurSchoolService _burSchoolService;

    @Autowired
    IWebLocalizationManager _localizationManager;

    @Autowired
    WizardDeliveryTableManager _wizardDeliveryTableManager;

    @Autowired
    WebServiceClientFactory _webServiceClientFactory;

    public static final String MANAGER_NAME = "upload";

    public static final String CONTROL_NAME = MANAGER_NAME + "Manager";

    /**
     * Return the name of the manager
     * 
     * @return the managers name
     */
    public String getName() {
        return MANAGER_NAME;
    }

    /**
     * Return the control name of the manager
     * 
     * @return the managers control name
     */
    public String getControlName() {
        return CONTROL_NAME;
    }

    /**
     * @see ch.bfs.meb.web.commons.dhtmlx.DhtmlxManagerBase#create()
     */
    @Override
    public void create() throws DhtmlxException {
        // At the moment no callbacks are created...
        // And the define call in the jsp has been removed...
    }

    /**
     * @see ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager#getLocalizationManager()
     */
    @Override
    public IWebLocalizationManager getLocalizationManager() {
        return _localizationManager;
    }

    private byte[] ensureUtf8EncodingWithXmlCheck(String fileName, byte[] rawBytes) throws IOException {
        fileName = fileName.toLowerCase(Locale.ROOT);

        if (fileName.endsWith(".xml")) {
            String decoded;
            // 1) BOM
            if (hasUtf8Bom(rawBytes)) {
                decoded = new String(stripUtf8Bom(rawBytes), StandardCharsets.UTF_8);
                if (!isXmlDeclaredEncodingAllowed(decoded)) {
                    throw new IOException("Unsupported declared XML encoding");
                }
                String fixed = normalizeXmlEncodingHeader(decoded);
                return fixed.getBytes(StandardCharsets.UTF_8);
            }

            // 2) UTF-8
            if (looksLikeUtf8(rawBytes)) {
                decoded = new String(rawBytes, StandardCharsets.UTF_8);
                if (!isXmlDeclaredEncodingAllowed(decoded)) {
                    throw new IOException("Unsupported declared XML encoding");
                }
                String fixed = normalizeXmlEncodingHeader(decoded);
                return fixed.getBytes(StandardCharsets.UTF_8);
            }

            // 3) Windows-1252
            if (looksLikeCharset(rawBytes, Charset.forName("windows-1252"))) {
                decoded = new String(rawBytes, Charset.forName("windows-1252"));
                if (!isXmlDeclaredEncodingAllowed(decoded)) {
                    throw new IOException("Unsupported declared XML encoding");
                }
                String fixed = normalizeXmlEncodingHeader(decoded);
                return fixed.getBytes(StandardCharsets.UTF_8);
            }

            // 4) ISO-8859-1
            if (looksLikeCharset(rawBytes, StandardCharsets.ISO_8859_1)) {
                decoded = new String(rawBytes, StandardCharsets.ISO_8859_1);
                if (!isXmlDeclaredEncodingAllowed(decoded)) {
                    throw new IOException("Unsupported declared XML encoding");
                }
                String fixed = normalizeXmlEncodingHeader(decoded);
                return fixed.getBytes(StandardCharsets.UTF_8);
            }

            throw new IOException("Unsupported XML encoding");
        }



        if (fileName.endsWith(".csv")) {
            if (hasUtf8Bom(rawBytes)) {
                return new String(stripUtf8Bom(rawBytes), StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8);
            } else if (looksLikeUtf8(rawBytes)) {
                return new String(rawBytes, StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8);
            } else if (looksLikeCharset(rawBytes, Charset.forName("windows-1252"))) {
                return new String(rawBytes, Charset.forName("windows-1252")).getBytes(StandardCharsets.UTF_8);
            } else if (looksLikeCharset(rawBytes, StandardCharsets.ISO_8859_1)) {
                return new String(rawBytes, StandardCharsets.ISO_8859_1).getBytes(StandardCharsets.UTF_8);
            }
            throw new IOException("Unsupported CSV encoding");
        }

        throw new IOException("Unsupported file type: only XML, CSV, or ZIP allowed");
    }

    /**
     * Vérifie si l'encodage déclaré dans l'en-tête XML est autorisé.
     * Autorisés : UTF-8, WINDOWS-1252, ISO-8859-1 (insensible à la casse).
     * Si pas d'en-tête ou pas d'attribut encoding → considéré comme OK.
     */
    private boolean isXmlDeclaredEncodingAllowed(String content) {
        Matcher matcher = Pattern.compile("(?i)encoding\\s*=\\s*['\"]([^'\"]+)['\"]")
                .matcher(content);
        if (matcher.find()) {
            String declared = matcher.group(1).toLowerCase(Locale.ROOT);
            return declared.equals("utf-8")
                    || declared.equals("windows-1252")
                    || declared.equals("iso-8859-1");
        }
        return true; // pas d'attribut encoding = autorisé
    }


    /** Vérifie la présence d'un BOM UTF-8 */
    private boolean hasUtf8Bom(byte[] bytes) {
        return bytes.length >= 3 &&
                (bytes[0] & 0xFF) == 0xEF &&
                (bytes[1] & 0xFF) == 0xBB &&
                (bytes[2] & 0xFF) == 0xBF;
    }

    /** Supprime le BOM UTF-8 */
    private byte[] stripUtf8Bom(byte[] bytes) {
        if (hasUtf8Bom(bytes)) {
            return Arrays.copyOfRange(bytes, 3, bytes.length);
        }
        return bytes;
    }

    /** Normalise l'en-tête XML pour forcer encoding="UTF-8". */
    private String normalizeXmlEncodingHeader(String content) {
        if (content.matches("(?is)^\\s*<\\?xml[^>]*encoding\\s*=.*\\?>.*")) {
            return content.replaceFirst("(?i)encoding\\s*=\\s*(['\"]).*?\\1", "encoding=\"UTF-8\"");
        }
        if (content.matches("(?is)^\\s*<\\?xml[^>]*\\?>.*")) {
            return content.replaceFirst("\\?>", " encoding=\"UTF-8\"?>");
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + content;
    }

    private boolean looksLikeUtf8(byte[] input) {
        return looksLikeCharset(input, StandardCharsets.UTF_8);
    }

    private boolean looksLikeCharset(byte[] input, Charset charset) {
        try {
            CharsetDecoder decoder = charset.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            decoder.decode(ByteBuffer.wrap(input));
            return true;
        } catch (CharacterCodingException e) {
            return false;
        }
    }



    private byte[] zip(String fileName, byte[] content) throws IOException {
        ByteArrayOutputStream binOut = new ByteArrayOutputStream();
        try (ZipOutputStream zipOut = new ZipOutputStream(binOut)) {
            zipOut.putNextEntry(new ZipEntry(fileName));
            zipOut.write(content);
            zipOut.closeEntry();
        }
        return binOut.toByteArray();
    }

    private byte[] processZipFile(MultipartFile file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream());
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            ZipEntry entry;
            byte[] buffer = new byte[4096];

            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                ByteArrayOutputStream tmpOut = new ByteArrayOutputStream();
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    tmpOut.write(buffer, 0, len);
                }
                byte[] entryBytes = tmpOut.toByteArray();

                if (name.toLowerCase(Locale.ROOT).endsWith(".xml") ||
                        name.toLowerCase(Locale.ROOT).endsWith(".csv")) {
                    // ⚠️ si encodage non supporté → IOException → tout le ZIP rejeté
                    entryBytes = ensureUtf8EncodingWithXmlCheck(name, entryBytes);
                }

                zos.putNextEntry(new ZipEntry(entry.getName()));
                zos.write(entryBytes);
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }


    protected DojoIframeIOHTML doDelivery(MultipartFile file, boolean wizard) {
        long start = System.currentTimeMillis();
        String upperFileName = file.getOriginalFilename().toUpperCase();

        if (!(upperFileName.endsWith(".XML") || upperFileName.endsWith(".CSV") || upperFileName.endsWith(".ZIP"))) {
            return new DojoIframeIOHTML(getLocalizationManager().getMessage("upload.deliverCsvWrongSuffix.message"));
        }

        byte[] data;
        try {
            if (upperFileName.endsWith(".XML") || upperFileName.endsWith(".CSV")) {
                data = ensureUtf8EncodingWithXmlCheck(file.getOriginalFilename(), file.getBytes());
                data = zip(file.getOriginalFilename(), data);
            } else if (upperFileName.endsWith(".ZIP")) {
                data = processZipFile(file);
            } else {
                data = file.getBytes();
            }
        } catch (IOException e) {
            LOGGER.error("Encoding check failed for file {}", file.getOriginalFilename(), e);
            return new DojoIframeIOHTML(
                    getLocalizationManager().getMessage("upload.invalidEncoding.message", new String[]{file.getOriginalFilename()}));
        }

        DataSource ds = new ByteArrayDataSource(data, "application/octet-stream");
        DataHandler dh = new DataHandler(ds);

        UploadResult state;
        if (wizard) {
            state = _webServiceClientFactory.getUploadWebService().uploadWizardDelivery(
                    _wizardDeliveryTableManager.getDlUser(),
                    _wizardDeliveryTableManager.getVersion(),
                    dh,
                    getLocalizationManager().getLanguage().toLowerCase()
            );
        } else {
            state = _webServiceClientFactory.getUploadWebService().uploadDelivery(
                    dh,
                    getLocalizationManager().getLanguage().toLowerCase()
            );
        }

        LOGGER.info("Upload Time: {} ms", (System.currentTimeMillis() - start));

        if (!StringUtils.isEmpty(state.getMessage())) {
            String messageText;
            try {
                messageText = getLocalizationManager().getMessage(state.getMessage(), state.getArgs().toArray());
            } catch (NoSuchMessageException e) {
                messageText = state.getMessage();
            }
            return new DojoIframeIOHTML(messageText);
        }
        return new DojoIframeIOHTML();
    }

        /**
         * Sends the delivery file to the server
         *
         * @param file
         *            file to send
         * @return Upload result
         * @throws DhtmlxException
         */
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public DojoIframeIOHTML delivery(MultipartFile file) {
        return doDelivery(file, false);
    }

    /**
     * Sends the delivery file to the server
     * 
     * @param file
     *            file to send
     * @return Upload result
     * @throws DhtmlxException
     */
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public DojoIframeIOHTML wizardDelivery(MultipartFile file) {
        return doDelivery(file, true);
    }
}
