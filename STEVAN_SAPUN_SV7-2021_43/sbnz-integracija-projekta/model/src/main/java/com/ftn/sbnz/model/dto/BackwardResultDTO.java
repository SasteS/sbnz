package com.ftn.sbnz.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; // Good practice to include this too

@Data
@AllArgsConstructor
@NoArgsConstructor // Add a no-arg constructor for potential serialization issues
public class BackwardResultDTO {
    private String machineName;
    private String hypothesis;
    private boolean proven;
    private String status;
    private String recommendations; // This will hold the combined recommendations
}