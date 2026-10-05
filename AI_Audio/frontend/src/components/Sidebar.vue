<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  currentView: String,
  selectedTag: String,
  isCollapsed: Boolean,
  summaries: { type: Array, default: () => [] }
})

const emit = defineEmits([
  'change-view',
  'select-tag',
  'click-recent',
  'click-todo',
  'toggle-collapse'
])

// 🌟 localStorageから開閉状態を取得・保存
const isTodoOpen = ref(localStorage.getItem('sidebar_isTodoOpen') !== 'false')
const isDataRefOpen = ref(localStorage.getItem('sidebar_isDataRefOpen') !== 'false')

watch(isTodoOpen, (val) => localStorage.setItem('sidebar_isTodoOpen', val))
watch(isDataRefOpen, (val) => localStorage.setItem('sidebar_isDataRefOpen', val))

const todoList = computed(() => {
  return props.summaries
    .filter(item => item.status !== 'completed')
    .slice(0, 5)
})

const recentList = computed(() => {
  return [...props.summaries]
    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    .slice(0, 5)
})

const availableTags = computed(() => {
  const tagsSet = new Set()
  props.summaries.forEach(item => {
    if (Array.isArray(item.tags)) {
      item.tags.forEach(tag => tagsSet.add(tag))
    }
  })
  return Array.from(tagsSet)
})
</script>

<template>
  <aside class="sidebar" :class="{ collapsed: isCollapsed }">
    <div class="sidebar-header">
      <h1 v-if="!isCollapsed" class="logo-title">通話要約ダッシュボード</h1>
      <button class="btn-toggle" @click="emit('toggle-collapse')" title="サイドバー切り替え">
        ☰
      </button>
    </div>

    <nav class="sidebar-nav">
      <!-- 1. 音声解析 -->
      <div
        class="nav-item"
        :class="{ active: currentView === 'upload' }"
        @click="emit('change-view', 'upload')"
      >
        <span class="icon">🎙️</span>
        <span v-if="!isCollapsed" class="label">音声解析</span>
      </div>

      <hr class="divider" v-if="!isCollapsed" />

      <!-- 2. TODO -->
      <div v-if="!isCollapsed" class="menu-section">
        <div
          class="nav-item section-header"
          :class="{ active: currentView === 'todo' }"
        >
          <div class="header-left" @click="emit('change-view', 'todo')">
            <span class="icon">☑️</span>
            <span class="label">TODO ({{ todoList.length }})</span>
          </div>
          <button class="btn-fold" @click.stop="isTodoOpen = !isTodoOpen">
            {{ isTodoOpen ? '▾' : '▸' }}
          </button>
        </div>

        <ul v-if="isTodoOpen" class="sub-list">
          <li
            v-for="(item, index) in todoList"
            :key="'todo-' + (item.id || item.transcriptionId)"
            class="sub-item"
            @click="emit('click-todo', item)"
          >
            <div class="todo-badge-row">
              <span class="todo-num">{{ index + 1 }}.</span>
              <span class="todo-name">{{ item.extractedName || '未検出' }}</span>
              <span class="priority-dot" :class="'dot-' + (item.priority || '中')"></span>
            </div>
            <div class="sub-subject">{{ item.extractedSubject || '用件なし' }}</div>
          </li>
        </ul>
      </div>

      <hr class="divider" v-if="!isCollapsed" />

      <!-- 3. データ参照 -->
      <div v-if="!isCollapsed" class="menu-section">
        <div
          class="nav-item section-header"
          :class="{ active: currentView === 'table' }"
        >
          <div class="header-left" @click="emit('change-view', 'table')">
            <span class="icon">📊</span>
            <span class="label">データ参照</span>
          </div>
          <button class="btn-fold" @click.stop="isDataRefOpen = !isDataRefOpen">
            {{ isDataRefOpen ? '▾' : '▸' }}
          </button>
        </div>

        <div v-if="isDataRefOpen" class="data-ref-body">
          <div class="inner-block">
            <span class="inner-title">最近</span>
            <ul class="sub-list">
              <li
                v-for="item in recentList"
                :key="'recent-' + (item.id || item.transcriptionId)"
                class="sub-item"
                @click="emit('click-recent', item)"
              >
                <div class="recent-name">
                  {{ item.extractedName || '未検出' }}
                  <span class="recent-company" v-if="item.extractedCompany">（{{ item.extractedCompany }}）</span>
                </div>
                <div class="sub-subject">{{ item.extractedSubject || '用件なし' }}</div>
              </li>
            </ul>
          </div>

          <div class="inner-block">
            <span class="inner-title">タグ</span>
            <div class="tag-list">
              <button
                class="tag-item"
                :class="{ active: selectedTag === null }"
                @click="emit('select-tag', null)"
              >
                すべて表示
              </button>
              <button
                v-for="tag in availableTags"
                :key="tag"
                class="tag-item"
                :class="{ active: selectedTag === tag }"
                @click="emit('select-tag', tag)"
              >
                # {{ tag }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </nav>
  </aside>
</template>

<style scoped>
.sidebar {
  width: 260px;
  background-color: #ffffff;
  border-right: 1px solid var(--border-gray);
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  z-index: 100;
}
.sidebar.collapsed { width: 72px; }

.sidebar-header {
  padding: 16px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--border-gray);
}
.logo-title { font-size: 0.95rem; font-weight: bold; color: var(--primary-green); margin: 0; }
.btn-toggle { background: none; border: none; font-size: 1.1rem; cursor: pointer; color: var(--text-sub); }

.sidebar-nav { padding: 16px 12px; overflow-y: auto; flex: 1; }

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  color: var(--text-dark);
  font-weight: bold;
}
.nav-item:hover { background-color: var(--bg-gray); }
.nav-item.active { background-color: var(--accent-green-bg); color: var(--primary-green); }

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-right: 8px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.btn-fold {
  background: none;
  border: none;
  color: var(--text-sub);
  cursor: pointer;
  font-size: 0.85rem;
  padding: 4px 6px;
  border-radius: 4px;
}
.btn-fold:hover { background-color: rgba(0,0,0,0.05); color: var(--text-dark); }

.divider { border: none; border-top: 1px solid var(--border-gray); margin: 8px 0; }

.menu-section { display: flex; flex-direction: column; }

.sub-list { list-style: none; padding: 0; margin: 4px 0 0 12px; }
.sub-item {
  padding: 6px 8px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 2px;
  transition: background-color 0.15s;
}
.sub-item:hover { background-color: var(--bg-gray); }

.todo-badge-row { display: flex; align-items: center; gap: 4px; }
.todo-num { font-size: 0.8rem; font-weight: bold; color: var(--primary-green); }
.todo-name { font-size: 0.8rem; font-weight: bold; flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.priority-dot { width: 8px; height: 8px; border-radius: 50%; }
.dot-高 { background-color: #e53e3e; }
.dot-中 { background-color: #dd6b20; }
.dot-低 { background-color: #a0aec0; }

.recent-name { font-size: 0.8rem; font-weight: bold; color: var(--text-dark); }
.recent-company { font-size: 0.725rem; color: var(--text-sub); font-weight: normal; }
.sub-subject { font-size: 0.725rem; color: var(--text-sub); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-top: 2px; }

.data-ref-body { padding-left: 12px; margin-top: 6px; display: flex; flex-direction: column; gap: 10px; }
.inner-block { display: flex; flex-direction: column; gap: 4px; }
.inner-title { font-size: 0.725rem; font-weight: bold; color: var(--text-sub); margin-left: 8px; }

.tag-list { display: flex; flex-wrap: wrap; gap: 4px; padding-left: 8px; }
.tag-item {
  background: var(--bg-gray);
  border: 1px solid var(--border-gray);
  border-radius: 12px;
  padding: 3px 8px;
  font-size: 0.725rem;
  color: var(--text-dark);
  cursor: pointer;
  transition: all 0.15s;
}
.tag-item:hover { background-color: var(--accent-green-bg); color: var(--primary-green); }
.tag-item.active { background-color: var(--primary-green); color: #ffffff; border-color: var(--primary-green); font-weight: bold; }
</style>