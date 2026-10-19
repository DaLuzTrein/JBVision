package com.JBVision.dto;
 
import java.util.List;
 
// DTO para receber a lista de subjects cadastrados no CompreFace
// Resposta do GET /api/v1/recognition/faces/subjects
public class SubjectListResponse {
 
    private List<String> subjects;
 
    public List<String> getSubjects() { return subjects; }
    public void setSubjects(List<String> subjects) { this.subjects = subjects; }
}
 