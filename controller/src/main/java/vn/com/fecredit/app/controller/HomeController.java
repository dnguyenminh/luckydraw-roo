package vn.com.fecredit.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller to handle redirects for SPA routing.
 * Since we're using a Single Page Application, we need to redirect any
 * non-API routes back to the index.html to let the frontend router handle them.
 */
@Controller
public class HomeController {
    
    /**
     * Catch-all mapping that redirects to index.html for SPA routing.
     * This is needed in case a user refreshes the page on a route like /events or /users.
     * 
     * @return the forwarded path to index.html
     */
    @GetMapping(value = {"/", "/{x:[\\w\\-]+}", "/{x:^(?!api$).*$}/**/{y:[\\w\\-]+}"})
    public String getIndex() {
        return "forward:/index.html";
    }
}