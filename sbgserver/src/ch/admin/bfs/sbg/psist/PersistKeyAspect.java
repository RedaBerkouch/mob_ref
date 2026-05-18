package ch.admin.bfs.sbg.psist;

import ch.admin.bfs.sbg.transfer.KeyAspect;

public class PersistKeyAspect extends KeyAspect implements java.io.Serializable {
    private static final long serialVersionUID = -1907395911429647170L;

    /** default constructor */
    public PersistKeyAspect() {}

    /** copy from transient event */
    public PersistKeyAspect(KeyAspect transKeyAspect) {
        setKeyAspectId(transKeyAspect.getKeyAspectId());
        setKeyAspectCode(transKeyAspect.getKeyAspectCode());
        setSbfiCode(transKeyAspect.getSbfiCode());
        setText_de(transKeyAspect.getText_de());
        setText_fr(transKeyAspect.getText_fr());
        setValidFromYear(transKeyAspect.getValidFromYear());
        setValidToYear(transKeyAspect.getValidToYear());
    }
}
