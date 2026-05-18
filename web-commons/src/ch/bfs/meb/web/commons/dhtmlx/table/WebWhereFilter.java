/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

public class WebWhereFilter {
    protected String _attribute;
    protected String _operator;
    protected String _value;
    protected String _relation;
    protected int _id;

    /**
     * @return Returns the attribute.
     */
    public String getAttribute() {
        return _attribute;
    }

    /**
     * @param attribute
     *            The attribute to set.
     */
    public void setAttribute(String attribute) {
        _attribute = attribute;
    }

    /**
     * @return Returns the operator.
     */
    public String getOperator() {
        return _operator;
    }

    /**
     * @param operator
     *            The operator to set.
     */
    public void setOperator(String operator) {
        _operator = operator;
    }

    /**
     * @return Returns the relation.
     */
    public String getRelation() {
        return _relation;
    }

    /**
     * @param relation
     *            The relation to set.
     */
    public void setRelation(String relation) {
        _relation = relation;
    }

    /**
     * @return Returns the value.
     */
    public String getValue() {
        return _value;
    }

    /**
     * @param value
     *            The value to set.
     */
    public void setValue(String value) {
        _value = value;
    }

    /**
     * @return Returns the id.
     */
    public int getId() {
        return _id;
    }

    /**
     * @param id
     *            The id to set.
     */
    public void setId(int id) {
        _id = id;
    }
}
