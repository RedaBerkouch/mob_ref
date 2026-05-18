package ch.bfs.meb.sba.web.frontend.controller;

import ch.bfs.meb.sba.web.frontend.dto.WizardContextDto;
import ch.bfs.meb.sba.web.frontend.dto.WizardUserContext;
import ch.bfs.meb.sba.web.frontend.manager.UploadManager;
import ch.bfs.meb.sba.web.frontend.manager.WizardDeliveryTableManager;
import ch.bfs.meb.sba.web.service.IWizardService;
import ch.bfs.meb.sba.web.ws.sbawizard.FileResult;
import ch.bfs.meb.sba.web.ws.sbawizard.SbaDeliveryListResult;
import ch.bfs.meb.sba.web.ws.sbawizard.SbaPlausiError;
import ch.bfs.meb.sba.web.ws.sbawizard.SbaPlausiErrorListResult;
import ch.bfs.meb.sba.web.ws.sbawizard.SbaWizardSchool;
import ch.bfs.meb.sba.web.ws.sbawizard.SbaWizardSchoolListResult;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.dhtmlx.DojoIframeIOHTML;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ch.bfs.meb.web.commons.util.FilterContextUtility;
import ch.bfs.meb.web.commons.util.IFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/wizard/delivery")
public class WizardDeliveryController {

    @Autowired
    private IWizardService wizardService;

    @Autowired
    private IFilterService filterService;

    @Autowired
    private IWebLocalizationManager localizationManager;

    @Autowired
    private UploadManager uploadManager;

    @Autowired
    private WizardSessionContext wizardSessionContext;


    /* ===============================
       CONTEXT
       =============================== */

    @GetMapping("/context")
    public WizardContextDto getContext() {

        MebUser user = getCurrentUser();
        Long version = getVersion();

        String dlUser = resolveDlUser(user, version);

        // ===============================
        // SCHOOLS (legacy: getRows())
        // ===============================
        SbaWizardSchoolListResult schools =
                wizardService.getSchools(dlUser, version);

        boolean allDelivered = true;
        int totalQualifications = 0;

        if (schools != null && schools.getSchools() != null) {
            for (SbaWizardSchool school : schools.getSchools()) {
                if (school.getNrOfQualifications() == 0) {
                    allDelivered = false;
                }
                totalQualifications += school.getNrOfQualifications();
            }
        }

        // Legacy: _nrOfColumn.setNrOfPersons(schools.getNrOfPersons())
        long totalPersons = schools != null ? schools.getNrOfPersons() : 0;

        // ===============================
        // ERRORS (legacy: getExtraHtml / hasErrors)
        // ===============================
        SbaPlausiErrorListResult errors =
                wizardService.getErrors(dlUser, version);

        int nrOfConfirmableErrors   = countErrors(errors, true);
        int nrOfNonConfirmableErrors = countErrors(errors, false);
        int nrOfConfirmedErrors     = countConfirmedErrors(errors);

        // ===============================
        // VALIDATION STATE
        // ===============================
        Boolean validated =
                wizardService.areDeliveriesValidated(dlUser, version);

        boolean deliveriesValidated =
                validated == null || validated.booleanValue();

        boolean conflict =
                deliveriesValidated && !allDelivered;   // legacy: _deliveriesValidatedConflict

        // ===============================
        // DTO
        // ===============================
        WizardContextDto dto = new WizardContextDto(
                dlUser,
                version,
                allDelivered,
                deliveriesValidated,
                conflict,
                nrOfConfirmableErrors,
                nrOfNonConfirmableErrors
        );

        dto.setNrOfConfirmedErrors(nrOfConfirmedErrors);
        dto.setTotalPersons(totalPersons);
        dto.setTotalQualifications(totalQualifications);

        // ===============================
        // DL USER COMBO (SBA_DV)
        // ===============================
        if (user.isInRole(SecurityConstants.ROLE_SBA_DV)) {
            dto.setAvailableDlUsers(
                    wizardService.getDlUserNames(version).getUserNames()
            );
        }

        return dto;
    }


    /* ===============================
       SCHOOLS
       =============================== */

    @GetMapping("/schools")
    public List<SbaWizardSchool> getSchools() {

        MebUser user = getCurrentUser();
        Long version = getVersion();
        String dlUser = resolveDlUser(user, version);

        return wizardService
                .getSchools(dlUser, version)
                .getSchools();
    }

    /* ===============================
       DL USER
       =============================== */
    @Autowired
    private WizardDeliveryTableManager wizardDeliveryTableManager;

    @PutMapping("/dl-user")
    public ResponseEntity<Void> changeDlUser(@RequestParam String dlUser) {

        wizardDeliveryTableManager.setDlUser(dlUser);

        return ResponseEntity.ok().build();
    }




    /* ===============================
       FILE UPLOAD
       =============================== */

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            // ✅ 200 obligatoire, erreur dans le body
            return ResponseEntity.ok("Fichier manquant");
        }

        DojoIframeIOHTML result = uploadManager.wizardDelivery(file);

        if (result != null) {
            String html = result.getDocument();

            // Legacy: <textarea id='error'>message</textarea>
            if (html != null && html.contains("id='error'")) {
                String message = extractErrorMessage(html);
                // ✅ 200 obligatoire
                return ResponseEntity.ok(message);
            }
        }

        // ✅ OK : body vide
        return ResponseEntity.ok("");
    }

    private String extractErrorMessage(String html) {

        if (html == null) return "Erreur inconnue";

        int start = html.indexOf(">");
        int end = html.lastIndexOf("<");

        if (start >= 0 && end > start) {
            return html.substring(start + 1, end);
        }

        return "Erreur lors de l'upload";
    }


    /* ===============================
       ACTIONS
       =============================== */

    @DeleteMapping("/deliveries")
    public ResponseEntity<?> deleteDeliveries() {

        MebUser user = getCurrentUser();
        Long version = getVersion();
        String dlUser = resolveDlUser(user, version);

        SbaDeliveryListResult result =
                wizardService.deleteDeliveries(dlUser, version);

        return result.getMessage() == null || result.getMessage().isEmpty()
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().body(result.getMessage());
    }

    @PostMapping("/deliveries/validate")
    public ResponseEntity<?> validateDeliveries() {

        MebUser user = getCurrentUser();
        Long version = getVersion();
        String dlUser = resolveDlUser(user, version);
        SbaDeliveryListResult result =
                wizardService.validateDeliveries(
                        dlUser,
                        version,
                        localizationManager.getLanguage()
                );

        return  ResponseEntity.ok(result.getMessage());
    }

    /* ===============================
       PLAUSI REPORT
       =============================== */

    @GetMapping("/plausi-report")
    public ResponseEntity<Resource> downloadPlausireport() {

        MebUser user = getCurrentUser();
        Long version = getVersion();
        String dlUser = resolveDlUser(user, version);

        FileResult file =
                wizardService.getPlausireport(
                        dlUser,
                        version,
                        localizationManager.getLanguage()
                );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=PlausiReport.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new ByteArrayResource(file.getBinaryFile()));
    }

    @PostMapping("/errors/confirm")
    public void confirmErrors(@RequestBody List<SbaPlausiError> errors) {
        wizardService.confirmErrors(errors);
    }


    /* ===============================
       HELPERS
       =============================== */

    private Long getVersion() {
        return FilterContextUtility.getActVersion(
                filterService,
                CodegroupUtility.SBA_OBJECTTYPE_CONFIGURATION
        );
    }

    private MebUser getCurrentUser() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof MebUser)) {
            throw new IllegalStateException("Invalid authenticated user");
        }

        return (MebUser) auth.getPrincipal();
    }

    private String resolveDlUser(MebUser user, Long version) {

        // 🔥 source unique de vérité
        String dlUser = wizardDeliveryTableManager.getDlUser();
        if (dlUser != null && !dlUser.isEmpty()) {
            return dlUser;
        }

        // fallback sécurité
        if (user.isInRole(SecurityConstants.ROLE_SBA_DV)) {
            List<String> users =
                    wizardService.getDlUserNames(version).getUserNames();
            return users.isEmpty() ? "" : users.get(0);
        }

        return user.getEmail();
    }


    private int countConfirmedErrors(SbaPlausiErrorListResult errors) {

        if (errors == null || errors.getPlausiErrors() == null) return 0;

        int count = 0;
        for (SbaPlausiError e : errors.getPlausiErrors()) {
            if (e.isConfirmable() && e.isIsConfirmed()) {
                count++;
            }
        }
        return count;
    }


    private int countErrors(SbaPlausiErrorListResult errors, boolean confirmable) {

        if (errors == null || errors.getPlausiErrors() == null) return 0;

        int count = 0;
        for (SbaPlausiError e : errors.getPlausiErrors()) {
            if (confirmable) {
                if (e.isConfirmable() && !e.isIsConfirmed()) count++;
            } else {
                if (!e.isConfirmable()) count++;
            }
        }
        return count;
    }

    @GetMapping("/errors/confirmable")
    public List<SbaPlausiError> getConfirmableErrors() {

        MebUser user = getCurrentUser();
        Long version = getVersion();
        String dlUser = resolveDlUser(user, version);

        SbaPlausiErrorListResult result =
                wizardService.getErrors(dlUser, version);

        if (result == null || result.getPlausiErrors() == null) {
            return Collections.emptyList();
        }

        List<SbaPlausiError> confirmable = new ArrayList<>();

        for (SbaPlausiError e : result.getPlausiErrors()) {
            if (e.isConfirmable() && !e.isIsConfirmed()) {
                confirmable.add(e);
            }
        }

        return confirmable;
    }

    /* ===============================
   ALL PLAUSI ERRORS (confirmés + non confirmés)
   =============================== */

    @GetMapping("/errors")
    public List<SbaPlausiError> getAllPlausiErrors() {

        MebUser user = getCurrentUser();
        Long version = getVersion();
        String dlUser = resolveDlUser(user, version);

        SbaPlausiErrorListResult result =
                wizardService.getErrors(dlUser, version);
        if (result == null || result.getPlausiErrors() == null) {
            return Collections.emptyList();
        }
        // 🔥 IMPORTANT :
        // on renvoie TOUTES les erreurs (legacy behavior)
        return result.getPlausiErrors();
    }


}

