package jp.co.medialink_ml.si.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web設定（CORS）
 *
 * フロントエンド（Vite開発サーバ: http://localhost:5173）から
 * バックエンドAPI（http://localhost:8080）への通信を許可します。
 *
 * ※ Vite側のproxy設定（vite.config.js）を使う場合はCORSを経由しませんが、
 *   フロントエンドから直接APIを呼ぶ実装をしても動くように許可しています。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
				.allowedOrigins("http://localhost:5173")
				.allowedMethods("GET", "POST", "PUT", "DELETE");
	}
}
