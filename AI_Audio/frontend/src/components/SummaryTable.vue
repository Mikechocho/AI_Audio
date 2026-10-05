<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  summaries: { type: Array, default: () => [] },
  selectedTag: String
})

const emit = defineEmits(['open-edit'])

const searchQuery = ref('')
const sortOrder = ref('desc')

// 表示モード管理 ('card' | 'list')
const viewMode = ref('card')

// ToDoのJSON文字列を配列に変換する関数
const parseTodos = (todoStr) => {
  if (!todoStr) return []
  try {
    return typeof todoStr === 'string' ? JSON.parse(todoStr) : todoStr
  } catch (e) {
    console.error("ToDoの読み込みに失敗しました", e)
    return []
  }
}

// タグのJSON文字列を配列に変換する関数
const parseTags = (tagStr) => {
  if (!tagStr) return []
  try {
    return typeof tagStr === 'string' ? JSON.parse(tagStr) : tagStr
  } catch (e) {
    return []
  }
}

// 重要度バッジ用CSSクラス判定関数
const getPriorityClass = (priority) => {
  if (priority === '高') return 'priority-high'
  if (priority === '中') return 'priority-medium'
  if (priority === '低') return 'priority-low'
  return 'priority-default'
}

// 検索・絞り込み・ソート適用済みのリスト
const filteredSummaries = computed(() => {
  return props.summaries
    .filter(item => {
      const tags = parseTags(item.tags)
      // サイドバー等でタグが選択されている場合のフィルタリング
      if (props.selectedTag && !tags.includes(props.selectedTag)) return false
      
      const query = searchQuery.value.toLowerCase().trim()
      if (!query) return true

      const todos = parseTodos(item.todoList)
      const todoMatch = todos.some(t => (t.task || t).toLowerCase().includes(query))
      const tagMatch = tags.some(t => t.toLowerCase().includes(query))

      return (
        item.extractedName?.toLowerCase().includes(query) ||
        item.extractedPhone?.includes(query) ||
        item.extractedSubject?.toLowerCase().includes(query) ||
        item.summaryText?.toLowerCase().includes(query) ||
        todoMatch ||
        tagMatch
      )
    })
    .sort((a, b) => {
      const dateA = new Date(a.createdAt).getTime()
      const dateB = new Date(b.createdAt).getTime()
      return sortOrder.value === 'desc' ? dateB - dateA : dateA - dateB
    })
})

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}/${String(date.getMonth() + 1).padStart(2, '0')}/${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}
</script>

<template>
  <section class="container">
    <!-- コントロールパネル -->
    <div class="control-panel">
      <div class="search-box">
        <span class="search-icon">🔍</span>
        <input v-model="searchQuery" type="text" placeholder="氏名・要約・用件・ToDo・タグで検索..." class="input-search" />
      </div>

      <div class="filter-group">
        <div class="select-wrapper">
          <span class="label-icon">⇅</span>
          <select v-model="sortOrder" class="select-filter">
            <option value="desc">新しい順</option>
            <option value="asc">古い順</option>
          </select>
        </div>

        <!-- 表示切り替えボタン（カード / リスト） -->
        <div class="view-switch">
          <button
            class="switch-btn"
            :class="{ active: viewMode === 'card' }"
            @click="viewMode = 'card'"
            title="カード表示"
          >
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="7" height="7" rx="1"/>
              <rect x="14" y="3" width="7" height="7" rx="1"/>
              <rect x="3" y="14" width="7" height="7" rx="1"/>
              <rect x="14" y="14" width="7" height="7" rx="1"/>
            </svg>
          </button>
          <button
            class="switch-btn"
            :class="{ active: viewMode === 'list' }"
            @click="viewMode = 'list'"
            title="リスト表示"
          >
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="8" y1="6" x2="21" y2="6"/>
              <line x1="8" y1="12" x2="21" y2="12"/>
              <line x1="8" y1="18" x2="21" y2="18"/>
              <line x1="3" y1="6" x2="3.01" y2="6"/>
              <line x1="3" y1="12" x2="3.01" y2="12"/>
              <line x1="3" y1="18" x2="3.01" y2="18"/>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- データなし表示 -->
    <div v-if="filteredSummaries.length === 0" class="no-data-card">
      該当するデータが見つかりません
    </div>

    <!-- パターンA: カード表示モード -->
    <div v-else-if="viewMode === 'card'" class="card-list">
      <div
        v-for="item in filteredSummaries"
        :key="item.id || item.transcriptionId"
        class="data-card"
        @click="emit('open-edit', item)"
      >
        <div class="card-header">
          <span class="date-text">{{ formatDate(item.createdAt) }}</span>
        </div>

        <div class="extracted-bar">
          <!-- 氏名ブロック（深緑ライン） -->
          <div class="info-block block-name">
            <span class="field-label">氏名</span>
            <p class="field-value">{{ item.extractedName || '未検出' }}</p>
          </div>

          <!-- 電話番号ブロック（黄緑ライン） -->
          <div class="info-block block-tel">
            <span class="field-label">電話番号</span>
            <p class="field-value val-phone">{{ item.extractedPhone || '未検出' }}</p>
          </div>

          <!-- 内容ブロック（グレーライン） -->
          <div class="info-block block-subject">
            <span class="field-label">内容</span>
            <p class="field-value">{{ item.extractedSubject || '未検出' }}</p>
          </div>
        </div>

        <!-- カード用 ToDoエリア -->
        <div class="summary-box todo-box">
          <div class="summary-label">ToDo</div>
          <ul v-if="parseTodos(item.todoList).length > 0" class="todo-list">
            <li v-for="(todo, index) in parseTodos(item.todoList)" :key="index" class="todo-item">
              <span class="todo-priority" :class="getPriorityClass(todo.priority)">
                {{ todo.priority || '-' }}
              </span>
              <span class="todo-task">{{ todo.task || todo }}</span>
            </li>
          </ul>
          <p v-else class="text-empty">なし</p>
        </div>

        <!-- タグ表示エリア -->
        <div class="card-footer" v-if="parseTags(item.tags).length > 0">
          <span v-for="tag in parseTags(item.tags)" :key="tag" class="tag-chip"># {{ tag }}</span>
        </div>
      </div>
    </div>

    <!-- パターンB: リスト（テーブル）表示モード -->
    <div v-else class="list-view-container">
      <table class="data-table">
        <thead>
          <tr>
            <th class="th-date">日時</th>
            <th class="th-name">氏名</th>
            <th class="th-tel">電話番号</th>
            <th class="th-subject">用件</th>
            <th class="th-summary">要約</th>
            <th class="th-todo">ToDo</th>
            <th class="th-tags">タグ</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="item in filteredSummaries"
            :key="item.id || item.transcriptionId"
            class="table-row"
            @click="emit('open-edit', item)"
          >
            <td class="td-date">{{ formatDate(item.createdAt) }}</td>
            <td class="td-name">{{ item.extractedName || '未検出' }}</td>
            <td class="td-tel">{{ item.extractedPhone || '未検出' }}</td>
            <td class="td-subject">
              <span class="subject-clamp">{{ item.extractedSubject || 'なし' }}</span>
            </td>
            <td class="td-summary">
              <span class="summary-clamp">{{ item.summaryText }}</span>
            </td>
            <td class="td-todo">
              <ul v-if="parseTodos(item.todoList).length > 0" class="table-todo-list">
                <li v-for="(todo, index) in parseTodos(item.todoList)" :key="index" class="table-todo-item">
                  <span class="todo-priority" :class="getPriorityClass(todo.priority)">
                    {{ todo.priority || '-' }}
                  </span>
                  <span class="todo-task-clamp">{{ todo.task || todo }}</span>
                </li>
              </ul>
              <span v-else class="text-empty-inline">なし</span>
            </td>
            <td class="td-tags">
              <div v-if="parseTags(item.tags).length > 0" class="table-tags-wrapper">
                <span v-for="tag in parseTags(item.tags)" :key="tag" class="tag-chip"># {{ tag }}</span>
              </div>
              <span v-else class="text-empty-inline">なし</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<style scoped>
.container { display: flex; flex-direction: column; gap: 16px; }

/* コントロールパネル */
.control-panel {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  background-color: #ffffff;
  padding: 12px 18px;
  border-radius: 10px;
  border: 1px solid var(--border-gray);
}
.search-box {
  display: flex;
  align-items: center;
  background-color: var(--bg-gray);
  border: 1px solid var(--border-gray);
  border-radius: 8px;
  padding: 8px 14px;
  flex: 1;
}
.search-icon { margin-right: 8px; opacity: 0.6; }
.input-search { border: none; background: transparent; width: 100%; outline: none; }
.filter-group { display: flex; gap: 12px; align-items: center; }
.select-wrapper {
  display: flex;
  align-items: center;
  gap: 6px;
  background-color: var(--bg-gray);
  border: 1px solid var(--border-gray);
  border-radius: 8px;
  padding: 4px 10px;
}
.select-filter { border: none; background: transparent; outline: none; cursor: pointer; color: var(--text-dark); }

/* 切り替えボタン */
.view-switch {
  display: flex;
  background-color: var(--bg-gray);
  padding: 4px;
  border-radius: 8px;
  border: 1px solid var(--border-gray);
  gap: 2px;
}
.switch-btn {
  background: transparent;
  border: none;
  padding: 6px 8px;
  border-radius: 6px;
  cursor: pointer;
  color: var(--text-sub);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.switch-btn:hover { color: var(--text-dark); }
.switch-btn.active {
  background-color: #ffffff;
  color: var(--primary-green);
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.no-data-card { background: #fff; padding: 40px; text-align: center; color: var(--text-sub); border-radius: 10px; border: 1px solid var(--border-gray); }

/* --- パターンA: カード表示 --- */
.card-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
  align-items: stretch;
}
.data-card {
  background-color: #ffffff;
  border: 1px solid var(--border-gray);
  border-radius: 10px;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(0,0,0,0.02);
  transition: all 0.2s ease;
}
.data-card:hover {
  transform: translateY(-2px);
  border-color: var(--accent-light-green);
  box-shadow: 0 4px 12px rgba(0, 141, 76, 0.08);
}
.card-header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  border-bottom: 1px solid var(--border-gray);
  padding-bottom: 6px;
}
.date-text { color: var(--text-sub); font-size: 0.725rem; }

/* 抽出項目のバーグループ */
.extracted-bar {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

/* ★元の配色を再現：薄グレー背景 + 左ボーダー */
.info-block {
  background-color: var(--bg-gray);
  border-radius: 6px;
  padding: 6px 10px;
  border-left: 3.5px solid var(--border-gray);
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.block-name {
  border-left-color: var(--primary-green); /* 深緑 */
}
.block-tel {
  border-left-color: var(--accent-light-green); /* 黄緑 */
}
.block-subject {
  border-left-color: #718096; /* 濃いグレー */
}

.field-label {
  font-size: 0.7rem;
  font-weight: bold;
  color: var(--text-sub);
}
.field-value {
  margin: 0;
  padding-left: 8px; /* ラベルの下にインデント */
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--text-dark);
  line-height: 1.4;
  word-break: break-all;
}
.val-phone {
  font-family: monospace;
}

.card-footer { display: flex; gap: 4px; flex-wrap: wrap; margin-top: auto; padding-top: 4px; }
.tag-chip { font-size: 0.7rem; color: var(--primary-green); background-color: var(--accent-green-bg); padding: 2px 8px; border-radius: 10px; font-weight: bold; }

/* --- パターンB: リスト表示 --- */
.list-view-container {
  background-color: #ffffff;
  border: 1px solid var(--border-gray);
  border-radius: 10px;
  overflow: hidden;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
  font-size: 0.85rem;
}
.data-table th {
  background-color: var(--bg-gray);
  color: var(--text-sub);
  padding: 12px 16px;
  font-weight: bold;
  border-bottom: 1px solid var(--border-gray);
  white-space: nowrap;
}
.table-row {
  border-bottom: 1px solid var(--border-gray);
  cursor: pointer;
  transition: background-color 0.15s;
}
.table-row:last-child { border-bottom: none; }
.table-row:hover { background-color: var(--accent-green-bg); }
.data-table td { padding: 14px 16px; vertical-align: middle; }

/* 各列のスタイル */
.td-date { color: var(--text-sub); font-size: 0.8rem; white-space: nowrap; width: 120px; }
.td-name { font-weight: bold; color: var(--text-dark); white-space: nowrap; width: 100px; }
.td-tel { font-family: monospace; white-space: nowrap; color: var(--text-sub); width: 110px; }
.td-subject { font-weight: 500; color: var(--primary-green); width: 15%; }
.subject-clamp {
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.td-summary { color: var(--text-dark); width: 25%; }
.summary-clamp {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}

/* ToDoスタイル */
.todo-box { margin-top: 4px; }
.todo-list, .table-todo-list {
  list-style: none;
  padding: 0;
  margin: 4px 0 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.todo-item, .table-todo-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.775rem;
  line-height: 1.4;
}
.todo-priority {
  font-size: 0.65rem;
  font-weight: bold;
  padding: 1px 6px;
  border-radius: 4px;
  color: #ffffff;
  white-space: nowrap;
  line-height: 1.3;
}
.priority-high { background-color: #E5484D; }
.priority-medium { background-color: #E8A33D; }
.priority-low { background-color: #29ABE2; }
.priority-default { background-color: var(--text-sub); }

.todo-task { color: var(--text-dark); word-break: break-all; }
.td-todo { width: 20%; }
.todo-task-clamp {
  color: var(--text-dark);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 160px;
}

/* リスト表示内のタグカラム用スタイル */
.td-tags { width: 15%; }
.table-tags-wrapper {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.text-empty { color: var(--text-sub); margin: 0; font-size: 0.75rem; }
.text-empty-inline { color: var(--text-sub); font-size: 0.75rem; }
</style>