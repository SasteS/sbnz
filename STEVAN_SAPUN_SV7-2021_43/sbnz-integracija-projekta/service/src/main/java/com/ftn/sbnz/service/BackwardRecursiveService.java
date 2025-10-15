package com.ftn.sbnz.service;

import com.ftn.sbnz.model.enums.OperationContext;
import com.ftn.sbnz.model.models.Machine;
import org.kie.api.definition.type.FactType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BackwardRecursiveService {

    private final KieContainer kieContainer;

    @Autowired
    public BackwardRecursiveService(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    public List<String> runBackwardChainingExample() throws Exception {
        KieSession ksession = kieContainer.newKieSession("bwKsession");

        // ✅ Insert a sample machine with conditions to trigger recursion
        Machine m = new Machine("M1", "Pump A");
        m.setTemperature(95.0); // high temp
        m.setVibration(12.0);   // high vibration
        m.setCurrentPercentOfRated(130.0); // high current

        ksession.insert(m);

        // Fire rules
        ksession.fireAllRules();

        // Since in fireAllRules we make the tree, here we print it
        printDependencyTree(ksession, "CriticalMachine");

        // Query which hypotheses were proven
        QueryResults results = ksession.getQueryResults("proveHypothesis", "CriticalMachine");
        List<String> proven = new ArrayList<>();

        for (QueryResultsRow row : results) {
            proven.add("Proven hypothesis: " + row.get("$hypothesis"));
        }

        ksession.dispose();
        return proven;
    }

    public static void printDependencyTree(KieSession ksession, String root) throws Exception {
        // Get the FactType for HypothesisDependency from your KieBase

        FactType depType = ksession.getKieBase().getFactType("rules.backward", "HypothesisDependency");

        printNode(ksession, root, 0, depType);
    }

    private static void printNode(KieSession ksession, String node, int indent, FactType depType) throws Exception {
        for (int i = 0; i < indent; i++) System.out.print("  ");
        System.out.println(node);

        // Iterate over all facts of this type
        for (Object fact : ksession.getObjects()) {
            if (!depType.getFactClass().isInstance(fact)) continue;

            String conclusion = (String) depType.get(fact, "conclusion");
            if (!conclusion.equals(node)) continue;

            List<?> prerequisites = (List<?>) depType.get(fact, "prerequisites");

            for (Object prereqObj : prerequisites) {
                String prereq = (String) prereqObj;
                printNode(ksession, prereq, indent + 1, depType);
            }
        }
    }



}
