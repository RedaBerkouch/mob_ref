package ch.admin.bfs.sbg.transfer;

import java.io.Serializable;

public class LocalizedCode implements Serializable, Comparable<LocalizedCode> {

    /**
     * 
     */
    private static final long serialVersionUID = -1774003725621382707L;
    private Long key;
    private String value;

    public LocalizedCode() {

    }

    public LocalizedCode(Long key, String value) {
        this.key = key;
        this.value = value;
    }

    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int compareTo(LocalizedCode o) {

        return this.getKey().compareTo(o.getKey());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LocalizedCode))
            return false;
        return this.compareTo((LocalizedCode) o) == 0;
    }

    @Override
    public int hashCode() {
        return (int) ((getKey().hashCode() | getValue().hashCode()));
    }
}
