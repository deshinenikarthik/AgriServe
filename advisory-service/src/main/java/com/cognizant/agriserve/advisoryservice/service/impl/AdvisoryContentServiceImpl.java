package com.cognizant.agriserve.advisoryservice.service.impl;

import com.cognizant.agriserve.advisoryservice.dao.AdvisoryContentRepository;
import com.cognizant.agriserve.advisoryservice.dto.AdvisoryContentRequestDTO;
import com.cognizant.agriserve.advisoryservice.dto.AdvisoryContentResponseDTO;
import com.cognizant.agriserve.advisoryservice.entity.AdvisoryContent;
import com.cognizant.agriserve.advisoryservice.exception.ResourceNotFoundException;
import com.cognizant.agriserve.advisoryservice.exception.UnauthorizedActionException;
import com.cognizant.agriserve.advisoryservice.service.AdvisoryContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdvisoryContentServiceImpl implements AdvisoryContentService {

    private final AdvisoryContentRepository contentRepo;
    private final ModelMapper modelMapper;

    @Override
    public AdvisoryContentResponseDTO saveContent(AdvisoryContentRequestDTO requestDto, Long uploaderId) {

        // Note: The role check ("Is this an Admin or ProgramManager?") is now handled
        // by the @PreAuthorize annotation in your Controller!

        // 1. Convert DTO to Entity
        AdvisoryContent content = modelMapper.map(requestDto, AdvisoryContent.class);

        // 2. Set default server-side values
        content.setStatus("Active");
        content.setUploadedDate(LocalDateTime.now());

        // 3. Securely set the uploader ID directly from the Gateway header
        content.setUploadedBy(uploaderId);

        // 4. Save to database
        AdvisoryContent saved = contentRepo.save(content);
        log.info("Advisory Content ID {} saved successfully by User ID {}", saved.getContentId(), uploaderId);

        return modelMapper.map(saved, AdvisoryContentResponseDTO.class);
    }

    @Override
    public List<AdvisoryContentResponseDTO> getAllActiveContent() {
        return contentRepo.findByStatus("Active").stream()
                .map(content -> modelMapper.map(content, AdvisoryContentResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void softDeleteContent(Long id, Long requesterId, boolean isAdmin) {

        AdvisoryContent content = contentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Advisory Content not found ID: " + id));

        // OWNERSHIP CHECK: Admins can delete anything. Others can only delete their own content.
        if (!isAdmin) {
            if (content.getUploadedBy() == null || !content.getUploadedBy().equals(requesterId)) {
                log.warn("SECURITY BLOCKED: User ID {} attempted to delete Content ID {}", requesterId, id);
                throw new UnauthorizedActionException("Access Denied: You can only delete your own content.");
            }
        }

        content.setStatus("Inactive");
        contentRepo.save(content);
        log.info("Advisory Content ID {} was soft-deleted by User ID {} (Admin: {})", id, requesterId, isAdmin);
    }
}