package com.ftn.sbnz.service;

import com.ftn.sbnz.model.enums.MachineStatus;
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

        // -----------------------------------------------------------------
        // Example 1: Overheat scenario
        Machine m1 = new Machine("M1", "Pump A");
        m1.setTemperature(92.0); // triggers TemperatureHigh → Overheat
        m1.setVibration(5.0);
        m1.setCurrentPercentOfRated(95.0);
        m1.setStatus(MachineStatus.NORMAL);
        ksession.insert(m1);

        // Example 2: Bearing fault scenario (temp + vibration)
        Machine m2 = new Machine("M2", "Compressor B");
        m2.setTemperature(90.0);
        m2.setVibration(8.0); // triggers BearingFault path
        m2.setCurrentPercentOfRated(90.0);
        m2.setStatus(MachineStatus.NORMAL);
        ksession.insert(m2);

        // Example 3: Electrical overload scenario
        Machine m3 = new Machine("M3", "Motor C");
        m3.setTemperature(60.0);
        m3.setVibration(5.0);
        m3.setCurrentPercentOfRated(130.0);
        m3.setOverloadTripCount(2);
        m3.setStatus(MachineStatus.NORMAL);
        ksession.insert(m3);

        // -----------------------------------------------------------------
        // Fire rules (this will initialize dependencies + run backward proofs)
        int fired = ksession.fireAllRules();
        System.out.println("=== Backward chaining complete (" + fired + " rules fired) ===\n");

        // -----------------------------------------------------------------
        // Print dependency tree (to visualize reasoning structure)
        printDependencyTree(ksession, "AtRisk");
        printDependencyTree(ksession, "BearingFault");
        printDependencyTree(ksession, "ElectricalOverload");

        // -----------------------------------------------------------------
        // Run and collect proof queries (per machine)
        List<String> proven = new ArrayList<>();
        String[] hypotheses = {"Overheat", "BearingFault", "ElectricalOverload"};
        List<Machine> machines = List.of(m1, m2, m3);

        for (Machine m : machines) {
            for (String h : hypotheses) {
                // ✅ This is where you run the query to see if the hypothesis is proven
                QueryResults results = ksession.getQueryResults("proveHypothesis", h, m.getId());

                if (results.size() > 0) {
                    proven.add("✅ Proven hypothesis: " + h + " for " + m.getName());
                } else {
                    proven.add("❌ Not proven: " + h + " for " + m.getName());
                }
            }
        }
        // MUST CALL AGAIN TO TRIGGER QUERY DEPENDANT RULES!!!!!!!
        int firedAfterQueries = ksession.fireAllRules();
        System.out.println("=== Rules fired after query checks: " + firedAfterQueries + " ===\n");

        // -----------------------------------------------------------------
        // Show resulting machine states
        System.out.println("\n=== Machine States After Backward Reasoning ===");
        System.out.println(m1.getName() + " → " + m1.getStatus() + " | " + m1.getRecommendations());
        System.out.println(m2.getName() + " → " + m2.getStatus() + " | " + m2.getRecommendations());
        System.out.println(m3.getName() + " → " + m3.getStatus() + " | " + m3.getRecommendations());

        ksession.dispose();
        return proven;
    }

    // -----------------------------------------------------------------
    // Utility: Print dependency tree recursively
    // -----------------------------------------------------------------
    public static void printDependencyTree(KieSession ksession, String root) throws Exception {
        FactType depType = ksession.getKieBase().getFactType("rules.backward", "HypothesisDependency");

        System.out.println("\n📚 Dependency tree for: " + root);
        printNode(ksession, root, 0, depType);
    }

    private static void printNode(KieSession ksession, String node, int indent, FactType depType) throws Exception {
        for (int i = 0; i < indent; i++) System.out.print("  ");
        System.out.println("• " + node);

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
