package com.cognizant.agriserve.advisoryservice.dao;

import com.cognizant.agriserve.advisoryservice.entity.AdvisorySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public interface AdvisorySessionRepository extends JpaRepository<AdvisorySession, Long> {



    // For P1A-31: Track Advisory Usage (The Manager's Report)
    // This Native Query counts sessions grouped by ContentID
    @Query(value = "SELECT content_id, COUNT(*) as usage_count FROM advisory_session GROUP BY content_id", nativeQuery = true)
    List<Map<String, Object>> getContentUsageReport();
    @Query("SELECT s FROM AdvisorySession s")
    List<AdvisorySession> getAllSessions();
    // To see which content is popular in a specific category
    @Query("SELECT s.content.title, COUNT(s) FROM AdvisorySession s GROUP BY s.content.title")
    List<Object[]> countUsageByTitle();

    List<AdvisorySession> findByFarmerId(Long farmerId);
}