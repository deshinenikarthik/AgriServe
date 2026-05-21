package com.cognizant.agriserve.trainingservice.dao;

import com.cognizant.agriserve.trainingservice.entity.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {


    List<Participation> findByWorkshop_WorkshopId(Long workshopId);

    boolean existsByWorkshop_WorkshopIdAndFarmerId(Long workshopId, Long farmerId);
    List<Participation> findByFarmerId(Long farmerId);
}

