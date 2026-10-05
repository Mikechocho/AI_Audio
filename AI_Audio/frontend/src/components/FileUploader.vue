<script setup>
import { ref } from 'vue'
import uploadIcon from '../assets/upload-icon.jpg'
import { processAudio } from '../api/client'
import { useRouter } from 'vue-router'

const emit = defineEmits(['analyze-complete'])
const router = useRouter()

const isDragOver = ref(false)
const selectedFileName = ref('')
const isAnalyzing = ref(false)
const isCompleted = ref(false) // 完了画面を表示するためのフラグ
const isDiarizationOn = ref(true)
const errorMessage = ref('')

const handleDragOver = (e) => { e.preventDefault(); isDragOver.value = true }
const handleDragLeave = () => { isDragOver.value = false }
const handleDrop = (e) => {
  e.preventDefault(); isDragOver.value = false
  if (e.dataTransfer.files.length > 0) {
    selectedFileName.value = e.dataTransfer.files[0].name
    isCompleted.value = false // 新しいファイルを入れたら完了画面をリセット
  }
}
const handleFileChange = (e) => {
  if (e.target.files[0]) {
    selectedFileName.value = e.target.files[0].name
    isCompleted.value = false // 新しいファイルを入れたら完了画面をリセット
  }
}

const handleClearFile = () => {
  selectedFileName.value = ''
  errorMessage.value = ''
  isCompleted.value = false
}

// データ参照ページへ遷移（※環境に合わせてパスを変更してください）
const goToDataPage = () => {
  router.push('/data') 
}

const handleAnalyze = async () => {
  if (!selectedFileName.value) return alert('音声ファイルを選択してください')
  
  isAnalyzing.value = true
  errorMessage.value = ''
  isCompleted.value = false

  try {
    const newSummary = await processAudio(selectedFileName.value)
    emit('analyze-complete', newSummary)
    
    // 成功したら完了フラグをONにする
    isCompleted.value = true

  } catch (error) {
    console.error('音声解析エラー:', error)
    errorMessage.value = error.message || '音声の解析処理に失敗しました'
  } finally {
    isAnalyzing.value = false
  }
}
</script>

<template>
  <section class="uploader-container">
    
    <!-- 1. 解析中（ローディング状態） -->
    <div v-if="isAnalyzing" class="status-card loading-card">
      <div class="spinner"></div>
      <h3 class="status-title">AI解析・要約を実行中...</h3>
      <p class="status-sub">文字起こしとデータの抽出を行っています。そのままお待ちください。</p>
      <div class="active-file-badge">
        <span class="file-icon">🎙️</span> {{ selectedFileName }}
      </div>
    </div>

    <!-- 2. 解析完了画面（絵文字を削除） -->
    <div v-else-if="isCompleted" class="status-card completed-card">
      <h3 class="status-title">解析が完了しました！</h3>
      <p class="status-sub">「{{ selectedFileName }}」のデータが抽出されました。</p>
      
      <div class="completed-actions">
        <button class="btn btn-cancel" @click="handleClearFile">続けてアップロード</button>
        <button class="btn btn-primary" @click="goToDataPage">データを確認する →</button>
      </div>
    </div>

    <!-- 3. ファイル未選択（ドラッグ＆ドロップ待機状態） -->
    <div
      v-else-if="!selectedFileName"
      class="drop-zone"
      :class="{ 'is-dragover': isDragOver }"
      @dragover="handleDragOver"
      @dragleave="handleDragLeave"
      @drop="handleDrop"
    >
      <div class="icon-crop-circle">
        <img :src="uploadIcon" alt="音声解析アイコン" class="upload-icon-img" />
      </div>

      <h2 class="upload-title">通話音声ファイルをAI文字起こし</h2>
      <p class="upload-sub">ファイルをドラッグ＆ドロップ</p>
      <p class="upload-or">または</p>

      <div class="file-btn-wrapper">
        <input type="file" id="audio-file" accept="audio/*" class="file-input-hidden" @change="handleFileChange" />
        <label for="audio-file" class="btn btn-select">ファイルを選択</label>
      </div>

      <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
    </div>

    <!-- 4. ファイル選択済み（確認・実行状態） -->
    <div v-else class="status-card confirm-card">
      <div class="confirm-header">
        <h3 class="confirm-title">ファイルの確認</h3>
        <p class="confirm-sub">以下のファイルを解析します。間違いがなければ「音声解析を実行」を押してください。</p>
      </div>

      <div class="selected-file-card">
        <div class="file-info">
          <span class="file-icon">📁</span>
          <span class="file-name">{{ selectedFileName }}</span>
        </div>
        <button class="btn-clear" title="ファイルを解除" @click="handleClearFile">✕</button>
      </div>

      <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

      <div class="confirm-actions">
        <button class="btn btn-cancel" @click="handleClearFile">キャンセル</button>
        <button class="btn btn-analyze" @click="handleAnalyze">
          <span class="btn-inner">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3z"/>
              <path d="M19 10v2a7 7 0 0 1-14 0v-2"/>
              <line x1="12" y1="19" x2="12" y2="22"/>
            </svg>
            音声解析を実行する
          </span>
        </button>
      </div>
    </div>

    <!-- オプションバー（解析完了画面以外で表示） -->
    <div class="options-bar" v-if="!isCompleted">
      <div class="option-item">
        <label class="toggle-switch">
          <input type="checkbox" v-model="isDiarizationOn" />
          <span class="slider"></span>
        </label>
        <span class="toggle-label">話者分離</span>
      </div>
    </div>
  </section>
</template>

<style scoped>
/* 完了画面のスタイル（アニメーションとアイコン関連を削除） */
.completed-card { 
  display: flex; 
  flex-direction: column; 
  align-items: center; 
  padding: 48px 24px; 
}
.completed-actions { 
  display: flex; 
  justify-content: center; 
  gap: 16px; 
  margin-top: 24px; 
  width: 100%; 
}
.btn-primary { 
  background-color: var(--primary-green, #38b2ac); 
  color: #ffffff; 
  padding: 10px 20px; 
  border-radius: 6px; 
  font-size: 0.95rem; 
  font-weight: bold; 
  cursor: pointer; 
  border: none;
  transition: opacity 0.2s;
}
.btn-primary:hover { opacity: 0.9; }

/* -------------------------------------
   以下は既存のスタイルそのまま
-------------------------------------- */
.uploader-container { background-color: var(--card-bg); border-radius: 12px; border: 1px solid var(--border-gray); overflow: hidden; max-width: 640px; margin: 0 auto; box-shadow: 0 4px 12px rgba(0,0,0,0.03); }
.drop-zone { padding: 40px 20px; text-align: center; border: 2px dashed var(--border-gray); border-radius: 12px; margin: 16px; background-color: #ffffff; transition: all 0.2s ease; }
.drop-zone.is-dragover { border-color: var(--primary-green); background-color: var(--accent-green-bg); }
.icon-crop-circle { width: 100px; height: 100px; margin: 0 auto 16px; border-radius: 50%; overflow: hidden; display: flex; align-items: center; justify-content: center; box-shadow: 0 4px 12px rgba(0,0,0,0.06); }
.upload-icon-img { width: 135%; height: 135%; object-fit: cover; }
.upload-title { font-size: 1.2rem; font-weight: bold; color: var(--text-dark); margin: 0 0 8px; }
.upload-sub { color: var(--text-sub); margin: 0 0 4px; font-size: 0.9rem; }
.upload-or { color: var(--text-sub); font-size: 0.8rem; margin: 0 0 16px; }
.file-btn-wrapper { margin-bottom: 0; }
.file-input-hidden { display: none; }
.btn-select { background-color: var(--primary-green); color: #fff; padding: 10px 24px; border-radius: 6px; font-weight: bold; cursor: pointer; display: inline-block; transition: opacity 0.2s; }
.btn-select:hover { opacity: 0.88; }
.status-card { padding: 36px 24px; text-align: center; background-color: #ffffff; margin: 16px; }
.confirm-title { font-size: 1.15rem; font-weight: bold; color: var(--text-dark); margin: 0 0 6px; }
.confirm-sub { font-size: 0.85rem; color: var(--text-sub); margin: 0 0 20px; }
.selected-file-card { display: flex; align-items: center; justify-content: space-between; background-color: var(--accent-green-bg); border: 1.5px solid var(--accent-light-green); padding: 12px 18px; border-radius: 8px; margin-bottom: 24px; }
.file-info { display: flex; align-items: center; gap: 8px; overflow: hidden; }
.file-icon { font-size: 1.2rem; }
.file-name { font-weight: bold; font-size: 0.95rem; color: var(--primary-green); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.btn-clear { background: none; border: none; font-size: 1rem; color: var(--text-sub); cursor: pointer; padding: 2px 6px; border-radius: 4px; }
.btn-clear:hover { background-color: rgba(0,0,0,0.05); color: var(--text-dark); }
.confirm-actions { display: flex; justify-content: center; gap: 12px; }
.loading-card { display: flex; flex-direction: column; align-items: center; padding: 48px 24px; }
.spinner { width: 40px; height: 40px; border: 3.5px solid var(--border-gray); border-top-color: var(--primary-green); border-radius: 50%; animation: spin 0.8s linear infinite; margin-bottom: 20px; }
@keyframes spin { to { transform: rotate(360deg); } }
.status-title { font-size: 1.15rem; font-weight: bold; color: var(--text-dark); margin: 0 0 8px; }
.status-sub { font-size: 0.85rem; color: var(--text-sub); margin: 0 0 20px; }
.active-file-badge { background-color: var(--bg-gray); padding: 8px 16px; border-radius: 20px; font-size: 0.85rem; font-weight: bold; color: var(--text-dark); border: 1px solid var(--border-gray); }
.btn { border: none; padding: 10px 20px; border-radius: 6px; font-size: 0.9rem; font-weight: bold; cursor: pointer; transition: opacity 0.2s; }
.btn:hover { opacity: 0.88; }
.btn-cancel { background-color: var(--bg-gray); color: var(--text-dark); border: 1px solid var(--border-gray); }
.btn-analyze { background-color: var(--accent-light-green); color: #ffffff; flex: 1; max-width: 260px; }
.btn-inner { display: inline-flex; align-items: center; justify-content: center; gap: 8px; }
.error-message { color: #e53e3e; font-size: 0.85rem; margin-top: 12px; font-weight: 500; }
.options-bar { background-color: var(--accent-green-bg); padding: 12px 24px; display: flex; justify-content: center; align-items: center; border-top: 1px solid var(--border-gray); }
.option-item { display: flex; align-items: center; gap: 8px; font-size: 0.9rem; }
.toggle-switch { position: relative; display: inline-block; width: 44px; height: 24px; }
.toggle-switch input { opacity: 0; width: 0; height: 0; }
.slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #ccc; transition: .3s; border-radius: 24px; }
.slider:before { position: absolute; content: ""; height: 18px; width: 18px; left: 3px; bottom: 3px; background-color: white; transition: .3s; border-radius: 50%; }
input:checked + .slider { background-color: var(--primary-green); }
input:checked + .slider:before { transform: translateX(20px); }
.toggle-label { font-weight: 500; color: var(--text-dark); }
</style>