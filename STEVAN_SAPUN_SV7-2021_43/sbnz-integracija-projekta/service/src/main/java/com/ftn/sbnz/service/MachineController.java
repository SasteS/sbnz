package com.ftn.sbnz.service;

import com.ftn.sbnz.model.enums.MachineStatus;
import com.ftn.sbnz.model.models.Machine;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    private CepSessionManager cepSessionManager;

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

    @PostMapping("/{id}/command")
    public ResponseEntity<Void> sendCommand(@PathVariable String id, @RequestBody String action) {
        // Dispatch to Go Simulator
        String routingKey = "commands." + id;
        rabbitTemplate.convertAndSend(routingKey, action);

        if ("RESET".equals(action)) {
            // 2. Resolve the case in the Database
            machineRepository.findById(id).ifPresent(m -> {
                m.setStatus(MachineStatus.NORMAL);
                m.getRecommendations().clear();
                machineRepository.save(m);
            });

            // 3. IMPORTANT: Wipe Drools Memory for this machine
            // This prevents CEP rules from re-firing immediately after repair
            cepSessionManager.resetMachineHistory(id);
        }

        System.out.println(">>> System Command [" + action + "] dispatched to unit: " + id);
        return ResponseEntity.ok().build();
    }
}
