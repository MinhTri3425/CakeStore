// controller/FavoriteController.java
package com.cakestore.cakestore.controller;

import com.cakestore.cakestore.service.FavoriteSessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/fav")
public class FavoriteController {
    private final FavoriteSessionService favSvc;

    public FavoriteController(FavoriteSessionService s) {
        favSvc = s;
    }

    @PostMapping("/toggle")
    public String toggle(@RequestParam Long productId, HttpSession session, @RequestHeader("Referer") String ref) {
        var set = favSvc.ensure(session);
        if (set.contains(productId))
            set.remove(productId);
        else
            set.add(productId);
        return "redirect:" + (ref != null ? ref : "/");
    }
}
