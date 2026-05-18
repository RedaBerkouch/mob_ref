/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: IMacroService.java 364 2007-09-18 13:16:34Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.service;

import ch.bfs.meb.sbg.web.ws.sbgmacro.*;

/**
 * TODO Describe this class
 *
 * @author $Author: dzw $
 * @version $Revision: 364 $
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