package com.ftn.sbnz.service;

import com.ftn.sbnz.model.events.*;
import com.ftn.sbnz.model.models.IDiagnosticService;
import com.ftn.sbnz.model.models.Machine;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Component
public class CepSessionManager {
    private final KieSession cepSession;

    @Autowired
    public CepSessionManager(KieContainer kieContainer, IDiagnosticService diagnosticService) {
        this.cepSession = kieContainer.newKieSession("cepKsession");
        this.cepSession.setGlobal("backwardService", diagnosticService);
    }

    public synchronized Machine processTelemetry(Machine m) {
        Date now = new Date();
        // Insert events under lock
        cepSession.insert(new TemperatureReading(m.getId(), m.getTemperature(), now));
        cepSession.insert(new VibrationReading(m.getId(), m.getVibration(), now));
        cepSession.insert(new CurrentReading(m.getId(), m.getCurrentPercentOfRated(), now));
        cepSession.insert(new ContextReading(m.getId(), m.getContext(), now));

        FactHandle machineHandle = cepSession.getFactHandles(o ->
                        o instanceof Machine && ((Machine)o).getId().equals(m.getId()))
                .stream().findFirst().orElse(null);

        Machine machineToReturn;
        if (machineHandle == null) {
            cepSession.insert(m);
            machineToReturn = m;
        } else {
            machineToReturn = (Machine) cepSession.getObject(machineHandle);
            machineToReturn.setTemperature(m.getTemperature());
            machineToReturn.setVibration(m.getVibration());
            machineToReturn.setContext(m.getContext());
            machineToReturn.setCurrentPercentOfRated(m.getCurrentPercentOfRated());
            cepSession.update(machineHandle, machineToReturn);
        }

        cepSession.fireAllRules();
        return machineToReturn;
    }

    public synchronized void resetMachineHistory(String machineId) {
        // 1. Identify all fact handles for this machine
        List<FactHandle> toDelete = new ArrayList<>();

        for (FactHandle handle : cepSession.getFactHandles()) {
            Object obj = cepSession.getObject(handle);
            if (obj == null) continue;

            String id = getMachineIdFromObject(obj);

            if (machineId.equals(id)) {
                // If it's the actual Machine object, just reset it
                if (obj instanceof Machine) {
                    Machine m = (Machine) obj;
                    m.setStatus(com.ftn.sbnz.model.enums.MachineStatus.NORMAL);
                    m.getRecommendations().clear();
                    cepSession.update(handle, m);
                } else {
                    // Delete everything else (Readings, Locks, etc.)
                    toDelete.add(handle);
                }
            }
        }

        // 2. Perform deletions
        for (FactHandle h : toDelete) {
            cepSession.delete(h);
        }

        // 3. Force the engine to recognize the empty state
        cepSession.fireAllRules();
        System.out.println(">>> [SESSION PURGED] Machine " + machineId + " is now clean.");
    }

    // Helper to extract ID regardless of class type
    private String getMachineIdFromObject(Object obj) {
        try {
            if (obj instanceof com.ftn.sbnz.model.events.SensorReading) {
                return ((com.ftn.sbnz.model.events.SensorReading) obj).getMachineId();
            }
            if (obj instanceof Machine) {
                return ((Machine) obj).getId();
            }
            // For AlertLock (which is a declared DRL type)
            return (String) obj.getClass().getMethod("getMachineId").invoke(obj);
        } catch (Exception e) {
            return null;
        }
    }
}