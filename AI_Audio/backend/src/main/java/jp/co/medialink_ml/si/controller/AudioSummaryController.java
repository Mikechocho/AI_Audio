package jp.co.medialink_ml.si.controller;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jp.co.medialink_ml.si.entity.AudioTranscriptionSummary;
import jp.co.medialink_ml.si.service.AudioSummaryService;

@RestController
@RequestMapping("/api")
public class AudioSummaryController {

    private final AudioSummaryService audioSummaryService;

    public AudioSummaryController(AudioSummaryService audioSummaryService) {
        this.audioSummaryService = audioSummaryService;
    }

    @GetMapping("/summaries")
    public List<AudioTranscriptionSummary> getSummaries() {
        return audioSummaryService.getAllSummaries();
    }

    @GetMapping("/summaries/{id}")
    public ResponseEntity<AudioTranscriptionSummary> getById(@PathVariable Long id) {
        return ResponseEntity.ok(audioSummaryService.getSummaryById(id));
    }

    // URLパラメータ(?filename=...) と JSONボディ({"filename":"..."}) の両方に対応
    @PostMapping("/summaries/process")
    public ResponseEntity<AudioTranscriptionSummary> processAndSave(
            @RequestParam(value = "filename", required = false) String filenameParam,
            @RequestBody(required = false) Map<String, String> body) {

        String filename = filenameParam;

        if ((filename == null || filename.trim().isEmpty()) && body != null) {
            filename = body.get("filename");
        }

        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("filename が指定されていません。");
        }

        AudioTranscriptionSummary result = audioSummaryService.processAndSave(filename);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/summaries/{id}")
    public ResponseEntity<AudioTranscriptionSummary> update(
            @PathVariable Long id,
            @RequestBody AudioTranscriptionSummary updateData) {
        return ResponseEntity.ok(audioSummaryService.updateSummary(id, updateData));
    }

    @DeleteMapping("/summaries/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        audioSummaryService.deleteSummary(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/audio-files")
    public String[] getAudioFiles() {
        File folder = new File("src/main/resources/static/audio");
        String[] files = folder.list((dir, name) -> name.endsWith(".wav"));
        return files != null ? files : new String[0];
    }
}