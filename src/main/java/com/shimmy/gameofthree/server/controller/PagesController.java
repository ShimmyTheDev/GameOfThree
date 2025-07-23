package com.shimmy.gameofthree.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PagesController {
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/play")
    public String play() {
        return "play";
    }

    @GetMapping("/game")
    public String game() {
        return "game";
    }
}
