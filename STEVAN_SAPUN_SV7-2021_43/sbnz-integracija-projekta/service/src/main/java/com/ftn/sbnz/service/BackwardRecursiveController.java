package com.ftn.sbnz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ftn.sbnz.model.dto.HypothesisRequsetDTO;

import java.util.Map;

@RestController
@RequestMapping("/api/backward-recursive")
@CrossOrigin(origins = "*")
public class BackwardRecursiveController {

    private final BackwardRecursiveService backwardChainingService;

    @Autowired
    public BackwardRecursiveController(BackwardRecursiveService backwardChainingService) {
        this.backwardChainingService = backwardChainingService;
    }

    @PostMapping("/prove-machine-hypothesis")
    public Map<String, Object> proveHypothesis(@RequestBody HypothesisRequsetDTO request) throws Exception {
        return backwardChainingService.runBackwardChainingForOne(request.getMachineId(), request.getHypothesis());
    }

    @PostMapping("/run-example")
    public Map<String, Object> runExample() throws Exception {
        return backwardChainingService.runBackwardChainingExample();
    }

}
