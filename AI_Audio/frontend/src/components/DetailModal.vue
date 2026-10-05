<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  isOpen: Boolean,
  itemData: Object,
  mode: { type: String, default: 'data' } // 'data' または 'todo'
})

const emit = defineEmits(['close', 'save', 'delete-item', 'complete-todo'])

const editForm = ref({
  extractedName: '',
  extractedCompany: '',
  extractedPhone: '',
  extractedSubject: '',
  summaryText: '',
  transcriptionText: '', // ★全文文字起こしを追加
  priority: '中'
})

watch(() => props.itemData, (newItem) => {
  if (newItem) {
    editForm.value = {
      ...newItem,
      transcriptionText: newItem.transcriptionText || newItem.transcript || ''
    }
  }
}, { immediate: true })

const handleClose = () => {
  emit('close')
}

const handleSave = () => {
  emit('save', editForm.value)
  emit('close')
}

const handleDelete = () => {
  const targetId = props.itemData?.id || props.itemData?.transcriptionId
  if (targetId && confirm('本当に削除しますか？')) {
    emit('delete-item', targetId)
    emit('close')
  }
}

const handleComplete = () => {
  const targetId = props.itemData?.id || props.itemData?.transcriptionId
  if (targetId) {
    emit('complete-todo', targetId)
  }
}
</script>

<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="handleClose">
    <div class="modal-content">
      <div class="modal-header">
        <h3>{{ mode === 'todo' ? 'Todo詳細・タスク操作' : 'データ詳細・編集' }}</h3>
        <button class="btn-close" @click="handleClose">✕</button>
      </div>

      <div class="modal-body" v-if="itemData">
        <div class="form-group">
          <label>所属・会社名</label>
          <input v-model="editForm.extractedCompany" type="text" class="form-input" placeholder="未検出" />
        </div>

        <div class="form-group">
          <label>氏名</label>
          <input v-model="editForm.extractedName" type="text" class="form-input" placeholder="未検出" />
        </div>

        <div class="form-group">
          <label>電話番号</label>
          <input v-model="editForm.extractedPhone" type="text" class="form-input" placeholder="未検出" />
        </div>

        <div class="form-group">
          <label>用件</label>
          <input v-model="editForm.extractedSubject" type="text" class="form-input" placeholder="未検出" />
        </div>

        <div class="form-group">
          <label>要約</label>
          <textarea v-model="editForm.summaryText" rows="3" class="form-textarea"></textarea>
        </div>

        <!-- ★全文文字起こしを表示・編集欄として復元 -->
        <div class="form-group">
          <label>全文文字起こし</label>
          <textarea v-model="editForm.transcriptionText" rows="5" class="form-textarea readonly-style" placeholder="文字起こしデータなし"></textarea>
        </div>
      </div>

      <div class="modal-footer">
        <button v-if="mode === 'todo'" class="btn btn-complete" @click="handleComplete">
          ✅ タスク完了にする
        </button>

        <template v-else>
          <button class="btn btn-delete" @click="handleDelete">削除</button>
          <button class="btn btn-save" @click="handleSave">保存</button>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background-color: rgba(0, 0, 0, 0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-content { background-color: #ffffff; border-radius: 12px; width: 90%; max-width: 580px; max-height: 90vh; overflow-y: auto; box-shadow: 0 10px 25px rgba(0,0,0,0.15); display: flex; flex-direction: column; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 24px; border-bottom: 1px solid var(--border-gray); }
.modal-header h3 { margin: 0; font-size: 1.1rem; color: var(--text-dark); }
.btn-close { background: none; border: none; font-size: 1.2rem; cursor: pointer; color: var(--text-sub); }
.modal-body { padding: 20px 24px; display: flex; flex-direction: column; gap: 14px; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-group label { font-size: 0.8rem; font-weight: bold; color: var(--text-sub); }
.form-input, .form-textarea { padding: 8px 12px; border: 1px solid var(--border-gray); border-radius: 6px; font-size: 0.9rem; outline: none; }
.form-input:focus, .form-textarea:focus { border-color: var(--primary-green); }
.readonly-style { background-color: var(--bg-gray); color: var(--text-dark); line-height: 1.5; }
.modal-footer { display: flex; justify-content: flex-end; gap: 12px; padding: 16px 24px; border-top: 1px solid var(--border-gray); background-color: var(--bg-gray); }
.btn { padding: 8px 18px; border-radius: 6px; font-size: 0.88rem; font-weight: bold; cursor: pointer; border: none; }
.btn-delete { background-color: #e53e3e; color: #ffffff; }
.btn-save { background-color: var(--primary-green); color: #ffffff; }
.btn-complete { background-color: var(--primary-green); color: #ffffff; width: 100%; }
</style>