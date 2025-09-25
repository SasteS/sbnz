package com.ftn.sbnz.service;

import com.ftn.sbnz.service.CepService;
import com.ftn.sbnz.model.models.Machine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cep")
public class CepController {

    private final CepService cepService;

    @Autowired
    public CepController(CepService cepService) {
        this.cepService = cepService;
    }

    // CEP example endpoint
    @GetMapping("/run-example")
    public Machine runCepExample() {
        return cepService.runCepExample();
    }
}
