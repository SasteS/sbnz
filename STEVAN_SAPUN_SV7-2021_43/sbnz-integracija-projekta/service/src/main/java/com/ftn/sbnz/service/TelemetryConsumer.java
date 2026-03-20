package com.ftn.sbnz.service;

import com.ftn.sbnz.model.models.Machine;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TelemetryConsumer {

    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    private BackwardRecursiveService backwardRecursiveService;

    @Autowired
    private KieContainer kieContainer;

    // ADD THIS INJECTION
    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = "machine_telemetry")
    @Transactional
    public void consumeTelemetry(Machine incomingMachine) {
        machineRepository.findById(incomingMachine.getId()).ifPresent(existingMachine -> {
            // 1. Update existing machine
            existingMachine.setTemperature(incomingMachine.getTemperature());
            existingMachine.setVibration(incomingMachine.getVibration());
            existingMachine.setLastUpdated(incomingMachine.getLastUpdated());
            existingMachine.setContext(incomingMachine.getContext());

            // 2. Run Forward Logic
            KieSession ksession = kieContainer.newKieSession("k-session");
            try {
                ksession.setGlobal("backwardService", backwardRecursiveService);
                ksession.insert(existingMachine);
                ksession.fireAllRules();
            } finally {
                ksession.dispose();
            }

            // 3. Save
            machineRepository.save(existingMachine);

            // 4. BROADCAST the update to the Dashboard
            // We use 'existingMachine' because it now contains the latest temp + Drools status
            messagingTemplate.convertAndSend("/topic/machines", existingMachine);
        });
    }
}