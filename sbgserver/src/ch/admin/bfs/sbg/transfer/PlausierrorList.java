/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: PlausierrorList.java 53 2007-06-01 09:00:31Z dzw $
 *
 * ------------------------------------------------------------------------- */

package ch.admin.bfs.sbg.transfer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * TODO Describe this class
 * 
 * @author $Author: dzw $
 * @version $Revision: 53 $
 */
public class PlausierrorList {
    private List<Plausierror> _plausierror = null;

    public PlausierrorList() {}

    public PlausierrorList(List<Plausierror> errors) {
        _plausierror = errors;
    }

    public PlausierrorList(Set<Plausierror> errorSet) {
        _plausierror = new ArrayList<Plausierror>(errorSet);
    }

    public List<Plausierror> getPlausierror() {
        if (_plausierror == null) {
            _plausierror = new ArrayList<Plausierror>();
        }
        return _plausierror;
    }

    public void setPlausierror(List<Plausierror> errors) {
        _plausierror = errors;
    }
}
