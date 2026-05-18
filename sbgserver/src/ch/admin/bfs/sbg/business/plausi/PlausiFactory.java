/* ----------------------------------------------------------------------------
 *
 * SBG-Projekt
 *
 * Copyright (c) 2006 GLANCE AG, Switzerland
 *
 * $Id: PlausiFactory.java 610 2009-12-01 09:20:25Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business.plausi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.admin.bfs.sbg.db.dao.MacroDAO;
import ch.admin.bfs.sbg.db.dao.PlausierrorDAO;
import ch.admin.bfs.sbg.transfer.Macro;
import ch.admin.bfs.sbg.transfer.Plausierror;
import ch.bfs.meb.sbg.server.keyaspect.KeyAspectManager;
import ch.bfs.meb.sbg.server.integration.repository.IEventRepository;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.commons.integration.sas.ISasService;
import lombok.Setter;

/**
 * Factory for plausi rules.
 *
 * @author $Author: lsc $
 * @version $Revision: 610 $
 */
public class PlausiFactory {
    public static final int FORMAT_PLAUSI_NR = 2;

    @Setter
    private IEventRepository eventRepository;
    @Setter
    private ISasService sasService;
    @Setter
    private KeyAspectManager keyAspectManager;

    /**
     * Mantis 1783: Load old confirmed errors for taking over confirmation info in replace/amend use case
     *
     * @param deliveryId
     * @param doLoadConfirmedErrors
     * @return
     */
    public List<PlausiBO> getSimplePlausis(MacroDAO macroDAO, ICodegroupManager codegroupManager, PlausierrorDAO plausierrorDAO, Long deliveryId,
            boolean doLoadConfirmedErrors) {
        List<PlausiBO> internalPlausis = getSimplePlausis(macroDAO, codegroupManager);
        if (!doLoadConfirmedErrors) {
            return internalPlausis;
        }
        // get all old internal confirmed errors for eventual taking over confirm information in replace/amend use case
        HashMap<String, Plausierror> confirmedInternalErrorMap = new HashMap<String, Plausierror>();
        List<Plausierror> confirmedInternalErrors = plausierrorDAO.findConfirmedInternalErrors(deliveryId);
        for (Plausierror error : confirmedInternalErrors) {
            confirmedInternalErrorMap.put(error.getLogicalKey(), error);
        }
        // store confirmed errors map for confirmable internal plausis
        for (PlausiBO plausi : internalPlausis) {
            if (plausi instanceof InternalPlausiBO) {
                ((InternalPlausiBO) plausi).setConfirmablePlausiErrorMap(confirmedInternalErrorMap);
            }
        }
        return internalPlausis;
    }

    public List<PlausiBO> getSimplePlausis(MacroDAO macroDAO, ICodegroupManager codegroupManager) {
        List<Macro> plausiList = macroDAO.findByType(Macro.MACRO_SIMPLEPLAUSI);

        ArrayList<PlausiBO> simplePlausis = new ArrayList<PlausiBO>();
        for (Macro plausi : plausiList) {
            Long active = new Long(1);
            if (plausi.getIsactive().equals(active)) {
                int plausiNr = Integer.valueOf(plausi.getSource().substring(0, 2).trim());
                switch (plausiNr) {
                case 1:
                    simplePlausis.add(new SimplePlausi1BO(plausi, keyAspectManager));
                    break;
                case 2:
                    simplePlausis.add(new SimplePlausi2BO(plausi));
                    break;
                case 3:
                    simplePlausis.add(new SimplePlausi3BO(plausi, codegroupManager));
                    break;
                case 4:
                    simplePlausis.add(new SimplePlausi4BO(plausi));
                    break;
                case 5:
                    simplePlausis.add(new SimplePlausi5BO(plausi));
                    break;
                case 6:
                    simplePlausis.add(new SimplePlausi6BO(plausi));
                    break;
                case 7:
                    simplePlausis.add(new SimplePlausi7BO(plausi));
                    break;
                case 8:
                    simplePlausis.add(new SimplePlausi8BO(plausi));
                    break;
                case 9:
                    simplePlausis.add(new SimplePlausi9BO(plausi));
                    break;
                case 10:
                    simplePlausis.add(new SimplePlausi10BO(plausi));
                    break;
                case 11:
                    simplePlausis.add(new SimplePlausi11BO(plausi));
                    break;
                case 12:
                    simplePlausis.add(new SimplePlausi12BO(plausi));
                    break;
                case 13:
                    simplePlausis.add(new SimplePlausi13BO(plausi, keyAspectManager));
                    break;
                case 17:
                    simplePlausis.add(new SimplePlausi17BO(plausi, eventRepository));
                    break;
                case 18:
                    simplePlausis.add(new SimplePlausi18BO(plausi));
                    break;
                case 19:
                    simplePlausis.add(new SimplePlausi19BO(plausi));
                    break;
                default:
                    break;
                }
            }
        }

        return simplePlausis;
    }

    public List<PlausiBO> getFormatPlausis(MacroDAO macroDAO) {
        List<Macro> plausiList = macroDAO.findByType(Macro.MACRO_SIMPLEPLAUSI);

        ArrayList<PlausiBO> formatPlausis = new ArrayList<PlausiBO>();
        for (Macro macro : plausiList) {
            int plausiNr = Integer.valueOf(macro.getSource().substring(0, 2).trim());
            Long active = new Long(1);
            if (plausiNr == FORMAT_PLAUSI_NR && macro.getIsactive().equals(active)) {
                formatPlausis.add(new SimplePlausi2BO(macro));
                break;
            }
        }

        return formatPlausis;
    }

    public static List<PlausiBO> getComplexPlausis(MacroDAO macroDAO, PlausierrorDAO plausierrorDAO, ISasService sasService) {
        List<Macro> plausiList = macroDAO.findByType(Macro.MACRO_COMPLEXPLAUSI);

        ArrayList<PlausiBO> complexPlausis = new ArrayList<PlausiBO>();
        for (Macro plausi : plausiList) {
            Long active = new Long(1);
            if (plausi.getIsactive().equals(active)) {
                complexPlausis.add(new ComplexPlausiBO(plausi, plausierrorDAO, sasService));
            }
        }

        return complexPlausis;
    }

    public List<PlausiBO> getComplexPlausisFor(MacroDAO macroDAO, PlausierrorDAO plausierrorDAO, Long objectType, ISasService sasService) {
        List<Macro> plausiList = macroDAO.findByType(Macro.MACRO_COMPLEXPLAUSI);

        ArrayList<PlausiBO> complexPlausis = new ArrayList<PlausiBO>();
        for (Macro plausi : plausiList) {
            Long active = new Long(1);
            if (plausi.getIsactive().equals(active) && plausi.getObjecttype().equals(objectType)) {
                complexPlausis.add(new ComplexPlausiBO(plausi, plausierrorDAO, sasService));
            }
        }

        return complexPlausis;
    }
}
