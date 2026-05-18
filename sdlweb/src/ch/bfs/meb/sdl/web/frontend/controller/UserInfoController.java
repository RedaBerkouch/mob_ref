package ch.bfs.meb.sdl.web.frontend.controller;

import ch.bfs.meb.security.MebUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UserInfoController {
    @GetMapping("/user")
    public Map<String, Object> getCurrentUser(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Principal principal = request.getUserPrincipal();

        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal != null) {
            response.put("authenticated", true);
            response.put("username", principal.getName());
            response.put("principalClass", principal.getClass().getName());
            response.put("roles", user.getAuthorities());

        } else {
            response.put("authenticated", false);
        }

        return response;
    }
}
