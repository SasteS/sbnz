package com.ftn.sbnz.service;

import com.ftn.sbnz.model.dto.BackwardResultDTO;
import com.ftn.sbnz.model.enums.MachineStatus;
import com.ftn.sbnz.model.models.DroolsLog;
import com.ftn.sbnz.model.models.Machine;
import org.kie.api.definition.type.FactType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BackwardRecursiveService {

    private final KieContainer kieContainer;
    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    public BackwardRecursiveService(KieContainer kieContainer, MachineRepository machineRepository) {
        this.kieContainer = kieContainer;
        this.machineRepository = machineRepository;
    }

    public Map<String, Object> runBackwardChainingForOne(String machineId, String hypothesis) throws Exception {
        DroolsLog.clear();
        KieSession ksession = kieContainer.newKieSession("bwKsession");

        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new RuntimeException("Machine not found: " + machineId));

        ksession.insert(machine);

        int fired = ksession.fireAllRules();
        DroolsLog.log("=== Backward chaining complete (" + fired + " rules fired) ===");

        QueryResults results = ksession.getQueryResults("proveHypothesis", hypothesis, machine.getId());
        boolean proven = results.size() > 0;

        // 4. Fire rules again to ensure any consequences of the query are processed
        int firedAfterQueries = ksession.fireAllRules();
        DroolsLog.log("=== Rules fired after query checks: " + firedAfterQueries + " ===");

        // BEATS ME WHY THIS WORKS.
        boolean logProvenCheck = DroolsLog.getLogs().stream().anyMatch(log -> log.contains("Backward: " + hypothesis + " proven for"));

        // 5. Construct the single DTO result
        String recs = machine.getRecommendations().isEmpty()
                ? ""
                : String.join(", ", machine.getRecommendations());

        BackwardResultDTO dto = new BackwardResultDTO(
                machine.getName(),
                hypothesis,
                logProvenCheck,// proven,
                machine.getStatus().toString(),
                recs
        );


        Map<String, Object> response = new LinkedHashMap<>();
        response.put("results", List.of(dto));
        response.put("logs", DroolsLog.getLogs());

        ksession.dispose();
        return response;
    }

    public Map<String, Object> runBackwardChainingExample() throws Exception {
        DroolsLog.clear(); // 🧹 clear old logs
        KieSession ksession = kieContainer.newKieSession("bwKsession");

        // --- Example setup ---
        Machine m1 = new Machine("M1", "Pump A");
        m1.setTemperature(92.0);
        m1.setVibration(5.0);
        m1.setCurrentPercentOfRated(95.0);
        m1.setStatus(MachineStatus.NORMAL);
        ksession.insert(m1);

        Machine m2 = new Machine("M2", "Compressor B");
        m2.setTemperature(90.0);
        m2.setVibration(8.0);
        m2.setCurrentPercentOfRated(90.0);
        m2.setStatus(MachineStatus.NORMAL);
        ksession.insert(m2);

        Machine m3 = new Machine("M3", "Motor C");
        m3.setTemperature(60.0);
        m3.setVibration(5.0);
        m3.setCurrentPercentOfRated(130.0);
        m3.setOverloadTripCount(2);
        m3.setStatus(MachineStatus.NORMAL);
        ksession.insert(m3);

        // --- Run reasoning ---
        int fired = ksession.fireAllRules();
        DroolsLog.log("=== Backward chaining complete (" + fired + " rules fired) ===");

        List<BackwardResultDTO> backwardResults = new ArrayList<>();
        String[] hypotheses = {"Overheat", "BearingFault", "ElectricalOverload"};
        List<Machine> machines = List.of(m1, m2, m3);

        for (Machine m : machines) {
            for (String h : hypotheses) {
                // Query Drools for the hypothesis on this machine
                QueryResults results = ksession.getQueryResults("proveHypothesis", h, m.getId());
                boolean proven = results.size() > 0;

                // Combine recommendations into a single string for the DTO
                String recs = m.getRecommendations().isEmpty()
                        ? ""
                        : String.join(", ", m.getRecommendations());

                // --- Construct the DTO for each check ---
                BackwardResultDTO dto = new BackwardResultDTO(
                        m.getName(),
                        h,
                        proven,
                        m.getStatus().toString(),
                        recs // Combined string of recommendations
                );
                backwardResults.add(dto);
            }
        }

        int firedAfterQueries = ksession.fireAllRules();
        DroolsLog.log("=== Rules fired after query checks: " + firedAfterQueries + " ===");

        // --- Collect results ---
        Map<String, Object> response = new LinkedHashMap<>();

        // 1. Send the DTO list under the key 'results' (Frontend expects this)
        response.put("results", backwardResults);
        // 2. Send the logs (Frontend expects this)
        response.put("logs", DroolsLog.getLogs());
        // 3. (Optional) Send the updated machine facts
        response.put("machines", machines);

        ksession.dispose();
        return response;
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
