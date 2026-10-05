package jp.co.medialink_ml.si.repository;

import jp.co.medialink_ml.si.entity.AudioTranscriptionSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AudioTranscriptionSummaryRepository extends JpaRepository<AudioTranscriptionSummary, Long> {
    
    // 論理削除されていないデータの一覧を取得
    List<AudioTranscriptionSummary> findByIsLogicalDeletedFalse();
    
    // 論理削除されていない単件データを取得
    Optional<AudioTranscriptionSummary> findByTranscriptionIdAndIsLogicalDeletedFalse(Long id);
}