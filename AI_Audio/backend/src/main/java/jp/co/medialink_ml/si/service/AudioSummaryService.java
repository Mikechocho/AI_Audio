package jp.co.medialink_ml.si.service;

import jp.co.medialink_ml.si.config.AppPropertyConfig;
import jp.co.medialink_ml.si.entity.AudioTranscriptionSummary;
import jp.co.medialink_ml.si.repository.AudioTranscriptionSummaryRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AudioSummaryService {

    private final AudioTranscriptionSummaryRepository repository;
    private final AppPropertyConfig appPropertyConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    public AudioSummaryService(AudioTranscriptionSummaryRepository repository, AppPropertyConfig appPropertyConfig) {
        this.repository = repository;
        this.appPropertyConfig = appPropertyConfig;
    }

    // --- CRUD機能 ---

    public List<AudioTranscriptionSummary> getAllSummaries() {
        return repository.findByIsLogicalDeletedFalse();
    }

    public AudioTranscriptionSummary getSummaryById(Long id) {
        return repository.findByTranscriptionIdAndIsLogicalDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("指定されたデータが見つかりません。ID: " + id));
    }

    public AudioTranscriptionSummary updateSummary(Long id, AudioTranscriptionSummary updateData) {
        AudioTranscriptionSummary existing = getSummaryById(id);
        existing.setSummaryText(updateData.getSummaryText());
        existing.setExtractedName(updateData.getExtractedName());
        existing.setExtractedPhone(updateData.getExtractedPhone());
        existing.setExtractedSubject(updateData.getExtractedSubject());
        existing.setUpdatedAt(LocalDateTime.now());
        return repository.save(existing);
    }

    public void deleteSummary(Long id) {
        AudioTranscriptionSummary existing = getSummaryById(id);
        existing.setIsLogicalDeleted(true);
        existing.setUpdatedAt(LocalDateTime.now());
        repository.save(existing);
    }

    // --- パイプライン処理 (POST API) ---

    public AudioTranscriptionSummary processAndSave(String filename) {
        String transcriptionText = transcribeAudio(filename);
        JSONObject aiResult = summarizeAndExtract(transcriptionText);

        LocalDateTime now = LocalDateTime.now();
        AudioTranscriptionSummary entity = new AudioTranscriptionSummary();
        entity.setAudioFileName(filename);
        entity.setTranscriptionText(transcriptionText);
        entity.setSummaryText(aiResult.optString("summary", ""));
        entity.setExtractedName(aiResult.isNull("name") ? null : aiResult.optString("name", null));
        entity.setExtractedPhone(aiResult.isNull("phone") ? null : aiResult.optString("phone", null));
        entity.setExtractedSubject(aiResult.isNull("subject") ? null : aiResult.optString("subject", null));

        // ToDoリストのセット
        JSONArray todosArray = aiResult.optJSONArray("todos");
        String todoListStr = (todosArray != null) ? todosArray.toString() : "[]";
        entity.setTodoList(todoListStr);

        // タグのセット
        JSONArray tagsArray = aiResult.optJSONArray("tags");
        String tagsStr = (tagsArray != null) ? tagsArray.toString() : "[]";
        entity.setTags(tagsStr);

        entity.setIsLogicalDeleted(false);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        return repository.save(entity);
    }

    // --- AmiVoice 呼び出し ---
    private String transcribeAudio(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("リクエストエラー: ファイル名(filename)が指定されていません。");
        }

        String audioDir = appPropertyConfig.getAudioDir();
        if (audioDir == null || audioDir.trim().isEmpty()) {
            audioDir = "src/main/resources/static/audio/"; 
        }

        Path filePath = Paths.get(audioDir, filename);
        File audioFile = filePath.toFile();

        if (!audioFile.exists()) {
            throw new RuntimeException("音声ファイルが見つかりません: " + audioFile.getAbsolutePath());
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("d", "-a-general");
        body.add("u", appPropertyConfig.getAmivoiceApiKey());
        body.add("a", new FileSystemResource(audioFile));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<byte[]> response = restTemplate.postForEntity(
            appPropertyConfig.getAmivoiceEndpoint(),
            new HttpEntity<>(body, headers),
            byte[].class
        );

        String responseBody = (response.getBody() != null) 
            ? new String(response.getBody(), StandardCharsets.UTF_8) 
            : "";
        String trimmed = responseBody.trim();

        if (trimmed.startsWith("{")) {
            JSONObject json = new JSONObject(trimmed);
            return json.optString("text", trimmed);
        }

        return trimmed;
    }

    // --- Gemini 呼び出し ---
    private JSONObject summarizeAndExtract(String transcriptionText) {
        String prompt = "以下の通話内容から氏名・電話番号・用件、発生した「やるべきこと（ToDo）」のリスト、および通話内容を表す短いタグ（例: クレーム, 見積依頼, 営業など）を1〜3個抽出してください。"
                + "各ToDoの重要度は会話の緊急性から推測し「高」「中」「低」で判定してください。"
                + "該当がない項目はnullまたは空配列にしてください。\n\n" + transcriptionText;

        JSONObject schema = new JSONObject()
                .put("type", "object")
                .put("properties", new JSONObject()
                        .put("summary", new JSONObject().put("type", "string"))
                        .put("name", new JSONObject().put("type", "string").put("nullable", true))
                        .put("phone", new JSONObject().put("type", "string").put("nullable", true))
                        .put("subject", new JSONObject().put("type", "string").put("nullable", true))
                        .put("tags", new JSONObject()
                                .put("type", "array")
                                .put("description", "通話内容の分類タグ（1〜3個）")
                                .put("items", new JSONObject().put("type", "string")))
                        .put("todos", new JSONObject()
                                .put("type", "array")
                                .put("description", "やるべきこと（ToDo）のリスト")
                                .put("items", new JSONObject()
                                        .put("type", "object")
                                        .put("properties", new JSONObject()
                                                .put("task", new JSONObject()
                                                        .put("type", "string")
                                                        .put("description", "タスクの具体的な内容"))
                                                .put("priority", new JSONObject()
                                                        .put("type", "string")
                                                        .put("description", "重要度（高・中・低のいずれか）"))
                                        )
                                )
                        )
                );

        JSONObject genConfig = new JSONObject()
                .put("responseMimeType", "application/json")
                .put("responseSchema", schema)
                .put("temperature", 0.0);

        JSONArray safetySettings = new JSONArray()
                .put(new JSONObject().put("category", "HARM_CATEGORY_HARASSMENT").put("threshold", "BLOCK_NONE"))
                .put(new JSONObject().put("category", "HARM_CATEGORY_HATE_SPEECH").put("threshold", "BLOCK_NONE"))
                .put(new JSONObject().put("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT").put("threshold", "BLOCK_NONE"))
                .put(new JSONObject().put("category", "HARM_CATEGORY_DANGEROUS_CONTENT").put("threshold", "BLOCK_NONE"));

        JSONArray contents = new JSONArray()
                .put(new JSONObject()
                        .put("parts", new JSONArray()
                                .put(new JSONObject().put("text", prompt))));

        JSONObject requestBody = new JSONObject();
        requestBody.put("contents", contents);
        requestBody.put("generationConfig", genConfig);
        requestBody.put("safetySettings", safetySettings);

        RestTemplate restTemplateForGemini = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

        String url = appPropertyConfig.getGeminiEndpoint() + appPropertyConfig.getGeminiApiKey();

        ResponseEntity<String> response = restTemplateForGemini.postForEntity(url, request, String.class);

        JSONObject responseJson = new JSONObject(response.getBody());
        String resultText = responseJson.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");

        return new JSONObject(resultText);
    }
}