package com.ftn.sbnz.model.models;

import java.util.Map;

public interface IDiagnosticService {
    // For the REST Controller (Manual)
    Map<String, Object> runBackwardChainingForOne(String machineId, String hypothesis, double threshold) throws Exception;

    // For the Drools Rules (Automatic) - No "throws", no "return"
    void triggerAutomaticDiagnosis(String machineId, String hypothesis, double threshold) throws Exception;
}