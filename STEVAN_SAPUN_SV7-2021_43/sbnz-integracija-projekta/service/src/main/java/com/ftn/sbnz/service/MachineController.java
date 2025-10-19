package com.ftn.sbnz.service;

import com.ftn.sbnz.model.enums.MachineStatus;
import com.ftn.sbnz.model.models.Machine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/machines")
@CrossOrigin(origins = "http://localhost:4200")
public class MachineController {

    private final MachineService machineService;

    @Autowired
    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    @PostMapping("/create")
    public ResponseEntity<Machine> createMachine(@RequestParam String name) {
        Machine saved = machineService.create(name);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Machine>> getAllMachines() {
        return ResponseEntity.ok(machineService.findAll());
    }
}
