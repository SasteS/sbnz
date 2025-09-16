package com.ftn.sbnz.service;

import com.ftn.sbnz.model.enums.MachineStatus;
import com.ftn.sbnz.model.models.Machine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/machine")
public class MachineController {

    private final MachineService machineService;

    @Autowired
    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    @GetMapping(produces = "application/json")
    public Machine evaluateMachine(
            @RequestParam String id,
            @RequestParam String name,
            @RequestParam double vibration,
            @RequestParam double temperature,
            @RequestParam double current) {

        Machine machine = new Machine();
        machine.setId(id);
        machine.setName(name);
        machine.setVibration(vibration);
        machine.setTemperature(temperature);
        machine.setCurrentPercentOfRated(current);
        machine.setStatus(MachineStatus.NORMAL);

        Machine evaluated = machineService.evaluate(machine);
        return evaluated;
    }
}
