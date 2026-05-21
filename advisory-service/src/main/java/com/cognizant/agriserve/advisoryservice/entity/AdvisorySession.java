package com.cognizant.agriserve.advisoryservice.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "advisorySession")
public class AdvisorySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

//    @NotNull(message = "Officer is required")
//    @ManyToOne(optional = false)
//    @JoinColumn(name = "officer_id", referencedColumnName = "userId", nullable = false) // Assuming User has userId
    private Long officerId;

//    @NotNull(message = "Farmer is required")
//    @ManyToOne(optional = false)
//It tells Hibernate, "This relationship is mandatory." If you try to save an AdvisorySession without a Farmer, Hibernate will catch it before even trying to talk to the database.
//    @JoinColumn(name = "farmer_id", nullable = false)// Assuming Farmer has farmerId
    private Long farmerId;

    @NotNull(message = "Advisory content is required")
    @ManyToOne(optional = false)
    @JoinColumn(name = "content_id", nullable = false)
    private AdvisoryContent content;

    private LocalDateTime date = LocalDateTime.now();
    private String status = "Completed";

    @NotBlank(message = "Consultation feedback is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String feedback;

}