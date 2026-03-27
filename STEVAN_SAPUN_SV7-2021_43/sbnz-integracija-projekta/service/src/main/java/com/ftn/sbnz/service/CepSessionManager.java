package com.ftn.sbnz.service;

import com.ftn.sbnz.model.events.*;
import com.ftn.sbnz.model.models.IDiagnosticService;
import com.ftn.sbnz.model.models.Machine;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
}