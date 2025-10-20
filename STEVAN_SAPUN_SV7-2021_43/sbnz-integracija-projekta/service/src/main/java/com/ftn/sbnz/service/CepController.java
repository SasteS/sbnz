package com.ftn.sbnz.service;

import com.ftn.sbnz.service.CepService;
import com.ftn.sbnz.model.models.Machine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cep")
@CrossOrigin("*")
public class CepController {

    private final CepService cepService;

    @Autowired
    public CepController(CepService cepService) {
        this.cepService = cepService;
    }

    @PostMapping("/run-on-machine/{machineId}")
    public Map<String, Object> runCepOnMachine(@PathVariable String machineId) {
        return cepService.runCepOnMachine(machineId);
    }

    // CEP example endpoint
    @GetMapping("/run-example")
    public Machine runCepExample() {
        return cepService.runCepExample();
    }
}
