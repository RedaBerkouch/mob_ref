package ch.bfs.meb.sdl.server.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.xmlbeans.XmlOptions;
import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.exception.MebUncheckedException;
import ch.bfs.meb.exception.MebUncheckedNotMonitoredException;
import ch.bfs.meb.sdl.server.business.DeliveryBO;
import ch.bfs.meb.sdl.server.integration.dto.*;
import ch.bfs.meb.sdl.server.integration.repository.*;
import ch.bfs.meb.sdl.server.service.xmlbeans.MEBResponseDocument;
import ch.bfs.meb.sdl.server.service.xmlbeans.MEBResponseDocument.MEBResponse;
import ch.bfs.meb.sdl.server.service.xmlbeans.MEBResponseDocument.MEBResponse.ValidationErrors;
import ch.bfs.meb.sdl.server.service.xmlbeans.ResponseType;
import ch.bfs.meb.sdl.server.service.xmlbeans.TableDocument;
import ch.bfs.meb.sdl.server.service.xmlbeans.ValidationErrorType;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.security.idm.User;
import ch.bfs.meb.server.commons.business.BOBase;
import ch.bfs.meb.server.commons.integration.dto.*;
import ch.bfs.meb.server.commons.service.impl.ExcelFactoryBase;
import ch.bfs.meb.server.commons.service.impl.ExportServiceProviderBase;
import ch.bfs.meb.server.commons.service.impl.ExportUser;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.MebUtils;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.util.StringUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExportServiceProvider extends ExportServiceProviderBase {

    @Setter
    private IExportRepository exportRepository;
    @Setter
    private IDeliveryRepository deliveryRepository;
    @Setter
    private IBurSchoolRepository burSchoolRepository;
    @Setter
    private ISchoolRepository schoolRepository;
    @Setter
    private IClassRepository classRepository;
    @Setter
    private ILearnerRepository learnerRepository;
    @Setter
    private IConfigDeliveryRepository configDeliveryRepository;
    @Setter
    private IPlausiErrorRepository plausiErrorRepository;
    @Setter
    private IPlausiRepository plausiRepository;

    @Override
    public Export getExportById(Long exportId) {
        return exportRepository.getExportById(exportId);
    }

    @Override
    public List<Export> getExports() {
        return Collections.unmodifiableList(exportRepository.getExports());
    }

    @Override
    public List<Export> getActiveExports() {
        return Collections.unmodifiableList(exportRepository.getActiveExports());
    }

    /**
     * Exports the delivery given by parameter canton and version to the XML
     * delivery format
     * 
     * @param export
     *            containing the parameters canton and version
     * @return XML file containing the delivery
     */
    @Override
    public FileResult runXmlExport(Export export, String locale) {
        String deliveryCode = null;
        Long version = null;
        Long canton = null;

        for (Parameter p : export.getParameters()) {
            if (p.getUniqueName().equals("deliveryCode")) {
                deliveryCode = p.getDefaultValue();
            } else if (p.getUniqueName().equals("version")) {
                version = BOBase.verifyLong(p.getDefaultValue());
            } else if (p.getUniqueName().equals("canton")) {
                canton = BOBase.verifyLong(p.getDefaultValue());
            }
        }
        if (StringUtils.isEmpty(deliveryCode) || version == null || canton == null) {
            // Parameter error
            return new FileResult("export.xml.parameter.error.message");
        }

        SdlDelivery delivery = evaluateParameters(version, canton, deliveryCode);

        // Load all business objects and build xml
        DeliveryBO deliveryBO = new DeliveryBO(delivery, true, schoolRepository, classRepository, learnerRepository);
        TableDocument xmlRoot = TableDocument.Factory.newInstance();
        deliveryBO.asXml(xmlRoot);

        XmlOptions options = new XmlOptions();
        options.setCharacterEncoding("windows-1252");
        options.setSavePrettyPrint();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(os);
        try {
            zipOut.putNextEntry(new ZipEntry("Export.xml"));
            xmlRoot.save(zipOut, options);
            zipOut.closeEntry();
            zipOut.close();
        } catch (IOException e) {
            throw new MebUncheckedException("export.result.creationerror.message", e);
        }

        return new FileResult(os.toByteArray(), "Export-xml.zip");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * ch.bfs.meb.server.commons.service.impl.IExportServiceProvider#runUsersExport
     * (ch.bfs.meb.server.commons.integration.dto.Export, java.lang.String)
     */
    @Override
    public FileResult runUsersExport(Export export, String locale) {
        byte[] userExport;
        ExportUser exportUser;

        Long version = null;
        for (Parameter p : export.getParameters()) {
            if (p.getUniqueName().equals("version")) {
                version = BOBase.verifyLong(p.getDefaultValue());
            }
        }
        if (version == null) {
            throw new MebUncheckedNotMonitoredException("export.version.parameter.error.message");
        }

        // Get all sdl users
        List<User> roUsers = idmService.getUsersForRole(SecurityConstants.ROLE_SDL_RO);
        List<User> dlUsers = idmService.getUsersForRole(SecurityConstants.ROLE_SDL_DL);
        List<User> dvUsers = idmService.getUsersForRole(SecurityConstants.ROLE_SDL_DV);

        // Get all data
        List<SdlDelivery> deliveries = deliveryRepository.getDeliveries(-1, -1, new SortContext(), new FilterContext(), version, -1L);
        List<SdlConfigDelivery> configDeliveries = configDeliveryRepository.getConfigDeliveriesByVersion(version);

        List<ExportUser> dvExportUsers = new ArrayList<>();
        List<ExportUser> dlExportUsers = new ArrayList<>();
        List<ExportUser> roExportUsers = new ArrayList<>();

        // Build export dv users
        for (User dv : dvUsers) {
            if (dv.getCantons() == null || dv.getCantons().isEmpty()) {
                if (dv.isActive()) {
                    exportUser = new ExportUser(dv, null, SecurityConstants.ROLE_DV);
                    setMinDeliveryStatus(exportUser, deliveries);
                    dvExportUsers.add(exportUser);
                }
            } else {
                String locality = dv.getCantonsAsString();
                List<Long> cantons = StringUtils.getCodesAsList(locality);
                for (Long canton : cantons) {
                    if (dv.isActive()) {
                        exportUser = new ExportUser(dv, canton, SecurityConstants.ROLE_DV);
                        setMinDeliveryStatus(exportUser, deliveries);
                        dvExportUsers.add(exportUser);
                    }
                }
            }
        }

        // Build export dl users
        for (User dl : dlUsers) {
            if (dl.getCantons() == null || dl.getCantons().isEmpty()) {
                exportUser = new ExportUser(dl, null, SecurityConstants.ROLE_DL);
                setMinDeliveryStatus(exportUser, deliveries);
                dlExportUsers.add(exportUser);
            } else {
                String locality = dl.getCantonsAsString();
                List<Long> cantons = StringUtils.getCodesAsList(locality);
                for (Long canton : cantons) {
                    if (dl.isActive()) {
                        exportUser = new ExportUser(dl, canton, SecurityConstants.ROLE_DL);
                        List<String> configDeliveryCodes = getConfigDeliveryCodesForDL(configDeliveries, exportUser);
                        exportUser.setDeliveries(configDeliveryCodes);
                        setMinDeliveryStatus(exportUser, deliveries);
                        dlExportUsers.add(exportUser);
                    }
                }
            }
        }

        // Build export ro users
        for (User ro : roUsers) {
            if (ro.getCantons() == null || ro.getCantons().isEmpty()) {
                exportUser = new ExportUser(ro, null, SecurityConstants.ROLE_RO);
                setMinDeliveryStatus(exportUser, deliveries);
                roExportUsers.add(exportUser);
            } else {
                String locality = ro.getCantonsAsString();
                List<Long> cantons = StringUtils.getCodesAsList(locality);
                for (Long canton : cantons) {
                    if (ro.isActive()) {
                        exportUser = new ExportUser(ro, canton, SecurityConstants.ROLE_RO);
                        List<String> configDeliveryCodes = getConfigDeliveryCodesForRO(configDeliveries, exportUser);
                        exportUser.setDeliveries(configDeliveryCodes);
                        setMinDeliveryStatus(exportUser, deliveries);
                        roExportUsers.add(exportUser);
                    }
                }
            }
        }

        try {
            String applicationTitle = localizationManager.getMessageByLanguage(APPLICATION_TITLE, locale);
            userExport = exportUsersFactory.createUsersExport(applicationTitle, dvExportUsers, dlExportUsers, roExportUsers, locale);
        } catch (IOException e) {
            throw new MebUncheckedException("export.result.creationerror.message", e);
        }
        return new FileResult(userExport, "users.xlsx");
    }

    private List<String> getConfigDeliveryCodesForDL(List<SdlConfigDelivery> configDeliveries, ExportUser dl) {
        List<String> deliveryCodes = new ArrayList<>();
        for (SdlConfigDelivery configDelivery : configDeliveries) {
            if (MebUtils.isUserEmailConfigured(configDelivery.getDl_users(), dl.getUser().getUsername()) && dl.getCanton().equals(configDelivery.getCanton())) {
                deliveryCodes.add(configDelivery.getDeliveryCode());
            }
        }
        return deliveryCodes;
    }

    private List<String> getConfigDeliveryCodesForRO(List<SdlConfigDelivery> configDeliveries, ExportUser ro) {
        List<String> deliveryCodes = new ArrayList<>();
        for (SdlConfigDelivery configDelivery : configDeliveries) {
            if (MebUtils.isUserEmailConfigured(configDelivery.getRo_users(), ro.getUser().getUsername()) && ro.getCanton().equals(configDelivery.getCanton())) {
                deliveryCodes.add(configDelivery.getDeliveryCode());
            }
        }
        return deliveryCodes;
    }

    private void setMinDeliveryStatus(ExportUser user, List<SdlDelivery> deliveries) {
        Long minStatus = null;

        log.debug("setMinDeliveryStatus for user: {}", user);

        if (user.getCanton() != null) {
            for (SdlDelivery delivery : deliveries) {
                log.debug("check delivery {}", ReflectionToStringBuilder.toString(delivery));
                if (delivery.getCanton().equals(user.getCanton()) && user.getUser().getUsername().equals(delivery.getCreation_user())
                        && (minStatus == null || delivery.getDeliveryStatus() < minStatus)) {
                    minStatus = delivery.getDeliveryStatus();
                }
            }
        }

        user.setMinDeliveryStatus(minStatus != null ? minStatus : CodegroupUtility.MEB_DELIVERYSTATUS_INITIALIZED);
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.server.commons.service.impl.IExportServiceProvider#
     * runInitStatusExport(ch.bfs.meb.server.commons.integration.dto.Export,
     * java.lang.String)
     */
    @Override
    public FileResult runInitStatusExport(Export export, String locale) {
        byte[] initStatusExport;

        Long version = null;
        for (Parameter p : export.getParameters()) {
            if (p.getUniqueName().equals("version")) {
                version = BOBase.verifyLong(p.getDefaultValue());
            }
        }
        if (version == null) {
            throw new MebUncheckedNotMonitoredException("export.version.parameter.error.message");
        }

        // Get all not configured schools
        List<SdlBurSchool> sdlSchools = burSchoolRepository.getNotConfiguredSchoolsForVersion(version);
        List<BurSchool> schools = new ArrayList<>();
        schools.addAll(sdlSchools);

        // Get all sdl users (DL and RO)
        HashMap<String, ExportUser> users = new HashMap<>();
        List<User> eaUsers = idmService.getUsersForRole(SecurityConstants.ROLE_SDL_EA);
        addAll(users, eaUsers, SecurityConstants.ROLE_EA);
        List<User> evUsers = idmService.getUsersForRole(SecurityConstants.ROLE_SDL_EV);
        addAll(users, evUsers, SecurityConstants.ROLE_EV);
        List<User> dvUsers = idmService.getUsersForRole(SecurityConstants.ROLE_SDL_DV);
        addAll(users, dvUsers, SecurityConstants.ROLE_DV);
        List<User> dlUsers = idmService.getUsersForRole(SecurityConstants.ROLE_SDL_DL);
        addAll(users, dlUsers, SecurityConstants.ROLE_DL);
        List<User> roUsers = idmService.getUsersForRole(SecurityConstants.ROLE_SDL_RO);
        addAll(users, roUsers, SecurityConstants.ROLE_RO);

        List<SdlConfigDelivery> sdlConfigDeliveries = configDeliveryRepository.getConfigDeliveriesByVersion(version);
        List<ConfigDelivery> configDeliveries = new ArrayList<>();
        configDeliveries.addAll(sdlConfigDeliveries);

        try {
            String applicationTitle = localizationManager.getMessageByLanguage(APPLICATION_TITLE, locale);
            initStatusExport = exportInitStatusFactory.createInitStatusExport(applicationTitle, version, schools, configDeliveries, users, localizationManager,
                    locale);
        } catch (IOException e) {
            throw new MebUncheckedException("export.result.creationerror.message", e);
        }
        return new FileResult(initStatusExport, "initStatus.xlsx");
    }

    /**
     * Exports an xml plausi report for the delivery given by parameter canton,
     * version and deliveryCode
     * 
     * @param export
     *            containing the parameters canton, version and deliveryCode
     * @param locale
     *            language
     * @return XML plausireport
     */
    @Override
    public FileResult runXmlDeliveryPlausireportExport(Export export, String locale) {
        String deliveryCode = null;
        Long version = null;
        Long canton = null;

        for (Parameter p : export.getParameters()) {
            if (p.getUniqueName().equals("deliveryCode")) {
                deliveryCode = p.getDefaultValue();
            } else if (p.getUniqueName().equals("version")) {
                version = BOBase.verifyLong(p.getDefaultValue());
            } else if (p.getUniqueName().equals("canton")) {
                canton = BOBase.verifyLong(p.getDefaultValue());
            }
        }
        if (StringUtils.isEmpty(deliveryCode) || version == null || canton == null) {
            // Parameter error
            return new FileResult("export.xml.parameter.error.message");
        }

        SdlDelivery delivery = evaluateParameters(version, canton, deliveryCode);

        // Load the business objects
        // Get all plausi data from database and build list and HashMap of
        // relevant plausis
        List<SdlPlausi> allPlausiList = plausiRepository.getPlausis();
        List<SdlPlausi> plausiList = new ArrayList<>();
        for (SdlPlausi plausi : allPlausiList) {
            if (plausi.getObjectLevel() >= CodegroupUtility.SDL_OBJECTTYPE_DELIVERY && plausi.getIsActive()) {
                plausiList.add(plausi);
            }
        }

        // Get all plausi errors
        List<SdlPlausiError> plausiErrors = plausiErrorRepository.getPlausiErrorsForDelivery(delivery.getDeliveryId());
        Collections.sort(plausiErrors, (error1, error2) -> {
            SdlPlausi plausi1 = error1.getPlausi();
            SdlPlausi plausi2 = error2.getPlausi();
            long order1 = (plausi1 != null && plausi1.getPlausiOrder() == null) ? Long.MAX_VALUE : plausi1.getPlausiOrder();
            long order2 = (plausi2 != null && plausi2.getPlausiOrder() == null) ? Long.MAX_VALUE : plausi2.getPlausiOrder();
            return order1 < order2 ? -1 : order1 > order2 ? 1 : 0;
        });

        // build the xml document
        MEBResponseDocument mebResponseDoc = buildXMLDeliveryPlausireportDocument(delivery, plausiErrors, locale);

        // Build XML File
        XmlOptions options = new XmlOptions();
        options.setCharacterEncoding("utf-8");
        options.setSavePrettyPrint();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(os);
        try {
            zipOut.putNextEntry(new ZipEntry("Plausireport.xml"));
            mebResponseDoc.save(zipOut, options);
            zipOut.closeEntry();
            zipOut.close();
        } catch (IOException e) {
            throw new MebUncheckedException("export.result.creationerror.message", e);
        }

        return new FileResult(os.toByteArray(), "Plausireport-xml.zip");
    }

    private SdlDelivery evaluateParameters(Long version, Long canton, String deliveryCode) {
        SdlDelivery delivery = deliveryRepository.getDeliveryByIdentification(canton, version, deliveryCode);
        if (delivery == null) {
            // No delivery error
            throw new MebUncheckedNotMonitoredException("export.xml.delivery.error.message");
        }

        // Check authentication
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SDL_EV)) {
            if (user.isInRole(SecurityConstants.ROLE_SDL_DV)) {
                if (!user.getCantons().contains(canton)) {
                    // no access permission for given canton
                    throw new MebUncheckedNotMonitoredException("export.xml.nocantonpermission.error.message");
                }
            } else {
                SdlConfigDelivery configDelivery = configDeliveryRepository.getConfigDeliveryByCodeVersionAndCanton(delivery.getConfigDeliveryCode(), version,
                        canton);
                if (configDelivery == null) {
                    // no configuration found
                    throw new MebUncheckedNotMonitoredException("export.xml.configdeliverymissing.error.message");
                }

                if (user.isInRole(SecurityConstants.ROLE_SDL_DL)) {
                    if (!MebUtils.isUserEmailConfigured(configDelivery.getDl_users(), user.getEmail())
                            && !user.getEmail().toLowerCase().equals(delivery.getCreation_user().toLowerCase())) {
                        // no required role or specific permission found
                        throw new MebUncheckedNotMonitoredException("export.xml.nodluser.error.message");
                    }
                } else if (user.isInRole(SecurityConstants.ROLE_SDL_RO)) {
                    if (!MebUtils.isUserEmailConfigured(configDelivery.getRo_users(), user.getEmail())) {
                        // no required role or specific permission found
                        throw new MebUncheckedNotMonitoredException("export.xml.norouser.error.message");
                    }
                } else {
                    // no permission at all
                    throw new MebUncheckedNotMonitoredException("no.authorization.message");
                }
            }
        }
        return delivery;
    }

    private MEBResponseDocument buildXMLDeliveryPlausireportDocument(SdlDelivery delivery, List<SdlPlausiError> plausiErrors, String locale) {
        MEBResponseDocument mebResponseDoc = MEBResponseDocument.Factory.newInstance();

        // Get object types
        List<CodeGroup> objectTypes = codegroupManager.getCodeGroupsByGroupIdAndLanguage(CodegroupUtility.SDL_OBJECTTYPE, locale);

        // build xml document
        MEBResponse response = mebResponseDoc.addNewMEBResponse();

        MEBResponse.Head head = response.addNewHead();
        head.setVersion(delivery.getVersion().intValue());
        head.setCantonId(delivery.getCanton().intValue());
        head.setDataDelivery(delivery.getDeliveryCode());
        // head.setLidat(_thisDelivery.getDeliveryDate() != null ? dateToString
        // (_thisDelivery.getDeliverydate()) : "");

        ResponseType responseType = response.addNewResponse();
        responseType.setMessage("");
        if (plausiErrors.size() == 0) {
            responseType.setCode(MEB_RESPONSE_NO_PLAUSIERRORS);
            return mebResponseDoc;
        } else {
            responseType.setCode(MEB_RESPONSE_WITH_PLAUSIERRORS);
        }

        ValidationErrors validationErrors = response.addNewValidationErrors();
        for (SdlPlausiError plausiError : plausiErrors) {
            ValidationErrorType validationErrorType = validationErrors.addNewValidationError();
            validationErrorType.setRuleId("");
            if (Locale.GERMAN.getLanguage().equals(locale)) {
                validationErrorType.setRuleName(plausiError.getPlausi().getName_de());
                validationErrorType.setErrorMessage(plausiError.getErrorMsg_de());
            } else if (Locale.FRENCH.getLanguage().equals(locale)) {
                validationErrorType.setRuleName(plausiError.getPlausi().getName_fr());
                validationErrorType.setErrorMessage(plausiError.getErrorMsg_fr());
            } else if (Locale.ITALIAN.getLanguage().equals(locale)) {
                validationErrorType.setRuleName(plausiError.getPlausi().getName_it());
                validationErrorType.setErrorMessage(plausiError.getErrorMsg_it());
            }
            long objectLevel = CodegroupUtility.SDL_OBJECTTYPE_CANTON;
            if (plausiError.getLearnerId() != null) {
                objectLevel = CodegroupUtility.SDL_OBJECTTYPE_LEARNER;
            } else if (plausiError.getClassId() != null) {
                objectLevel = CodegroupUtility.SDL_OBJECTTYPE_CLASS;
            } else if (plausiError.getSchoolId() != null) {
                objectLevel = CodegroupUtility.SDL_OBJECTTYPE_SCHOOL;
            } else if (plausiError.getDeliveryId() != null) {
                objectLevel = CodegroupUtility.SDL_OBJECTTYPE_DELIVERY;
            }
            validationErrorType.setObjectType(ExcelFactoryBase.getCodeText(objectTypes, objectLevel));
            if (plausiError.getLearnerOrigDeliveryData() != null) {
                validationErrorType.setOriginText(plausiError.getLearnerOrigDeliveryData());
            }

            if (plausiError.getDeliveredSchoolIdType() != null) {
                validationErrorType.setInstIdCategory(plausiError.getDeliveredSchoolIdType());
            }
            if (plausiError.getDeliveredSchoolId() != null) {
                validationErrorType.setInstId(plausiError.getDeliveredSchoolId());
            }
            if (plausiError.getDeliveredClassId() != null) {
                validationErrorType.setClassId(plausiError.getDeliveredClassId());
            }
            if (plausiError.getDeliveredLearnerIdType() != null) {
                validationErrorType.setPersonIdCategory(plausiError.getDeliveredLearnerIdType());
            }
            if (plausiError.getDeliveredLearnerId() != null) {
                validationErrorType.setPersonId(plausiError.getDeliveredLearnerId());
            }
        }

        return mebResponseDoc;
    }

    @Override
    public List<?> executeGenericQuery(String sqlSource) {
        return exportRepository.executeGenericQuery(sqlSource);
    }

    @Override
    public Export insertExport(Export export) {
        return exportRepository.insertExport(new SdlExport(export));
    }

    @Override
    public Export updateExport(Export export) {
        SdlExport updatedExport = new SdlExport(export);
        updatedExport.setSdlParameters(exportRepository.getParameters(export.getExportId()));
        return exportRepository.updateExport(updatedExport);
    }

    @Override
    public void deleteExport(Export export) {
        exportRepository.deleteExport(new SdlExport(export));
    }
}
