package com.ftn.sbnz.model.models;

import java.io.Serializable;
import java.time.Instant;

// Generic sensor reading. Have to use subclasses for domain-specific fields.
public abstract class SensorReading implements Serializable {
    private String machineId;
    private double value;
    private Instant timestamp;

    protected SensorReading() { this.timestamp = Instant.now(); }

    protected SensorReading(String machineId, double value, Instant timestamp) {
        this.machineId = machineId;
        this.value = value;
        this.timestamp = timestamp == null ? Instant.now() : timestamp;
    }

    public String getMachineId() { return machineId; }
    public void setMachineId(String machineId) { this.machineId = machineId; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
