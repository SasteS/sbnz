package com.ftn.sbnz.model.models;

import java.util.Map;

public interface IDiagnosticService {
    // For the REST Controller (Manual)
    Map<String, Object> runBackwardChainingForOne(Machine machine, String hypothesis, double threshold) throws Exception;

    // For the Drools Rules (Automatic) - No "throws", no "return"
    void triggerAutomaticDiagnosis(Machine machine, String hypothesis, double threshold) throws Exception;

    // For CEP Rules (triggers direct explanation)
    void triggerSimpleAlert(Machine machine, String alertTitle, String recommendation);
}