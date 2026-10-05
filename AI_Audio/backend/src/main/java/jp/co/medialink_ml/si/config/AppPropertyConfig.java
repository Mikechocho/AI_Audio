package jp.co.medialink_ml.si.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.Getter;

@Getter
@Component
public class AppPropertyConfig {

    @Value("${gemini.endpoint}")
    private String geminiEndpoint;

    @Value("${gemini.apikey}")
    private String geminiApiKey;

    @Value("${amivoice.endpoint}")
    private String amivoiceEndpoint;

    @Value("${amivoice.apikey}")
    private String amivoiceApiKey;

    @Value("${app.audio-dir:src/main/resources/static/audio/}")
    private String audioDir;

    public String getAudioDir() {
        return audioDir;
    }
}