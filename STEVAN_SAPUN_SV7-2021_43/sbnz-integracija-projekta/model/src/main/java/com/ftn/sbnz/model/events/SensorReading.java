package com.ftn.sbnz.model.events;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

// Generic sensor reading. Have to use subclasses for domain-specific fields.
public abstract class SensorReading implements Serializable {
    private String machineId;
    private double value;
    // Was instant, now is Date because Drools CEP only supports java.util.Date or long for the @timestamp field
    private Date timestamp; //private Instant timestamp;

    protected SensorReading() { this.timestamp = new Date(); }

    protected SensorReading(String machineId, double value, Date timestamp) {
        this.machineId = machineId;
        this.value = value;
        this.timestamp = timestamp == null ? new Date() : timestamp;
    }

    public String getMachineId() { return machineId; }
    public void setMachineId(String machineId) { this.machineId = machineId; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
