package com.ftn.sbnz.model.enums;


// Operational context for rules (K in specification).
public enum OperationContext {
    NORMAL("Normal"),
    HIGH_LOAD("HighLoad"),
    POST_MAINTENANCE("PostMaintenance"),
    IDLE("Idle");

    private final String label;

    OperationContext(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
