package com.ftn.sbnz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/forward")
public class ForwardController {

    private final ForwardService forwardService;

    @Autowired
    public ForwardController(ForwardService forwardService) {
        this.forwardService = forwardService;
    }

    @GetMapping("/run-example")
    public String runForward() {
        return forwardService.runForwardRules();
    }
}
