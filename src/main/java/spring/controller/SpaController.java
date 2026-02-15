package spring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping({
            "/",
            "/files/**",
            "/sign-in",
            "/sign-up"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
