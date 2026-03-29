package com.ftn.sbnz.service;

import com.ftn.sbnz.model.events.*;
import com.ftn.sbnz.model.models.IDiagnosticService;
import com.ftn.sbnz.model.models.Machine;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;

@Component
public class CepSessionManager {
    private final KieSession cepSession;

    @Autowired
    public CepSessionManager(KieContainer kieContainer, IDiagnosticService diagnosticService) {
        this.cepSession = kieContainer.newKieSession("cepKsession");
        this.cepSession.setGlobal("backwardService", diagnosticService);
    }

    public void processTelemetry(Machine m) {
        Date now = new Date();
        cepSession.insert(new TemperatureReading(m.getId(), m.getTemperature(), now));
        cepSession.insert(new VibrationReading(m.getId(), m.getVibration(), now));
        cepSession.insert(new CurrentReading(m.getId(), m.getCurrentPercentOfRated(), now));
        cepSession.insert(new ContextReading(m.getId(), m.getContext(), now));

        FactHandle machineHandle = cepSession.getFactHandle(m);
        if (machineHandle == null) {
            cepSession.insert(m);
        } else {
            // This is crucial: it tells Drools the object was updated outside the engine
            cepSession.update(machineHandle, m);
        }

        cepSession.fireAllRules();
    }

    public void resetMachineHistory(String machineId) {
        // We look for all 'AlertLock' facts in the stateful session that belong to this machine
        Collection<FactHandle> handles = cepSession.getFactHandles(obj -> {
            // Check if the fact is an AlertLock (declared in DRL)
            if (obj.getClass().getSimpleName().equals("AlertLock")) {
                try {
                    // Get the machineId field via reflection
                    String lockId = (String) obj.getClass().getMethod("getMachineId").invoke(obj);
                    return machineId.equals(lockId);
                } catch (Exception e) { return false; }
            }
            return false;
        });

        // Delete the locks so the machine is "clean"
        for (FactHandle h : handles) {
            cepSession.delete(h);
        }

        // Also update the Machine status to NORMAL in the CEP memory
        // so rules start checking it again
        FactHandle machineHandle = cepSession.getFactHandles(o ->
                o instanceof Machine && ((Machine)o).getId().equals(machineId)).stream().findFirst().orElse(null);

        if (machineHandle != null) {
            Machine m = (Machine) cepSession.getObject(machineHandle);
            m.setStatus(com.ftn.sbnz.model.enums.MachineStatus.NORMAL);
            m.getRecommendations().clear();
            cepSession.update(machineHandle, m);
        }

        cepSession.fireAllRules();
        System.out.println(">>> Drools Memory Cleared for machine: " + machineId);
    }
}