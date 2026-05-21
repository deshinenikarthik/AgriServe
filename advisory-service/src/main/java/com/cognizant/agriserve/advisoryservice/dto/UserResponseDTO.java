package com.cognizant.agriserve.advisoryservice.dto;

import lombok.Data;

@Data
public class UserResponseDTO {
    public enum Role {
        Admin, ExtensionOfficer, ComplianceOfficer, Farmer, Auditor, ProgramManager
    }

    private Long userId;
    private String name;
    private Role role;
    private String email;
    private String phone;
    private String status;

}