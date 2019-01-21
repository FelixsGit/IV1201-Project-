package se.kth.iv1201.recruitmentsystem.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ApplicationController {

    static final String DEFAULT_PAGE_URL = "/";
    static final String DEFAULT_PAGE_SHOULDBECHANGED = "default";

    /**
     * No page is specified, redirect to the welcome page.
     *
     * @return A response that redirects the browser to the welcome page.
     */
    @GetMapping(DEFAULT_PAGE_URL)
    public String showDefaultView() {
        //return "redirect:" + DEFAULT_PAGE_SHOULDBECHANGED;
        return DEFAULT_PAGE_SHOULDBECHANGED;
    }

}