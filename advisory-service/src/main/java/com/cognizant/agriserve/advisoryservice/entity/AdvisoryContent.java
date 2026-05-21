package com.cognizant.agriserve.advisoryservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "advisoryContent")
public class AdvisoryContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contentId;
    @NotBlank(message = "Title is mandatory")
    @Column(nullable = false)
    private String title;
//    @ManyToOne
//    @JoinColumn(name ="uploaded_By") // Database column name [cite: 89]
    @NotNull(message = "Uploader is required") // Use @NotNull for Objects [cite: 31]
    private Long uploadedBy; // Java field name [cite: 32]
    @NotBlank(message = "Category is mandatory")
    private String category;
    private String fileUri;
    private String description;
    private LocalDateTime uploadedDate = LocalDateTime.now();
    private String status = "Active";
}