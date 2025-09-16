package com.ftn.sbnz.service;

import com.ftn.sbnz.model.enums.MachineStatus;
import com.ftn.sbnz.model.models.Machine;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Test {

    private final KieContainer kieContainer;

    @Autowired
    public Test(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    public void runTest() {
        KieSession kSession = kieContainer.newKieSession("k-session");

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

        kSession.insert(machine1);
        kSession.insert(machine2);
        kSession.insert(machine3);
        int fired = kSession.fireAllRules();
        System.out.println("Number of rules fired: " + fired);

        kSession.dispose();

        System.out.println("Final machine states:");
        System.out.println(machine1);
        System.out.println(machine2);
        System.out.println(machine3);
    }
}
