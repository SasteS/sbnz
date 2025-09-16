package com.ftn.sbnz.model.models;

import com.ftn.sbnz.model.enums.MachineStatus;
import com.ftn.sbnz.model.enums.OperationContext;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Aggregated machine state. This is the primary Drools fact in most rules.
public class Machine implements Serializable {
    private String id;
    private String name;

    // sensor aggregated values (simple representation)
    private double vibration;     // e.g. RMS mm/s
    private double temperature;   // °C
    private double currentPercentOfRated; // % of rated current, e.g. 110.0
    private double rpm;

    private MachineStatus status = MachineStatus.NORMAL;
    private OperationContext context = OperationContext.NORMAL;

    private Instant lastUpdated;
    private int overloadTripCount = 0;

    // recommendations / actions produced by rules
    private final List<String> recommendations = new ArrayList<>();

    public Machine() {}

    public Machine(String id, String name) {
        this.id = id;
        this.name = name;
        this.lastUpdated = Instant.now();
    }

    // --- getters / setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getVibration() { return vibration; }
    public void setVibration(double vibration) {
        this.vibration = vibration;
        this.lastUpdated = Instant.now();
    }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) {
        this.temperature = temperature;
        this.lastUpdated = Instant.now();
    }

    public double getCurrentPercentOfRated() { return currentPercentOfRated; }
    public void setCurrentPercentOfRated(double currentPercentOfRated) {
        this.currentPercentOfRated = currentPercentOfRated;
        this.lastUpdated = Instant.now();
    }

    public double getRpm() { return rpm; }
    public void setRpm(double rpm) {
        this.rpm = rpm;
        this.lastUpdated = Instant.now();
    }

    public MachineStatus getStatus() { return status; }
    public void setStatus(MachineStatus status) { this.status = status; }

    public OperationContext getContext() { return context; }
    public void setContext(OperationContext context) { this.context = context; }

    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }

    public int getOverloadTripCount() { return overloadTripCount; }
    public void setOverloadTripCount(int overloadTripCount) { this.overloadTripCount = overloadTripCount; }
    public void incrementOverloadTripCount() { this.overloadTripCount++; }

    public List<String> getRecommendations() { return recommendations; }
    public void addRecommendation(String rec) { this.recommendations.add(rec); }

    // convenience helpers
    public boolean isCritical() { return this.status == MachineStatus.CRITICAL; }
    public boolean isRisky() { return this.status == MachineStatus.RISKY; }

    @Override
    public String toString() {
        return "Machine{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", vibration=" + vibration +
                ", temperature=" + temperature +
                ", currentPercentOfRated=" + currentPercentOfRated +
                ", rpm=" + rpm +
                ", status=" + status +
                ", context=" + context +
                ", lastUpdated=" + lastUpdated +
                ", overloadTripCount=" + overloadTripCount +
                ", recommendations=" + recommendations +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Machine)) return false;
        Machine machine = (Machine) o;
        return Objects.equals(id, machine.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
