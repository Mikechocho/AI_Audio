package jp.co.medialink_ml.si.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audio_transcription_summary")
public class AudioTranscriptionSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transcription_id")
    private Long transcriptionId;

    @Column(name = "audio_file_name")
    private String audioFileName;

    @Column(name = "transcription_text")
    private String transcriptionText;

    @Column(name = "summary_text")
    private String summaryText;

    @Column(name = "extracted_name")
    private String extractedName;

    @Column(name = "extracted_phone")
    private String extractedPhone;

    @Column(name = "extracted_subject")
    private String extractedSubject;

    @Column(name = "todo_list")
    private String todoList;

    @Column(name = "tags")
    private String tags;

    @Column(name = "is_logical_deleted")
    private Boolean isLogicalDeleted;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Getters & Setters ---

    public Long getTranscriptionId() { return transcriptionId; }
    public void setTranscriptionId(Long transcriptionId) { this.transcriptionId = transcriptionId; }

    public String getAudioFileName() { return audioFileName; }
    public void setAudioFileName(String audioFileName) { this.audioFileName = audioFileName; }

    public String getTranscriptionText() { return transcriptionText; }
    public void setTranscriptionText(String transcriptionText) { this.transcriptionText = transcriptionText; }

    public String getSummaryText() { return summaryText; }
    public void setSummaryText(String summaryText) { this.summaryText = summaryText; }

    public String getExtractedName() { return extractedName; }
    public void setExtractedName(String extractedName) { this.extractedName = extractedName; }

    public String getExtractedPhone() { return extractedPhone; }
    public void setExtractedPhone(String extractedPhone) { this.extractedPhone = extractedPhone; }

    public String getExtractedSubject() { return extractedSubject; }
    public void setExtractedSubject(String extractedSubject) { this.extractedSubject = extractedSubject; }

    public String getTodoList() { return todoList; }
    public void setTodoList(String todoList) { this.todoList = todoList; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public Boolean getIsLogicalDeleted() { return isLogicalDeleted; }
    public void setIsLogicalDeleted(Boolean isLogicalDeleted) { this.isLogicalDeleted = isLogicalDeleted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}