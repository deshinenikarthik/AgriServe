package com.cognizant.agriserve.trainingservice.config;

import com.cognizant.agriserve.trainingservice.dto.response.WorkshopResponseDTO;
import com.cognizant.agriserve.trainingservice.entity.Workshop;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setAmbiguityIgnored(true);

        // THE NUCLEAR OPTION: Explicitly tell ModelMapper to skip these fields
        modelMapper.typeMap(Workshop.class, WorkshopResponseDTO.class).addMappings(mapper -> {
            mapper.skip(WorkshopResponseDTO::setProgramId);
            mapper.skip(WorkshopResponseDTO::setProgramTitle);
        });

        return modelMapper;
    }
}