package com.cognizant.agriserve.trainingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "workshop")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Workshop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workshopId;

    @Column(nullable = false, length = 150)
    private String title;

    @ManyToOne
    @JoinColumn(name = "programId")
    private TrainingProgram trainingProgram;

    private Long officerId;

    private String location;
    private LocalDateTime date;
    private String status;

    @OneToMany(mappedBy = "workshop", cascade = CascadeType.ALL)
    private List<Participation> participation;
}