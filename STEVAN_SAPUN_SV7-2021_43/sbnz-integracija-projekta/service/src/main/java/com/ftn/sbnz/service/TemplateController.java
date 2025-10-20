package com.ftn.sbnz.service;

import com.ftn.sbnz.model.models.Machine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/template")
@CrossOrigin("*")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping("/run")
    public ResponseEntity<String> runTemplate() throws Exception {
        templateService.runTemplateExample("/rules/template/machine-template-data.xlsx",
                "/rules/template/machine-template.drt");
        return ResponseEntity.ok("Template rules executed!");
    }

    @PostMapping("/add-rule")
    public ResponseEntity<String> addRule(@RequestParam String ruleName, @RequestBody String drl) {
        templateService.addRule(ruleName, drl);
        return ResponseEntity.ok("Rule added dynamically!");
    }

    @PostMapping("/diagnose-machine")
    public ResponseEntity<Map<String, Object>> diagnoseMachine(@RequestBody Machine machine) {
        Map<String, Object> result = templateService.diagnoseMachineWithTemplateRules(machine);
        return ResponseEntity.ok(result);
    }
}
