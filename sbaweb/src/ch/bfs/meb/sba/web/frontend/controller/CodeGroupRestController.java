package ch.bfs.meb.sba.web.frontend.controller;

import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.i18n.CodeGroupService;
import ch.bfs.meb.web.commons.i18n.ILocalizedCode;
import ch.bfs.meb.web.commons.i18n.LocalizedCode;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@RestController
@RequestMapping("/codegroups")
public class CodeGroupRestController {

    private static final Long FIRST_CANTON = 1L;
    private static final Long LAST_CANTON = 26L;

    private final CodeGroupService codeGroupService;

    public CodeGroupRestController(CodeGroupService codeGroupService) {
        this.codeGroupService = codeGroupService;
    }

    @GetMapping("/"+ CodegroupUtility.CANTON)
    public List<ILocalizedCode> getCantons(@RequestParam(required = false) Long canton) {
        Locale locale = LocaleContextHolder.getLocale();
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        boolean isPrivileged = user.isInRole(SecurityConstants.ROLE_SBA_EA)
                || user.isInRole(SecurityConstants.ROLE_SBA_EV);
        List<Long> allowedCantons = isPrivileged ? null : user.getCantons();

        Stream<ILocalizedCode> stream = codeGroupService.getAllValues(CodegroupUtility.CANTON, null, true, locale)
                .stream()
                .filter(code -> allowedCantons == null || allowedCantons.contains(code.getKey()));

        if (isPrivileged) {
            stream = Stream.concat(Stream.of(new LocalizedCode(null, "")), stream); // rajout du champ vide pour les EA / EV
        }

        return stream.collect(Collectors.toList());
    }

    @GetMapping("/{codeGroup}")
    public Collection<ILocalizedCode> getAllValues(@PathVariable String codeGroup,
                                                   @RequestParam(required = false) Long canton,
                                                   @RequestParam(required = false) boolean allCantons) {
        Locale locale = LocaleContextHolder.getLocale();
        if (allCantons) {
            return LongStream.rangeClosed(FIRST_CANTON, LAST_CANTON)
                    .boxed()
                    .flatMap(c -> codeGroupService.getAllValues(codeGroup, c, true, locale).stream())
                    .distinct()
                    .collect(Collectors.toList());
        }
        return codeGroupService.getAllValues(codeGroup, canton, true, locale);
    }

    @GetMapping("/{codeGroup}/{id}")
    public String getValueById(@PathVariable String codeGroup,
                               @PathVariable Long id,
                               @RequestParam(required = false) Long canton,
                               @RequestParam(defaultValue = "fr") String lang) {
        Locale locale = new Locale(lang);
        return codeGroupService.getValueById(codeGroup, id, canton, locale);
    }

    @GetMapping("/{codeGroup}/{id}/search-all-cantons")
    public String searchValueInAllCantons(@PathVariable String codeGroup,
                                          @PathVariable Long id,
                                          @RequestParam(defaultValue = "fr") String lang) {
        Locale locale = new Locale(lang);
        return codeGroupService.searchValueInAllCantons(codeGroup, id, locale);
    }

    @PostMapping("/refresh")
    public void refreshCache() {
        codeGroupService.refreshCache();
    }

    @GetMapping("/initialized")
    public boolean isInitialized() {
        return codeGroupService.isInitialized();
    }
}


