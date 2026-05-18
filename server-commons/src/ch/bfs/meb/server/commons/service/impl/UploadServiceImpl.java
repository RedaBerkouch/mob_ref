package ch.bfs.meb.server.commons.service.impl;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.activation.DataHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.glaforge.i18n.io.SmartEncodingInputStream;

import ch.bfs.meb.server.commons.integration.dto.UploadResult;

@Service
public class UploadServiceImpl implements IUploadService {
    private final static Logger LOGGER = LoggerFactory.getLogger(UploadServiceImpl.class);

    private static final String UPLOAD_DELIVER_CSV_WRONG_SUFFIX_MESSAGE = "upload.deliverCsvWrongSuffix.message";

    IUploadServiceProvider _uploadServiceProvider;

    public void setUploadServiceProvider(IUploadServiceProvider uploadServiceProvider) {
        _uploadServiceProvider = uploadServiceProvider;
    }

    @Override
    public UploadResult deliver(String dlUser, Long version, DataHandler data, String locale) {
        UploadResult result;
        try {
            File file = File.createTempFile("delivery", null);

            ZipInputStream in = new ZipInputStream(data.getInputStream());
            // Get the first entry
            ZipEntry entry = in.getNextEntry();
            String fileName = entry.getName();
            String upperFileName = fileName.toUpperCase();
            // determine encoding
            SmartEncodingInputStream smartIn = new SmartEncodingInputStream(in, SmartEncodingInputStream.BUFFER_LENGTH_4KB, StandardCharsets.UTF_8);
            boolean changeXmlEncoding = false;
            if (upperFileName.endsWith(".XML") && !smartIn.getEncoding().equals(Charset.forName("UTF-8"))) {
                changeXmlEncoding = true;
            }
            BufferedReader reader = new BufferedReader(smartIn.getReader());
            // Only XMLs in UTF-8 encoding can be parsed correctly on Unix machines
            Writer out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            if (changeXmlEncoding) {
                String firstLine = reader.readLine();
                // replace encoding string
                String pattern = "[Ee][Nn][Cc][Oo][Dd][Ii][Nn][Gg]\\s*=\\s*\"[\\w\\-\\.:]*\"";
                Pattern p = Pattern.compile(pattern);
                Matcher m = p.matcher(firstLine);
                firstLine = m.replaceFirst("encoding=\"UTF-8\"");
                out.write(firstLine);
                out.write(System.getProperty("line.separator"));
            }
            // Read (remaining) data from the ZIP stream
            char[] buf = new char[1024];
            char[] outBuffer = new char[1024];
            int len;
            while ((len = reader.read(buf)) > 0) {
                // remove non-printables
                int outMax = 0;
                for (int i = 0; i < len; i++) {
                    if (buf[i] > 31 || buf[i] > 7 && buf[i] < 14) {
                        outBuffer[outMax++] = buf[i];
                    }
                }
                if (outMax > 0) {
                    out.write(outBuffer, 0, outMax);
                }
            }

            out.close();
            LOGGER.info("file: " + file.getName() + " was transfered");

            if (upperFileName.endsWith(".XML") || upperFileName.endsWith(".CSV")) {
                result = _uploadServiceProvider.deliver(dlUser, version, file, fileName, locale);
            } else {
                result = new UploadResult(UPLOAD_DELIVER_CSV_WRONG_SUFFIX_MESSAGE);
            }
        } catch (IOException e) {
            result = new UploadResult(e.toString());
        }

        return result;
    }
}
