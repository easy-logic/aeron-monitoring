package io.aeron.monitoring;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexPageController {

    private static final String TEMPLATE_NAME = "index";
    private static final String TEMPLATE_KEY_APP_VERSION = "app_version";
    private static final String TEMPLATE_KEY_PAGE_TITLE = "page_title";
    private static final String TEMPLATE_KEY_TOOLBAR_TITLE = "toolbar_title";
    
    @Value("${app.version}")
    private String appVersion;

    @Value("${page.index.title}")
    private String pageTitle;

    @Value("${page.index.toolbar.title}")
    private String toolBarTitle;

    @RequestMapping("/")
    public String welcome(final Map<String, Object> model) {
        model.put(TEMPLATE_KEY_APP_VERSION, appVersion);
        model.put(TEMPLATE_KEY_PAGE_TITLE, pageTitle);
        model.put(TEMPLATE_KEY_TOOLBAR_TITLE, toolBarTitle);
        return TEMPLATE_NAME;
    }
}
