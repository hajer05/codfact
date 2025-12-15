package com.example.learning_service.repository;

import com.example.learning_service.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    List<Document> findBySubjectId(Long subjectId);
    
    List<Document> findBySubjectIdOrderByUploadDateDesc(Long subjectId);
    
    List<Document> findByUploadedByIdOrderByUploadDateDesc(Long uploadedById);
    
    List<Document> findBySubjectIdAndDocumentTypeOrderByUploadDateDesc(Long subjectId, Document.DocumentType documentType);
    
    List<Document> findByDocumentTypeOrderByUploadDateDesc(Document.DocumentType documentType);
}
