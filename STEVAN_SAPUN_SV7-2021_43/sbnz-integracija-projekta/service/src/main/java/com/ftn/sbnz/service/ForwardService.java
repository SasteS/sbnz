package com.ftn.sbnz.service;

import com.ftn.sbnz.model.enums.MachineStatus;
import com.ftn.sbnz.model.models.Machine;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ForwardService {

    private final KieContainer kieContainer;

    @Autowired
    public ForwardService(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    public String runForwardRules() {
        KieSession ksession = kieContainer.newKieSession("k-session");

        // Create machines
        Machine machine1 = new Machine();
        machine1.setId("M1");
        machine1.setName("Pump A");
        machine1.setVibration(8.0);
        machine1.setTemperature(70.0);
        machine1.setCurrentPercentOfRated(100.0);
        machine1.setStatus(MachineStatus.NORMAL);

        Machine machine2 = new Machine();
        machine2.setId("M2");
        machine2.setName("Pump B");
        machine2.setVibration(5.0);
        machine2.setTemperature(90.0);
        machine2.setCurrentPercentOfRated(100.0);
        machine2.setStatus(MachineStatus.NORMAL);

        Machine machine3 = new Machine();
        machine3.setId("M3");
        machine3.setName("Pump C");
        machine3.setVibration(8.0);
        machine3.setTemperature(90.0);
        machine3.setCurrentPercentOfRated(100.0);
        machine3.setStatus(MachineStatus.NORMAL);

        // Insert into session
        ksession.insert(machine1);
        ksession.insert(machine2);
        ksession.insert(machine3);

        // Fire rules
        int fired = ksession.fireAllRules();
        ksession.dispose();

        // Build result string
        StringBuilder sb = new StringBuilder();
        sb.append("Number of rules fired: ").append(fired).append("\n");
        sb.append("Final machine states:\n");
        sb.append(machine1).append("\n");
        sb.append(machine2).append("\n");
        sb.append(machine3).append("\n");

        return sb.toString();
    }
}
