package jp.co.medialink_ml.si.config;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 起動時にデータベースへの接続を確認するチェッカー
 *
 * 接続できない場合もアプリは起動を続けます（画面表示等の動作確認は可能）。
 * その場合、データ取得・保存を行うAPIはエラーになります。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseConnectionChecker implements CommandLineRunner {

	private final DataSource dataSource;

	@Override
	public void run(String... args) {
		try (Connection connection = dataSource.getConnection()) {
			log.info("データベース接続OK: {}", connection.getMetaData().getURL());
		} catch (Exception e) {
			log.warn("=================================================================");
			log.warn("データベースに接続できませんでした。");
			log.warn("原因: {}", e.getMessage());
			log.warn("以下を確認してください:");
			log.warn("  1. application.properties の spring.datasource.url（接続先ホスト・スキーマ名）");
			log.warn("  2. backend/.env の DB_PASSWORD");
			log.warn("※ DB未接続でもアプリは起動しますが、データ取得・保存のAPIはエラーになります。");
			log.warn("=================================================================");
		}
	}
}
