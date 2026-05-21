package com.cognizant.agriserve.trainingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "trainingProgram")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainingProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long programId;

    @Column(nullable = false)
    private String title;

    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;


    @Column(name = "manager_id", nullable = false)
    private Long managerId;


    @OneToMany(mappedBy = "trainingProgram", cascade = CascadeType.ALL)
    private List<Workshop> workshops;
}
