package com.ftn.sbnz.service;

import com.ftn.sbnz.service.BackwardRecursiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/backward-recursive")
@CrossOrigin(origins = "*")
public class BackwardRecursiveController {

    private final BackwardRecursiveService backwardChainingService;

    @Autowired
    public BackwardRecursiveController(BackwardRecursiveService backwardChainingService) {
        this.backwardChainingService = backwardChainingService;
    }

    @PostMapping("/run-example")
    public List<String> runExample() throws Exception {
        return backwardChainingService.runBackwardChainingExample();
    }
}
