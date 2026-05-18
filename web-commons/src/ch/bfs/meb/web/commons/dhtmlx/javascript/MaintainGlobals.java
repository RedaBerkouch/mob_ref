package ch.bfs.meb.web.commons.dhtmlx.javascript;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;

public class MaintainGlobals extends GlobalJavascriptBase {
    public static final String GLOBALS = "maintainglobals";

    public MaintainGlobals() throws DhtmlxException {
        create();
    }

    @Override
    public void create() throws DhtmlxException {
        defineGlobal(JSBoolean.byRef("adminFilterTableManagerIsMaster"), JSBoolean.istrue);
        defineGlobal(JSBoolean.byRef("adminPlausiTableManagerIsMaster"), JSBoolean.istrue);
        defineGlobal(JSBoolean.byRef("adminExportTableManagerIsMaster"), JSBoolean.istrue);
        defineGlobal(JSBoolean.byRef("adminMacroTableManagerIsMaster"), JSBoolean.istrue);
        defineGlobal(JSBoolean.byRef("deliveryTableManagerIsMaster"), JSBoolean.istrue);
        defineGlobal(JSBoolean.byRef("interventionTableManagerIsMaster"), JSBoolean.isfalse);
        defineGlobal(JSBoolean.byRef("actionTableManagerIsMaster"), JSBoolean.isfalse);
        defineGlobal(JSBoolean.byRef("cantonTableManagerIsMaster"), JSBoolean.istrue);
        defineGlobal(JSBoolean.byRef("cantonInterventionTableManagerIsMaster"), JSBoolean.isfalse);
        defineGlobal(JSBoolean.byRef("configDeliveryTableManagerIsMaster"), JSBoolean.istrue);
        defineGlobal(JSBoolean.byRef("burSchoolTableManagerIsMaster"), JSBoolean.isfalse);
        defineGlobal(JSBoolean.byRef("schoolTableManagerIsMaster"), JSBoolean.istrue);
        defineGlobal(JSBoolean.byRef("classTableManagerIsMaster"), JSBoolean.isfalse);
        defineGlobal(JSBoolean.byRef("learnerTableManagerIsMaster"), JSBoolean.isfalse);
        defineGlobal(JSBoolean.byRef("personTableManagerIsMaster"), JSBoolean.istrue);
        defineGlobal(JSBoolean.byRef("activityTableManagerIsMaster"), JSBoolean.isfalse);
        defineGlobal(JSBoolean.byRef("qualificationTableManagerIsMaster"), JSBoolean.isfalse);
        defineGlobal(JSBoolean.byRef("eventTableManagerIsMaster"), JSBoolean.isfalse);
        defineGlobal(JSString.byRef("filterCommand"), new JSString(""));
    }
}
