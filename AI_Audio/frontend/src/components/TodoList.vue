<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  summaries: { type: Array, default: () => [] }
})

const emit = defineEmits(['open-todo-edit'])

// フィルタータブ ('all' | 'high' | 'medium' | 'low' | 'completed')
const activeTab = ref('all')

const todoItems = computed(() => {
  return props.summaries.map(item => ({
    ...item,
    priority: item.priority || '中',
    status: item.status || 'pending'
  }))
})

const filteredTodos = computed(() => {
  if (activeTab.value === 'completed') {
    return todoItems.value.filter(item => item.status === 'completed')
  }
  
  // 未完了タスクのみ抽出
  const pendingItems = todoItems.value.filter(item => item.status !== 'completed')
  
  if (activeTab.value === 'high') return pendingItems.filter(i => i.priority === '高' || i.priority === 'high')
  if (activeTab.value === 'medium') return pendingItems.filter(i => i.priority === '中' || i.priority === 'medium')
  if (activeTab.value === 'low') return pendingItems.filter(i => i.priority === '低' || i.priority === 'low')
  
  return pendingItems
})

const getPriorityClass = (priority) => {
  if (priority === '高' || priority === 'high') return 'p-high'
  if (priority === '低' || priority === 'low') return 'p-low'
  return 'p-medium'
}

const handleRowClick = (item) => {
  emit('open-todo-edit', item)
}
</script>

<template>
  <section class="todo-container">
    <h2>Todo 管理</h2>

    <!-- 分類タブ -->
    <div class="todo-tabs">
      <button class="tab-btn" :class="{ active: activeTab === 'all' }" @click="activeTab = 'all'">すべて</button>
      <button class="tab-btn badge-high" :class="{ active: activeTab === 'high' }" @click="activeTab = 'high'">高</button>
      <button class="tab-btn badge-medium" :class="{ active: activeTab === 'medium' }" @click="activeTab = 'medium'">中</button>
      <button class="tab-btn badge-low" :class="{ active: activeTab === 'low' }" @click="activeTab = 'low'">低</button>
      <button class="tab-btn tab-completed" :class="{ active: activeTab === 'completed' }" @click="activeTab = 'completed'">✅ 完了済み</button>
    </div>

    <div v-if="filteredTodos.length === 0" class="no-data">
      該当するTodoはありません
    </div>

    <!-- リスト（テーブル）表示形式 -->
    <div v-else class="list-view-container">
      <table class="todo-table">
        <thead>
          <tr>
            <th class="th-priority">優先度</th>
            <th class="th-company">所属</th>
            <th class="th-name">氏名</th>
            <th class="th-subject">用件</th>
            <th class="th-status" v-if="activeTab === 'completed'">状態</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="item in filteredTodos"
            :key="item.id || item.transcriptionId"
            class="table-row"
            :class="{ 'is-completed': item.status === 'completed' }"
            @click="handleRowClick(item)"
          >
            <td class="td-priority">
              <span class="priority-badge" :class="getPriorityClass(item.priority)">
                {{ item.priority }}
              </span>
            </td>
            <td class="td-company">{{ item.extractedCompany || '未検出' }}</td>
            <td class="td-name">{{ item.extractedName || '未検出' }}</td>
            <td class="td-subject">
              <span class="subject-clamp">{{ item.extractedSubject || '未検出' }}</span>
            </td>
            <td class="td-status" v-if="activeTab === 'completed'">
              <span class="status-badge">完了済み</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<style scoped>
.todo-container { display: flex; flex-direction: column; gap: 16px; }
.todo-tabs { display: flex; gap: 8px; flex-wrap: wrap; }
.tab-btn { padding: 6px 14px; border-radius: 20px; border: 1px solid var(--border-gray); background: #ffffff; cursor: pointer; font-size: 0.85rem; font-weight: bold; }
.tab-btn.active { background-color: var(--primary-green); color: #ffffff; border-color: var(--primary-green); }
.tab-completed.active { background-color: #4a5568; border-color: #4a5568; }

.list-view-container {
  background-color: #ffffff;
  border: 1px solid var(--border-gray);
  border-radius: 10px;
  overflow: hidden;
}
.todo-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
  font-size: 0.85rem;
}
.todo-table th {
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
.table-row.is-completed { opacity: 0.6; background-color: var(--bg-gray); }
.todo-table td { padding: 14px 16px; vertical-align: middle; }

.td-priority { width: 100px; }
.td-company { font-weight: 500; color: var(--text-dark); white-space: nowrap; width: 160px; }
.td-name { font-weight: bold; color: var(--text-dark); white-space: nowrap; width: 140px; }
.td-subject { font-weight: 500; color: var(--primary-green); }
.subject-clamp { display: block; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

/* 優先度バッジスタイル */
.priority-badge { font-size: 0.75rem; padding: 4px 10px; border-radius: 4px; font-weight: bold; display: inline-block; }
.p-high { background-color: #fed7d7; color: #9b2c2c; }
.p-medium { background-color: #feebc8; color: #9c4221; }
.p-low { background-color: #e2e8f0; color: #4a5568; }

.status-badge { font-size: 0.75rem; background: #cbd5e0; color: #2d3748; padding: 2px 8px; border-radius: 4px; }
.no-data { background: #ffffff; padding: 32px; text-align: center; color: var(--text-sub); border-radius: 8px; border: 1px solid var(--border-gray); }
</style>