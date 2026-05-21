package com.cognizant.agriserve.trainingservice.dao;

import com.cognizant.agriserve.trainingservice.entity.TrainingProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingProgramRepository extends JpaRepository<TrainingProgram, Long> {


    List<TrainingProgram> findByStatus(String status);
}
