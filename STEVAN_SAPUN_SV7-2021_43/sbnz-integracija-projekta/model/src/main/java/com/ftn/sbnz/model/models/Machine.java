package com.ftn.sbnz.model.models;

import com.ftn.sbnz.model.enums.MachineStatus;
import com.ftn.sbnz.model.enums.OperationContext;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.*;

@Entity
@Table(name = "machines")
public class Machine implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid") // Reference the generator name
    @GenericGenerator(
            name = "uuid",
            // This is the Hibernate strategy for String-based UUIDs
            strategy = "uuid2"
    )
    private String id;

    @Column(nullable = false, unique = true)
    private String name;

    private double vibration;
    private double temperature;
    private double currentPercentOfRated;
    private double rpm;

    @Enumerated(EnumType.STRING)
    private MachineStatus status = MachineStatus.NORMAL;

    @Enumerated(EnumType.STRING)
    private OperationContext context = OperationContext.NORMAL;

    private Instant lastUpdated;
    private int overloadTripCount = 0;

    @ElementCollection
    private List<String> recommendations = new ArrayList<>();

    public Machine() {}

    public Machine(String id, String name) {
        this.id = id;
        this.name = name;
        this.lastUpdated = Instant.now();
    }

    public Machine(String id, String name, double vibration, double temperature, double currentPercentOfRated, double rpm, MachineStatus status, OperationContext context, Instant lastUpdated, int overloadTripCount) {
        this.id = id;
        this.name = name;
        this.vibration = vibration;
        this.temperature = temperature;
        this.currentPercentOfRated = currentPercentOfRated;
        this.rpm = rpm;
        this.status = status;
        this.context = context;
        this.lastUpdated = lastUpdated;
        this.overloadTripCount = overloadTripCount;
        this.recommendations = new ArrayList<>();
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
    public void resetRecommendations() { recommendations.clear(); }


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
