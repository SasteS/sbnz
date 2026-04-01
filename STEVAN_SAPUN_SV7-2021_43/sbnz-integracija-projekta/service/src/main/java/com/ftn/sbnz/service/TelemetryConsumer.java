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
    private CepSessionManager cepSessionManager;

    @Autowired
    private KieContainer kieContainer;

    // ADD THIS INJECTION
    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = "machine_telemetry")
    @Transactional
    public void consumeTelemetry(Machine incomingMachine) {
        machineRepository.findById(incomingMachine.getId()).ifPresent(existingMachine -> {
            // 1. Set values on the DB instance
            existingMachine.setTemperature(incomingMachine.getTemperature());
            existingMachine.setVibration(incomingMachine.getVibration());
            existingMachine.setCurrentPercentOfRated(incomingMachine.getCurrentPercentOfRated());
            existingMachine.setContext(incomingMachine.getContext());
            existingMachine.setLastUpdated(incomingMachine.getLastUpdated());

            // 2. IMPORTANT: Pass to CEP and GET BACK the instance Drools modified
            Machine droolsModifiedMachine = cepSessionManager.processTelemetry(existingMachine);

            // 3. Run Forward Logic on the modified instance
            KieSession ksession = kieContainer.newKieSession("k-session");
            try {
                ksession.setGlobal("backwardService", backwardRecursiveService);
                // Insert the machine that potentially already has CEP changes
                ksession.insert(droolsModifiedMachine);
                ksession.fireAllRules();
            } finally {
                ksession.dispose();
            }

            // 4. Save the machine that Drools actually changed
            machineRepository.save(droolsModifiedMachine);

            // 5. Broadcast the machine that Drools actually changed
            messagingTemplate.convertAndSend("/topic/machines", droolsModifiedMachine);
        });
    }
}