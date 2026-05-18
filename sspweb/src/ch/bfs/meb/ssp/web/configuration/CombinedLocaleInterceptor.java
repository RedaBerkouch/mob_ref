package ch.bfs.meb.ssp.web.configuration;

import lombok.Setter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Setter
public class CombinedLocaleInterceptor implements HandlerInterceptor {

    private LocaleResolver localeResolver;
    private String paramName = "language";
    private List<String> supportedLanguages = Arrays.asList("fr", "de", "it");

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        String lang = null;

        // PRIORITÉ 1 : Paramètre de requête (query param ou form param)
        String paramValue = request.getParameter(paramName);
        if (paramValue != null && !paramValue.isEmpty()) {
            lang = paramValue.trim().toLowerCase();
        }

        // PRIORITÉ 2 : Header Accept-Language (si pas de paramètre)
        if (lang == null) {
            String acceptLanguage = request.getHeader("Accept-Language");
            if (acceptLanguage != null && !acceptLanguage.isEmpty()) {
                // Extraire la première langue (ex: "fr-CH,de;q=0.9" -> "fr")
                lang = acceptLanguage.split(",")[0].split("-")[0].trim().toLowerCase();
            }
        }

        // Appliquer le locale si valide et supporté
        if (lang != null && supportedLanguages.contains(lang)) {
            Locale locale = new Locale(lang);
            localeResolver.setLocale(request, response, locale);
        }

        return true;
    }

}