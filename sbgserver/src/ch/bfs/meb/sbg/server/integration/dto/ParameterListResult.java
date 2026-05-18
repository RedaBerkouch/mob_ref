/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: FilterList.java 36 2007-05-29 09:45:22Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.server.integration.dto;

import java.util.ArrayList;
import java.util.List;

import ch.admin.bfs.sbg.transfer.ResultBase;
import lombok.Getter;
import lombok.Setter;

/**
 * List of {@link ch.bfs.meb.sbg.server.integration.dto.SbgParameter}s.
 *
 * @author $Author: dzw $
 * @version $Revision: 36 $
 */
@Getter
@Setter
public class ParameterListResult extends ResultBase {
    private static final long serialVersionUID = -1869187257512891165L;

    List<SbgParameter> parameters = new ArrayList<SbgParameter>();

    public ParameterListResult() {}

    public ParameterListResult(List<SbgParameter> parameters) {
        this.parameters = parameters;
        setState(OK);
    }

    public ParameterListResult(String message) {
        setParameters(null);
        setMessage(message);
        setState(FAILURE);
    }
}
