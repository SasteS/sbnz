package com.ftn.sbnz.service;

import com.ftn.sbnz.model.enums.MachineStatus;
import com.ftn.sbnz.model.events.VibrationReading;
import com.ftn.sbnz.model.models.Machine;
import com.ftn.sbnz.model.events.TemperatureReading;
import org.drools.core.time.SessionPseudoClock;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class CepService {

    private final KieContainer kieContainer;

    @Autowired
    public CepService(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    public Machine runCepExample() {
        KieSession ksession = kieContainer.newKieSession("cepKsession");
        SessionPseudoClock clock = ksession.getSessionClock();

        // Insert machine
        Machine m = new Machine("M1", "Pump A");
        Machine m2 = new Machine("M2", "Pump B");
        ksession.insert(m);
        ksession.insert(m2);

        // Temperature Rule
        ksession.insert(new TemperatureReading("M1", 101.0, new Date()));
        ksession.insert(new TemperatureReading("M2", 100.0, new Date()));
        clock.advanceTime(10, TimeUnit.SECONDS);

        ksession.insert(new TemperatureReading("M1", 103.0, new Date()));
        ksession.insert(new TemperatureReading("M2", 101.0, new Date()));
        clock.advanceTime(10, TimeUnit.SECONDS);

        ksession.insert(new TemperatureReading("M1", 105.0, new Date()));
        ksession.insert(new TemperatureReading("M2", 103.0, new Date()));
        clock.advanceTime(10, TimeUnit.SECONDS);

        // Vibration Rule
        ksession.insert(new VibrationReading("M1", 10.5, new Date()));
        ksession.insert(new VibrationReading("M2", 10.5, new Date()));
        //ksession.insert(new TemperatureReading("M2", 103.0, new Date()));
        clock.advanceTime(10, TimeUnit.SECONDS);

        ksession.insert(new VibrationReading("M1", 12.0, new Date()));
        clock.advanceTime(10, TimeUnit.SECONDS);

        ksession.insert(new VibrationReading("M1", 15.0, new Date()));
        clock.advanceTime(10, TimeUnit.SECONDS);

        // Combined CEP Rule (Vibration + Temperature)
        // Already inserted temperature & vibration above, CEP should detect correlation within 10 min window

        int fired = ksession.fireAllRules();

        System.out.println("Rules fired: " + fired);
        System.out.println("Machine1 after CEP: " + m);
        System.out.println("Machine2 afrer CEP: " + m2);

        ksession.dispose();
        return m;
    }
}
