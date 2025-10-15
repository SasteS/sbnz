package com.ftn.sbnz.service;

import com.ftn.sbnz.service.BackwardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/backward")
public class BackwardController {

    private final BackwardService backwardService;

    @Autowired
    public BackwardController(BackwardService backwardService) {
        this.backwardService = backwardService;
    }

    @GetMapping("/run-example")
    public List<String> runBackwardExample() {
        return backwardService.runBackwardExample();
    }
}
