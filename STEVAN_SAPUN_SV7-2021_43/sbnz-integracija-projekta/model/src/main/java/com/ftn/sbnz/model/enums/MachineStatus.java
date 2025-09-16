package com.ftn.sbnz.model.enums;


// Business-level machine status.
public enum MachineStatus {
    NORMAL("Normalna"),
    SUSPICIOUS("Sumnjiva"),
    RISKY("Rizična"),
    CRITICAL("Kritična");

    private final String label;

    MachineStatus(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
