/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id$
 */
package ch.bfs.meb.server.commons.integration.sas;

import java.util.List;

/** 
 * Helper class for building SAS commands.
 * 
 * Call example: 
 * %include '/project/SBG/dev/plausi/plausi_agg_04.sas'; %plausi_agg_04
 * (deliveryid=1,pid=0,eventid=0,macroid=0); run;
 */
public class SASCall {

    private final StringBuffer _code;

    public SASCall(String macroPath, List<SASParameter> params) {
        super();
        _code = new StringBuffer();
        _code.append("%include '");
        _code.append(macroPath);
        _code.append("'; %");
        String macroName = parseMacroName(macroPath);
        _code.append(macroName);
        _code.append(" (");

        // Parameters to append
        boolean isfirstParam = true;
        for (SASParameter param : params) {
            addSasParamCode(param, isfirstParam);
            isfirstParam = false;
        }

        _code.append("); run;");
    }

    public String getCode() {
        return _code.toString();
    }

    private String parseMacroName(String path) {
        String macroName = "";

        if (path != null) {
            int startIndex = path.lastIndexOf("/");
            if (startIndex < 0)
                startIndex = 0;
            int endIndex = path.indexOf(".sas");
            if (endIndex < 0)
                throw new RuntimeException("Fatal error while calling plausi macro: Extracting plausi macro name from " + path);

            macroName = path.substring(startIndex + 1, endIndex);
        }
        return macroName;
    }

    private void addSasParamCode(SASParameter param, boolean isFirstParam) {
        if (!isFirstParam)
            _code.append(",");
        _code.append(param.getName());
        _code.append("=");
        _code.append((param.getValue() == null) ? "" : param.getValue());
    }
}
