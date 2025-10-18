//package com.ftn.sbnz.service;
//
//import com.ftn.sbnz.model.enums.MachineStatus;
//import com.ftn.sbnz.model.events.VibrationReading;
//import com.ftn.sbnz.model.models.Machine;
//import com.ftn.sbnz.model.events.TemperatureReading;
//import org.drools.core.time.SessionPseudoClock;
//import org.kie.api.runtime.KieContainer;
//import org.kie.api.runtime.KieSession;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.time.Instant;
//import java.util.Date;
//import java.util.concurrent.TimeUnit;
//
//@Service
//public class CepService {
//
//    private final KieContainer kieContainer;
//
//    @Autowired
//    public CepService(KieContainer kieContainer) {
//        this.kieContainer = kieContainer;
//    }
//
//    public Machine runCepExample() {
//        KieSession ksession = kieContainer.newKieSession("cepKsession");
//        SessionPseudoClock clock = ksession.getSessionClock();
//
//        // Insert machine
//        Machine m = new Machine("M1", "Pump A");
//        Machine m2 = new Machine("M2", "Pump B");
//        ksession.insert(m);
//        ksession.insert(m2);
//
//        // Temperature Rule
//        ksession.insert(new TemperatureReading("M1", 101.0, new Date()));
//        ksession.insert(new TemperatureReading("M2", 100.0, new Date()));
//        clock.advanceTime(10, TimeUnit.SECONDS);
//
//        ksession.insert(new TemperatureReading("M1", 103.0, new Date()));
//        ksession.insert(new TemperatureReading("M2", 101.0, new Date()));
//        clock.advanceTime(10, TimeUnit.SECONDS);
//
//        ksession.insert(new TemperatureReading("M1", 105.0, new Date()));
//        ksession.insert(new TemperatureReading("M2", 103.0, new Date()));
//        clock.advanceTime(10, TimeUnit.SECONDS);
//
//        // Vibration Rule
//        ksession.insert(new VibrationReading("M1", 10.5, new Date()));
//        ksession.insert(new VibrationReading("M2", 10.5, new Date()));
//        //ksession.insert(new TemperatureReading("M2", 103.0, new Date()));
//        clock.advanceTime(10, TimeUnit.SECONDS);
//
//        ksession.insert(new VibrationReading("M1", 12.0, new Date()));
//        clock.advanceTime(10, TimeUnit.SECONDS);
//
//        ksession.insert(new VibrationReading("M1", 15.0, new Date()));
//        clock.advanceTime(10, TimeUnit.SECONDS);
//
//        // Combined CEP Rule (Vibration + Temperature)
//        // Already inserted temperature & vibration above, CEP should detect correlation within 10 min window
//
//        int fired = ksession.fireAllRules();
//
//        System.out.println("Rules fired: " + fired);
//        System.out.println("Machine1 after CEP: " + m);
//        System.out.println("Machine2 afrer CEP: " + m2);
//
//        ksession.dispose();
//        return m;
//    }
//}



package com.ftn.sbnz.service;

import com.ftn.sbnz.model.enums.MachineStatus;
import com.ftn.sbnz.model.enums.OperationContext;
import com.ftn.sbnz.model.events.*;
import com.ftn.sbnz.model.models.Machine;
import org.drools.core.time.SessionPseudoClock;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        // --- MACHINE SETUP -------------------------------------------------
        Machine m1 = new Machine("M1", "Pump A");
        Machine m2 = new Machine("M2", "Compressor B");
        ksession.insert(m1);
        ksession.insert(m2);

        System.out.println("=== Starting CEP simulation ===");

        // ------------------------------------------------------------------
        // CONTEXT 1: Machine 1 starts under HIGH LOAD
        // ------------------------------------------------------------------
        ksession.insert(new ContextReading("M1", OperationContext.HIGH_LOAD, new Date()));
        System.out.println("[CTX] M1 in HighLoad context");
        clock.advanceTime(10, TimeUnit.SECONDS);

        // Machine 2 is IDLE
        ksession.insert(new ContextReading("M2", OperationContext.IDLE, new Date()));
        System.out.println("[CTX] M2 in Idle context");
        clock.advanceTime(10, TimeUnit.SECONDS);

        // ------------------------------------------------------------------
        // MACHINE 1 - Abnormal behavior: rising vibration & temperature
        // ------------------------------------------------------------------
        System.out.println("\n[Time 0s] Inserting Machine 1 readings...");
        ksession.insert(new VibrationReading("M1", 7.5, new Date()));
        ksession.insert(new TemperatureReading("M1", 82, new Date()));
        clock.advanceTime(10, TimeUnit.SECONDS);

        ksession.insert(new VibrationReading("M1", 9.0, new Date()));
        ksession.insert(new TemperatureReading("M1", 86, new Date()));
        clock.advanceTime(10, TimeUnit.SECONDS);

        ksession.insert(new VibrationReading("M1", 11.0, new Date()));
        clock.advanceTime(10, TimeUnit.SECONDS);

        ksession.insert(new VibrationReading("M1", 12.0, new Date()));
        ksession.insert(new TemperatureReading("M1", 88, new Date()));
        clock.advanceTime(10, TimeUnit.SECONDS);

        ksession.insert(new VibrationReading("M1", 13.0, new Date()));
        clock.advanceTime(10, TimeUnit.SECONDS);

        // ------------------------------------------------------------------
        // MACHINE 2 - IDLE and heating up (to trigger Idle rule)
        // ------------------------------------------------------------------
        System.out.println("\n[Time 50s] Inserting Machine 2 readings...");
        ksession.insert(new TemperatureReading("M2", 65.0, new Date()));
        clock.advanceTime(10, TimeUnit.SECONDS);
        ksession.insert(new TemperatureReading("M2", 67.0, new Date()));
        clock.advanceTime(10, TimeUnit.SECONDS);
        ksession.insert(new TemperatureReading("M2", 70.0, new Date())); // should trigger idle high-temp rule
        clock.advanceTime(10, TimeUnit.SECONDS);

        // ------------------------------------------------------------------
        // MACHINE 1 - PostMaintenance context test
        // ------------------------------------------------------------------
        ksession.insert(new ContextReading("M1", OperationContext.POST_MAINTENANCE, new Date()));
        System.out.println("[CTX] M1 switched to PostMaintenance");
        clock.advanceTime(10, TimeUnit.SECONDS);
        ksession.insert(new TemperatureReading("M1", 85.0, new Date()));
        ksession.insert(new TemperatureReading("M1", 87.0, new Date()));
        ksession.insert(new TemperatureReading("M1", 90.0, new Date()));
        clock.advanceTime(30, TimeUnit.SECONDS);

        // ------------------------------------------------------------------
        // ADD Current overloads for M1 (for current rule)
        // ------------------------------------------------------------------
        ksession.insert(new CurrentReading("M1", 105.0, new Date()));
        clock.advanceTime(15, TimeUnit.SECONDS);
        ksession.insert(new CurrentReading("M1", 110.0, new Date()));
        clock.advanceTime(15, TimeUnit.SECONDS);
        ksession.insert(new CurrentReading("M1", 107.0, new Date()));
        clock.advanceTime(15, TimeUnit.SECONDS);

        // ------------------------------------------------------------------
        // FIRE RULES
        // ------------------------------------------------------------------
        int fired = ksession.fireAllRules();
        System.out.println("\n=== CEP processing complete ===");
        System.out.println("Total rules fired: " + fired);

        System.out.println("\nMachine 1 (M1) status: " + m1.getStatus());
        System.out.println("Recommendations: " + m1.getRecommendations());

        System.out.println("\nMachine 2 (M2) status: " + m2.getStatus());
        System.out.println("Recommendations: " + m2.getRecommendations());

        ksession.dispose();
        return m1;
    }
}
