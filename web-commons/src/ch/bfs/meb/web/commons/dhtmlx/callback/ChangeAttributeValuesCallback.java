/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.i18n.ILocalizedCode;

/**
 * Base class for callbacks in where filter tables
 * 
 * @author $Author$
 * @version $Revision$
 */
public abstract class ChangeAttributeValuesCallback extends CallbackBase {
    protected final IDhtmlxManager _target;
    protected final String _attrCol;
    protected final String _valueCol;
    protected final String[] _operators;
    protected String _codeGroup = CodegroupUtility.MEB_PLAUSISTATUS;

    /**
     * Creates a new callback
     * 
     * @param methodName
     *            javascript method name
     * @param manager
     *            calling manager
     * @param target
     *            name of related table manager (target for where filters)
     * @param attributeColumnId
     *            id of attribute column
     * @param valueColumnId
     *            id of value column
     */
    public ChangeAttributeValuesCallback(String methodName, IDhtmlxManager manager, IDhtmlxManager target, String attributeColumnId, String valueColumnId,
            String[] operators) {
        super(methodName, manager);

        _operators = operators;

        _target = target;
        _attrCol = attributeColumnId;
        _valueCol = valueColumnId;
    }

    public void setCodeGroup(String codeGroup) {
        _codeGroup = codeGroup;
    }

    protected String createPlausistatusCombo() {
        StringBuilder code = new StringBuilder();

        code.append("var combo=new dhtmlXGridComboObject();");
        for (ILocalizedCode plausistatus : getManager().getLocalizationManager().getCodeGroupAllValues(_codeGroup, true)) {
            code.append("combo.put(" + plausistatus.getKey() + ",'" + plausistatus.getValue() + " (" + plausistatus.getKey() + ")');");
        }

        return new String(code);
    }

    protected String createOperatorsCombo(boolean forCombo, boolean forCode) {
        StringBuilder code = new StringBuilder();

        int index = 0;
        code.append("var combo=new dhtmlXGridComboObject();");
        for (String operatorOption : _operators) {
            if (forCombo && (operatorOption.equals("=") || operatorOption.equals("<>")) || !forCombo && forCode && !operatorOption.equals("LIKE")
                    || !forCombo && !forCode) {
                code.append("combo.put(" + String.valueOf(index) + ",'" + operatorOption + "');");
            }
            index++;
        }

        return new String(code);
    }
}
