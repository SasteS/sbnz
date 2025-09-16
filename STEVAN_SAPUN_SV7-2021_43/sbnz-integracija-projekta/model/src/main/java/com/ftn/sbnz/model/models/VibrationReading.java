package com.ftn.sbnz.model.models;

import java.time.Instant;
import java.util.Map;

// Vibration reading: value is RMS (mm/s). Optionally carry spectral data.
public class VibrationReading extends SensorReading {
    // RMS in mm/s
    public VibrationReading() { super(); }

    public VibrationReading(String machineId, double rms, Instant timestamp) {
        super(machineId, rms, timestamp);
    }

    // optional: spectrum (frequency -> amplitude)
    private Map<Double, Double> spectrum;

    public Map<Double, Double> getSpectrum() { return spectrum; }
    public void setSpectrum(Map<Double, Double> spectrum) { this.spectrum = spectrum; }
}
