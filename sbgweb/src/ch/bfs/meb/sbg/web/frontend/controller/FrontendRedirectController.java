package ch.bfs.meb.sbg.web.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class FrontendRedirectController {
    @GetMapping("/refonte")
    public String redirectToFrontend() {
        return "forward:/frontFolder/index.html";
    }



    @GetMapping("/data-delivery")
    public String redirectToFrontenddatadelivery() {
        return "forward:/frontFolder/index.html";
    }

    @GetMapping("/data-maintain")
    public String redirectToFrontenddatamaintain() {
        return "forward:/frontFolder/index.html";
    }

    @GetMapping("/administration")
    public String redirectToFrontendadministration  () {
        return "forward:/frontFolder/index.html";
    }




    @GetMapping("/assets/**")
    public String redirectAsset(HttpServletRequest request) {

        String path = request.getRequestURI()
                .replace("/sbgweb/api/assets/", "");

        return "redirect:/frontFolder/assets/" + path;
    }


}
