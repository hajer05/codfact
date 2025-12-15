package com.example.learning_service.service;

import com.example.learning_service.dto.ConsultingRequestDto;
import com.example.learning_service.dto.CreateConsultingRequest;
import com.example.learning_service.dto.UpdateConsultingRequest;
import com.example.learning_service.entity.ConsultingRequest;
import com.example.learning_service.entity.User;
import com.example.learning_service.repository.ConsultingRequestRepository;
import com.example.learning_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ConsultingService {
    
    private final ConsultingRequestRepository consultingRequestRepository;
    private final UserRepository userRepository;
    
    public ConsultingRequestDto createRequest(CreateConsultingRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        ConsultingRequest consultingRequest = new ConsultingRequest();
        consultingRequest.setCompanyName(request.getCompanyName());
        consultingRequest.setContactName(request.getContactName());
        consultingRequest.setEmail(request.getEmail());
        consultingRequest.setPhone(request.getPhone());
        consultingRequest.setServiceType(request.getServiceType());
        consultingRequest.setProjectDescription(request.getProjectDescription());
        consultingRequest.setNeeds(request.getNeeds());
        consultingRequest.setBudget(request.getBudget());
        consultingRequest.setTimeline(request.getTimeline());
        consultingRequest.setStatus(ConsultingRequest.ConsultingStatus.PENDING);
        consultingRequest.setRequestedBy(user);
        
        ConsultingRequest saved = consultingRequestRepository.save(consultingRequest);
        return convertToDto(saved);
    }
    
    public List<ConsultingRequestDto> getAllRequests() {
        return consultingRequestRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<ConsultingRequestDto> getRequestsByUser(Long userId) {
        return consultingRequestRepository.findByRequestedByIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<ConsultingRequestDto> getRequestsByStatus(ConsultingRequest.ConsultingStatus status) {
        return consultingRequestRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<ConsultingRequestDto> getRequestsAssignedToConsultant(Long consultantId) {
        return consultingRequestRepository.findByAssignedToIdOrderByCreatedAtDesc(consultantId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public ConsultingRequestDto updateRequest(Long requestId, UpdateConsultingRequest request) {
        ConsultingRequest consultingRequest = consultingRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Consulting request not found"));
        
        if (request.getStatus() != null) {
            consultingRequest.setStatus(request.getStatus());
        }
        
        if (request.getAdminNotes() != null) {
            consultingRequest.setAdminNotes(request.getAdminNotes());
        }
        
        if (request.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            consultingRequest.setAssignedTo(assignedTo);
        }
        
        if (request.getStatus() == ConsultingRequest.ConsultingStatus.ANSWERED ||
            request.getStatus() == ConsultingRequest.ConsultingStatus.CLOSED) {
            consultingRequest.setAnsweredAt(LocalDateTime.now());
        }
        
        ConsultingRequest saved = consultingRequestRepository.save(consultingRequest);
        return convertToDto(saved);
    }
    
    public ConsultingRequestDto getRequestById(Long requestId) {
        ConsultingRequest request = consultingRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Consulting request not found"));
        return convertToDto(request);
    }
    
    public Long getUserIdByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.map(User::getId).orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    private ConsultingRequestDto convertToDto(ConsultingRequest request) {
        ConsultingRequestDto dto = new ConsultingRequestDto();
        dto.setId(request.getId());
        dto.setCompanyName(request.getCompanyName());
        dto.setContactName(request.getContactName());
        dto.setEmail(request.getEmail());
        dto.setPhone(request.getPhone());
        dto.setServiceType(request.getServiceType());
        dto.setProjectDescription(request.getProjectDescription());
        dto.setNeeds(request.getNeeds());
        dto.setBudget(request.getBudget());
        dto.setTimeline(request.getTimeline());
        dto.setStatus(request.getStatus());
        dto.setAdminNotes(request.getAdminNotes());
        
        if (request.getRequestedBy() != null) {
            dto.setRequestedById(request.getRequestedBy().getId());
            dto.setRequestedByName(request.getRequestedBy().getFirstName() + " " + request.getRequestedBy().getLastName());
        }
        
        if (request.getAssignedTo() != null) {
            dto.setAssignedToId(request.getAssignedTo().getId());
            dto.setAssignedToName(request.getAssignedTo().getFirstName() + " " + request.getAssignedTo().getLastName());
        }
        
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());
        dto.setAnsweredAt(request.getAnsweredAt());
        
        return dto;
    }
}

