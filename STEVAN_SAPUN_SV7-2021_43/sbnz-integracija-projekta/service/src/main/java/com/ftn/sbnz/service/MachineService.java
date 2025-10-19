package com.ftn.sbnz.service;

import com.ftn.sbnz.model.enums.MachineStatus;
import com.ftn.sbnz.model.enums.OperationContext;
import com.ftn.sbnz.model.models.Machine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class MachineService {

    private final MachineRepository machineRepository;

    @Autowired
    public MachineService(MachineRepository machineRepository) {
        this.machineRepository = machineRepository;
    }

    // This method should be inside your MachineService class
    public Machine create(String name) {
        String newId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        Machine newMachine = new Machine(
                newId, // String id
                name, // String name
                3.0,  // double vibration
                60.0, // double temperature
                90.0, // double currentPercentOfRated
                1200.0, // double rpm (assuming default RPM, must be double)
                MachineStatus.NORMAL, // MachineStatus status
                OperationContext.NORMAL, // OperationContext context (MISSING in your original call)
                now, // Instant lastUpdated
                0 // int overloadTripCount
        );

        return machineRepository.save(newMachine);
    }

    public List<Machine> findAll() {
        return machineRepository.findAll();
    }
}
