package io.aeron.monitoring;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class IndexPageController {

    @RequestMapping("/")
    public RedirectView welcome() {
        return new RedirectView("/swagger-ui.html");
    }


}
