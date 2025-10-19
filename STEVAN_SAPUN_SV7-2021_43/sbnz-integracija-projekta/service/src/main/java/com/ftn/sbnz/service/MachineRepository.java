package com.ftn.sbnz.service;

import com.ftn.sbnz.model.models.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MachineRepository extends JpaRepository<Machine, String> {
}
