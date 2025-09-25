package com.ftn.sbnz.model.events;

import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Role(Role.Type.EVENT)
@Timestamp("timestamp")
@Expires("1m")
// Vibration reading: value is RMS (mm/s). Optionally carry spectral data.
public class VibrationReading extends SensorReading {
    // RMS in mm/s
    public VibrationReading() { super(); }

    public VibrationReading(String machineId, double rms, Date timestamp) {
        super(machineId, rms, timestamp);
    }

    // optional: spectrum (frequency -> amplitude)
    private Map<Double, Double> spectrum;

    public Map<Double, Double> getSpectrum() { return spectrum; }
    public void setSpectrum(Map<Double, Double> spectrum) { this.spectrum = spectrum; }
}
