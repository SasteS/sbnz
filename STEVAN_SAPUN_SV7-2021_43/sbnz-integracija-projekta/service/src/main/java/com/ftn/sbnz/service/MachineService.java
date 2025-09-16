package com.ftn.sbnz.service;

import com.ftn.sbnz.model.models.Machine;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MachineService {

    private final KieContainer kieContainer;

    @Autowired
    public MachineService(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    public Machine evaluate(Machine machine) {
        KieSession kieSession = kieContainer.newKieSession();
        kieSession.insert(machine);
        kieSession.fireAllRules();
        kieSession.dispose();
        return machine;
    }
}
