package com.ftn.sbnz.model.events;

import com.ftn.sbnz.model.enums.EventType;

import java.io.Serializable;
import java.time.Instant;

// Discrete events from PLC/SCADA (Start/Stop/Overload/ MaintenanceAction).
public class MachineEvent implements Serializable {
    private String machineId;
    private EventType type;
    private Instant timestamp;
    private String details;

    public MachineEvent() { this.timestamp = Instant.now(); }

    public MachineEvent(String machineId, EventType type, Instant timestamp, String details) {
        this.machineId = machineId;
        this.type = type;
        this.timestamp = timestamp == null ? Instant.now() : timestamp;
        this.details = details;
    }

    public String getMachineId() { return machineId; }
    public void setMachineId(String machineId) { this.machineId = machineId; }

    public EventType getType() { return type; }
    public void setType(EventType type) { this.type = type; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    @Override
    public String toString() {
        return "MachineEvent{" +
                "machineId='" + machineId + '\'' +
                ", type=" + type +
                ", timestamp=" + timestamp +
                ", details='" + details + '\'' +
                '}';
    }
}
