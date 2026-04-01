package com.ftn.sbnz.service;

import com.ftn.sbnz.model.enums.MachineStatus;
import com.ftn.sbnz.model.models.Machine;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // ADD THIS INJECTION
    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

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
        String routingKey = "commands." + id;
        rabbitTemplate.convertAndSend(routingKey, action);

        if ("RESET".equals(action)) {
            machineRepository.findById(id).ifPresent(m -> {
                m.setStatus(MachineStatus.NORMAL);
                m.getRecommendations().clear();
                machineRepository.save(m);

                // 1. Wipe Drools
                cepSessionManager.resetMachineHistory(id);

                // 2. BROADCAST A CLEAR MESSAGE: Tells Angular to empty the alert feed
                Map<String, Object> clearMsg = new HashMap<>();
                clearMsg.put("type", "SYSTEM_RESET");
                clearMsg.put("machineId", id);
                messagingTemplate.convertAndSend("/topic/diagnosis", clearMsg);

                // 3. BROADCAST THE CLEAN MACHINE: Fixes the card color instantly
                messagingTemplate.convertAndSend("/topic/machines", m);
            });
        }
        return ResponseEntity.ok().build();
    }
}
