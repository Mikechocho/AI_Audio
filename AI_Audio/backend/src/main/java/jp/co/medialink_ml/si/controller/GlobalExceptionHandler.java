package jp.co.medialink_ml.si.controller;

import java.util.Map;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

/**
 * REST API共通の例外ハンドラ
 *
 * コントローラで発生した例外をまとめて処理し、
 * フロントエンドが扱いやすいJSON（messageフィールド）で返します。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * データベースに接続できない場合のハンドリング
	 * @param e 接続失敗に起因する例外
	 * @return 503 Service Unavailable ＋ エラーメッセージ（JSON）
	 */
	@ExceptionHandler({ CannotCreateTransactionException.class, DataAccessResourceFailureException.class })
	public ResponseEntity<Map<String, String>> handleDatabaseConnectionError(Exception e) {
		log.warn("データベースに接続できませんでした: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body(Map.of("message",
						"データベースに接続できません。application.properties の接続先と .env の DB_PASSWORD を確認してください。"));
	}

	/**
	 * その他の予期しない例外のハンドリング
	 * @param e 発生した例外
	 * @return 500 Internal Server Error ＋ エラーメッセージ（JSON）
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handleUnexpectedError(Exception e) {
		log.error("予期しないエラーが発生しました", e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("message", "サーバ内部でエラーが発生しました: " + e.getMessage()));
	}
}
