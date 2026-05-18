package ch.admin.bfs.sbg.transfer;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import ch.bfs.meb.sbg.server.integration.dto.SbgParameter;
import lombok.Setter;

/**
 * <p/>
 * Java class for MacroParameterList complex type.
 * <p/>
 * <p/>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p/>
 * <pre>
 * &lt;complexType name="MacroParameterList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Parameter" type="{http://macro.transfer.sbg.bfs.admin.ch}Parameter" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */

public class MacroParameterList extends ResultBase {
    private static final long serialVersionUID = -3531167330913478869L;

    @Setter
    private List<SbgParameter> macroParameter = new ArrayList<SbgParameter>();

    public MacroParameterList() {}

    public MacroParameterList(List<SbgParameter> params) {
        macroParameter = params;
    }

    public MacroParameterList(SortedSet<SbgParameter> params) {
        macroParameter = new ArrayList<SbgParameter>();
        for (SbgParameter param : params) {
            macroParameter.add(param);
        }
    }

    public List<SbgParameter> getMacroParameter() {
        if (macroParameter == null) {
            macroParameter = new ArrayList<SbgParameter>();
        }
        return macroParameter;
    }
}
