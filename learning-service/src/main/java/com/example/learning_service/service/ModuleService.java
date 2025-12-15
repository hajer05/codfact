package com.example.learning_service.service;

import com.example.learning_service.dto.ModuleRequest;
import com.example.learning_service.entity.Course;
import com.example.learning_service.entity.Module;
import com.example.learning_service.repository.CourseRepository;
import com.example.learning_service.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ModuleService {
    
    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    
    public Module createModule(ModuleRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        Module module = new Module();
        module.setTitle(request.getTitle());
        module.setDescription(request.getDescription());
        module.setCourse(course);
        
        // Set order index
        if (request.getOrderIndex() != null) {
            module.setOrderIndex(request.getOrderIndex());
        } else {
            Long moduleCount = moduleRepository.countByCourseId(request.getCourseId());
            module.setOrderIndex(moduleCount.intValue());
        }
        
        return moduleRepository.save(module);
    }
    
    public Module updateModule(Long id, ModuleRequest request) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Module not found"));
        
        module.setTitle(request.getTitle());
        module.setDescription(request.getDescription());
        
        if (request.getOrderIndex() != null) {
            module.setOrderIndex(request.getOrderIndex());
        }
        
        return moduleRepository.save(module);
    }
    
    public void deleteModule(Long id) {
        moduleRepository.deleteById(id);
    }
    
    public List<Module> getModulesByCourse(Long courseId) {
        return moduleRepository.findByCourseIdOrderByOrderIndex(courseId);
    }
    
    public Module getModuleById(Long id) {
        return moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Module not found"));
    }
}
