package com.ftn.sbnz.service;

import com.ftn.sbnz.model.models.Machine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forward")
@CrossOrigin(origins = "http://localhost:4200")
public class ForwardController {

    private final ForwardService forwardService;

    @Autowired
    public ForwardController(ForwardService forwardService) {
        this.forwardService = forwardService;
    }

    @PostMapping("/run")
    public ResponseEntity<Machine> runForwardRules(@RequestBody Machine machine) throws Exception {
        Machine result = forwardService.runForwardRules(machine);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/run-batch")
    public ResponseEntity<List<Machine>> runForwardRulesBatch(@RequestBody List<Machine> machines) {
        List<Machine> result = forwardService.runForwardRulesBatch(machines);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/run-example")
    public String runForward() {
        return forwardService.runForwardRulesExample();
    }
}
