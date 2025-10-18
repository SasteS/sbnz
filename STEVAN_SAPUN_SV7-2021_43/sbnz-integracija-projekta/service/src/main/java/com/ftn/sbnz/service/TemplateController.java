package com.ftn.sbnz.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/template")
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
}
