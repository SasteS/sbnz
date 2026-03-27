package com.ftn.sbnz.service;

import com.ftn.sbnz.model.dto.BackwardResultDTO;
import com.ftn.sbnz.model.enums.MachineStatus;
import com.ftn.sbnz.model.models.DroolsLog;
import com.ftn.sbnz.model.models.IDiagnosticService;
import com.ftn.sbnz.model.models.Machine;
import org.kie.api.definition.type.FactType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BackwardRecursiveService implements IDiagnosticService {

    private final KieContainer kieContainer;

    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public BackwardRecursiveService(KieContainer kieContainer, MachineRepository machineRepository, SimpMessagingTemplate messagingTemplate) {
        this.kieContainer = kieContainer;
        this.machineRepository = machineRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void triggerAutomaticDiagnosis(Machine machine, String hypothesis, double threshold) {
        try {
            // We run the diagnosis and get the result map
            Map<String, Object> result = runBackwardChainingForOne(machine, hypothesis, threshold);
            // Push the result to the UI
            messagingTemplate.convertAndSend("/topic/diagnosis", result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void triggerSimpleAlert(Machine machine, String alertTitle, String recommendation) {
        List<String> logs = new ArrayList<>();
        logs.add(">>> CEP ENGINE ALERT <<<");
        logs.add("Temporal pattern detected: " + alertTitle);
        logs.add("Recommendation: " + recommendation);

        BackwardResultDTO dto = new BackwardResultDTO(
                machine.getName(),
                alertTitle,
                true, // CEP firing is the proof
                machine.getStatus().toString(),
                recommendation
        );

        Map<String, Object> response = new HashMap<>();
        response.put("results", List.of(dto));
        response.put("logs", logs);

        messagingTemplate.convertAndSend("/topic/diagnosis", response);
    }

//    public Map<String, Object> runBackwardChainingForOne(String machineId, String hypothesis) throws Exception {
//        DroolsLog.clear();
//        KieSession ksession = kieContainer.newKieSession("bwKsession");
//
//        Machine machine = machineRepository.findById(machineId)
//                .orElseThrow(() -> new RuntimeException("Machine not found: " + machineId));
//
//        ksession.insert(machine);
//
//        int fired = ksession.fireAllRules();
//        DroolsLog.log("=== Backward chaining complete (" + fired + " rules fired) ===");
//
//        QueryResults results = ksession.getQueryResults("proveHypothesis", hypothesis, machine.getId());
//        boolean proven = results.size() > 0;
//
//        // 4. Fire rules again to ensure any consequences of the query are processed
//        int firedAfterQueries = ksession.fireAllRules();
//        DroolsLog.log("=== Rules fired after query checks: " + firedAfterQueries + " ===");
//
//        // BEATS ME WHY THIS WORKS.
//        boolean logProvenCheck = DroolsLog.getLogs().stream().anyMatch(log -> log.contains("Backward: " + hypothesis + " proven for"));
//
//        // 5. Construct the single DTO result
//        String recs = machine.getRecommendations().isEmpty()
//                ? ""
//                : String.join(", ", machine.getRecommendations());
//
//        BackwardResultDTO dto = new BackwardResultDTO(
//                machine.getName(),
//                hypothesis,
//                logProvenCheck,// proven,
//                machine.getStatus().toString(),
//                recs
//        );
//
//
//        Map<String, Object> response = new LinkedHashMap<>();
//        response.put("results", List.of(dto));
//        response.put("logs", DroolsLog.getLogs());
//
//        ksession.dispose();
//        return response;
//    }

    public Map<String, Object> runBackwardChainingForOne(Machine machine, String hypothesis, double threshold) throws Exception {
        // 1. Thread-local log list
        List<String> currentLogs = new ArrayList<>();
        KieSession ksession = kieContainer.newKieSession("bwKsession");

        ksession.insert(machine);

        // 2. Initialize Dependency Graph (Rule in DRL)
        int fired = ksession.fireAllRules();
        currentLogs.add("=== Dependency Graph Initialized (" + fired + " rules fired) ===");

        // 3. Run Query
        QueryResults results = ksession.getQueryResults("proveHypothesis", hypothesis, machine.getId());
        boolean proven = results.size() > 0;

        // 4. Generate the Reasoning Trace using the LOCAL log list
        if (proven) {
            currentLogs.add("--- REASONING PATH FOR " + hypothesis + " ---");
            // FIXED: Passing 'currentLogs' to the correct method
            recordReasoningTrace(ksession, machine, hypothesis, 0, threshold, currentLogs);
            currentLogs.add("-------------------------------------------");
        } else {
            currentLogs.add("⚠️ Hypothesis " + hypothesis + " could not be proven for " + machine.getName());
        }

        // 5. Fire reaction rules (escalation)
        ksession.fireAllRules();

        // 6. Build DTO
        String recs = machine.getRecommendations().isEmpty() ? "" : String.join(", ", machine.getRecommendations());
        BackwardResultDTO dto = new BackwardResultDTO(
                machine.getName(),
                hypothesis,
                proven,
                machine.getStatus().toString(),
                recs
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("results", List.of(dto));
        response.put("logs", currentLogs); // Return the local logs
        response.put("machine", machine);

        ksession.dispose();
        return response;
    }


//    public Map<String, Object> runBackwardChainingExample() throws Exception {
//        DroolsLog.clear(); // 🧹 clear old logs
//        KieSession ksession = kieContainer.newKieSession("bwKsession");
//
//        // --- Example setup ---
//        Machine m1 = new Machine("M1", "Pump A");
//        m1.setTemperature(92.0);
//        m1.setVibration(5.0);
//        m1.setCurrentPercentOfRated(95.0);
//        m1.setStatus(MachineStatus.NORMAL);
//        ksession.insert(m1);
//
////        Machine m2 = new Machine("M2", "Compressor B");
////        m2.setTemperature(90.0);
////        m2.setVibration(8.0);
////        m2.setCurrentPercentOfRated(90.0);
////        m2.setStatus(MachineStatus.NORMAL);
////        ksession.insert(m2);
////
////        Machine m3 = new Machine("M3", "Motor C");
////        m3.setTemperature(60.0);
////        m3.setVibration(5.0);
////        m3.setCurrentPercentOfRated(130.0);
////        m3.setOverloadTripCount(2);
////        m3.setStatus(MachineStatus.NORMAL);
////        ksession.insert(m3);
//
//        // --- Run reasoning ---
//        int fired = ksession.fireAllRules();
//        DroolsLog.log("=== Backward chaining complete (" + fired + " rules fired) ===");
//
//        List<BackwardResultDTO> backwardResults = new ArrayList<>();
//        String[] hypotheses = {"Overheat"};//, "BearingFault", "ElectricalOverload"};
//        List<Machine> machines = List.of(m1); //, m2, m3);
//
//        for (Machine m : machines) {
//            for (String h : hypotheses) {
//                // Query Drools for the hypothesis on this machine
//                QueryResults results = ksession.getQueryResults("proveHypothesis", h, m.getId());
//                boolean proven = results.size() > 0;
//
//                // Combine recommendations into a single string for the DTO
//                String recs = m.getRecommendations().isEmpty()
//                        ? ""
//                        : String.join(", ", m.getRecommendations());
//
//                // --- Construct the DTO for each check ---
//                BackwardResultDTO dto = new BackwardResultDTO(
//                        m.getName(),
//                        h,
//                        proven,
//                        m.getStatus().toString(),
//                        recs // Combined string of recommendations
//                );
//                backwardResults.add(dto);
//            }
//        }
//
//        int firedAfterQueries = ksession.fireAllRules();
//        DroolsLog.log("=== Rules fired after query checks: " + firedAfterQueries + " ===");
//
//        // --- Collect results ---
//        Map<String, Object> response = new LinkedHashMap<>();
//
//        // 1. Send the DTO list under the key 'results' (Frontend expects this)
//        response.put("results", backwardResults);
//        // 2. Send the logs (Frontend expects this)
//        response.put("logs", DroolsLog.getLogs());
//        // 3. (Optional) Send the updated machine facts
//        response.put("machines", machines);
//
//        ksession.dispose();
//        return response;
//    }







//public Map<String, Object> runBackwardChainingExample() throws Exception {
//    DroolsLog.clear();
//    KieSession ksession = kieContainer.newKieSession("bwKsession");
//
//    // 1. Setup Data
//    Machine m1 = new Machine("M1", "Pump A");
//    m1.setTemperature(92.0);
//    m1.setVibration(8.0);
//    m1.setCurrentPercentOfRated(95.0);
//    m1.setStatus(MachineStatus.NORMAL);
//    ksession.insert(m1);
//
//    // 2. Initialize the Dependency Graph (the "Init" rule)
//    int initialFired = ksession.fireAllRules();
//    DroolsLog.log("System initialized. Rules fired: " + initialFired);
//
//    // 3. Define what we are looking for
//    String[] hypotheses = {"Overheat", "BearingFault", "ElectricalOverload", "AtRisk"};
//    List<Machine> machines = List.of(m1);
//
//    // This map stores the result of the query itself
//    Map<String, Boolean> queryResultsMap = new HashMap<>();
//
//    // 4. Run Queries
//    for (Machine m : machines) {
//        for (String h : hypotheses) {
//            QueryResults results = ksession.getQueryResults("proveHypothesis", h, m.getId());
//            boolean isProven = results.size() > 0;
//            queryResultsMap.put(m.getId() + h, isProven);
//
//            if (isProven) {
//                DroolsLog.log("--- REASONING PATH FOR " + h + " ---");
//                recordReasoningTrace(ksession, m, h, 0, );
//                DroolsLog.log("-------------------------------------------");
//            }
//        }
//    }
//
//    // 5. Fire Rules AGAIN
//    // This executes the "Overheat proven -> escalate" rules that were activated by the queries
//    int reactionFired = ksession.fireAllRules();
//    DroolsLog.log("=== Backward reasoning reaction complete (" + reactionFired + " rules fired) ===");
//
//    // 6. Build DTOs (Now that Machine objects have been updated by rules)
//    List<BackwardResultDTO> backwardResults = new ArrayList<>();
//    for (Machine m : machines) {
//        for (String h : hypotheses) {
//            boolean proven = queryResultsMap.getOrDefault(m.getId() + h, false);
//
//            String recs = (m.getRecommendations() == null || m.getRecommendations().isEmpty())
//                    ? ""
//                    : String.join(", ", m.getRecommendations());
//
//            backwardResults.add(new BackwardResultDTO(
//                    m.getName(),
//                    h,
//                    proven,
//                    m.getStatus().toString(),
//                    recs
//            ));
//        }
//    }
//
//    // 7. Collect results
//    Map<String, Object> response = new LinkedHashMap<>();
//    response.put("results", backwardResults);
//    response.put("logs", DroolsLog.getLogs());
//    response.put("machines", machines);
//
//    ksession.dispose();
//    return response;
//}







//    // -----------------------------------------------------------------
//    // Utility: Print dependency tree recursively
//    // -----------------------------------------------------------------
//    public static void printDependencyTree(KieSession ksession, String root) throws Exception {
//        FactType depType = ksession.getKieBase().getFactType("rules.backward", "HypothesisDependency");
//
//        System.out.println("\n📚 Dependency tree for: " + root);
//        printNode(ksession, root, 0, depType);
//    }
//
//    private static void printNode(KieSession ksession, String node, int indent, FactType depType) throws Exception {
//        for (int i = 0; i < indent; i++) System.out.print("  ");
//        System.out.println("• " + node);
//
//        for (Object fact : ksession.getObjects()) {
//            if (!depType.getFactClass().isInstance(fact)) continue;
//
//            String conclusion = (String) depType.get(fact, "conclusion");
//            if (!conclusion.equals(node)) continue;
//
//            List<?> prerequisites = (List<?>) depType.get(fact, "prerequisites");
//            for (Object prereqObj : prerequisites) {
//                String prereq = (String) prereqObj;
//                printNode(ksession, prereq, indent + 1, depType);
//            }
//        }
//    }
//}











    private void recordReasoningTrace(KieSession ksession, Machine m, String node, int indent, double threshold, List<String> logs) throws Exception {
        String padding = "  ".repeat(indent);
        FactType depType = ksession.getKieBase().getFactType("rules.backward", "HypothesisDependency");

        // 1. Check if Base Case
        String baseReason = getBaseConditionReason(m, node, threshold);
        if (baseReason != null) {
            logs.add(padding + "✅ " + node + " is TRUE because: " + baseReason);
            return;
        }

        // 2. Look for dependencies
        boolean foundDep = false;
        for (Object fact : ksession.getObjects()) {
            // Using class name check to avoid ClassLoader/FactType issues in multi-module setups
            if (!fact.getClass().getSimpleName().equals("HypothesisDependency")) continue;

            String conclusion = (String) depType.get(fact, "conclusion");
            if (!conclusion.equals(node)) continue;

            foundDep = true;
            List<?> prerequisites = (List<?>) depType.get(fact, "prerequisites");
            logs.add(padding + "📂 Proving " + node + " via subgoals: " + prerequisites);

            for (Object prereqObj : prerequisites) {
                recordReasoningTrace(ksession, m, (String) prereqObj, indent + 1, threshold, logs);
            }
        }

        if (!foundDep) {
            logs.add(padding + "❌ " + node + " has no further dependencies in the graph.");
        }
    }

    // This helper must mirror the thresholds you have in your DRL Base Cases
    private String getBaseConditionReason(Machine m, String hypothesis, double threshold) {
        switch (hypothesis) {
            case "AskableVibration":
                // If the rule fired because of vibration, 'threshold' is the vibration limit
                return (m.getVibration() > threshold) ?
                        String.format("Vibration (%.2f) > limit (%.2f)", m.getVibration(), threshold) : null;

            case "AskableTemperature":
                // If the rule fired because of temp, 'threshold' is the temp limit
                return (m.getTemperature() > threshold) ?
                        String.format("Temperature (%.2f) > limit (%.2f)", m.getTemperature(), threshold) : null;

            case "AskableCurrent":
                // 'threshold' here is 120.0
                return (m.getCurrentPercentOfRated() > threshold) ?
                        String.format("Current load (%.2f%%) > limit (%.2f%%)", m.getCurrentPercentOfRated(), threshold) : null;

            case "AskableOverload":
                // Overload logic usually looks at the count
                return (m.getOverloadTripCount() >= 1) ?
                        "Machine has recent overload trip history" : null;

            default:
                return null;
        }
    }
}
