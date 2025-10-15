package com.ftn.sbnz.service;

import com.ftn.sbnz.model.enums.MachineStatus;
import com.ftn.sbnz.model.models.Machine;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

@Service
public class BackwardService {

    private final KieContainer kieContainer;

    @Autowired
    public BackwardService(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    public List<String> runBackwardExample() {
        KieSession ksession = kieContainer.newKieSession("bwKsession");

        Machine m1 = new Machine("M1", "Pump A");
        m1.setStatus(MachineStatus.CRITICAL);
        m1.setTemperature(95.0);
        m1.setVibration(8.0);
        m1.setCurrentPercentOfRated(130.0);

        ksession.insert(m1);
        ksession.fireAllRules();

        QueryResults results = ksession.getQueryResults("CausesForMachine", "M1");
        List<String> causes = new ArrayList<>();
        for (QueryResultsRow row : results) {
            Object fact = row.get("$r");
            causes.add(fact.toString());
        }

        ksession.dispose();
        return causes;
    }
}
