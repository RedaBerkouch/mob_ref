/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbg-webservice

  $Id: IActionService.java 1162 2010-03-26 12:39:56Z msc $
 */
package ch.admin.bfs.sbg.webservice;

import ch.admin.bfs.sbg.transfer.ExportResult;
import ch.admin.bfs.sbg.transfer.Macro;
import ch.admin.bfs.sbg.transfer.MacroList;
import ch.admin.bfs.sbg.transfer.MacroResult;
import ch.bfs.meb.sbg.server.integration.dto.ParameterListResult;

/**
 * Interface for generic macro services.
 *
 * @author $Author: msc $
 * @version $Revision: 1162 $
 */
public interface IMacroService {
    public MacroList getMacros();

    public MacroList getExportMacros();

    public ExportResult runExport(Macro exportMacro, String locale);

    public ParameterListResult getParameters(Long macroId);

    public MacroResult getMacroById(Long id);

    public MacroResult updateMacro(Macro macro, String locale);

    public MacroResult insertMacro(Macro macro, String locale);

    public MacroResult deleteMacro(Macro macro);
}
