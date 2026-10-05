<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Sidebar from './components/Sidebar.vue'
import DetailModal from './components/DetailModal.vue'

const API_BASE_URL = 'http://localhost:8080/api'

const route = useRoute()
const router = useRouter()

const currentView = computed(() => route.name)

const selectedTag = ref(null)
const isSidebarCollapsed = ref(false)

const summaries = ref([])
const isModalOpen = ref(false)
const selectedItem = ref(null)
const modalMode = ref('data')

const fetchSummaries = async () => {
  try {
    const res = await fetch(`${API_BASE_URL}/summaries`)
    if (res.ok) {
      summaries.value = await res.json()
    }
  } catch (err) {
    console.error('データ取得失敗:', err)
  }
}

onMounted(() => {
  fetchSummaries()
})

const handleAnalyzeComplete = (newSummary) => {
  summaries.value.unshift(newSummary)
  selectedTag.value = null
}

// データ参照からの詳細表示
const handleOpenEdit = (item) => {
  selectedItem.value = item
  modalMode.value = 'data'
  isModalOpen.value = true
}

// Todo画面からの詳細表示
const handleOpenTodoEdit = (item) => {
  selectedItem.value = item
  modalMode.value = 'todo'
  isModalOpen.value = true
}

// 「最近」選択時
const handleClickRecent = (item) => {
  selectedTag.value = null
  router.push('/data')
  handleOpenEdit(item)
}

// 「Todo」選択時
const handleClickTodo = (item) => {
  router.push('/todo')
  handleOpenTodoEdit(item)
}

const applyLocalUpdate = (targetId, updatedItem) => {
  const targetStr = String(targetId)
  summaries.value = summaries.value.map(s => {
    if (String(s.id || s.transcriptionId) === targetStr) {
      return { ...updatedItem }
    }
    return s
  })
}

const handleSaveEdit = async (updatedItem) => {
  const targetId = updatedItem.id || updatedItem.transcriptionId
  try {
    const res = await fetch(`${API_BASE_URL}/summaries/${targetId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(updatedItem)
    })
    if (res.ok) {
      const savedData = await res.json()
      const targetStr = String(targetId)
      summaries.value = summaries.value.map(s => {
        if (String(s.id || s.transcriptionId) === targetStr) {
          return {
            ...savedData,
            status: savedData.status || updatedItem.status || 'pending'
          }
        }
        return s
      })
    } else {
      applyLocalUpdate(targetId, updatedItem)
    }
  } catch (err) {
    console.error('更新失敗:', err)
    applyLocalUpdate(targetId, updatedItem)
  }
}

const handleDeleteItem = async (id) => {
  const targetStr = String(id)
  try {
    const res = await fetch(`${API_BASE_URL}/summaries/${id}`, { method: 'DELETE' })
    if (res.ok || res.status === 204) {
      summaries.value = summaries.value.filter(s => String(s.id || s.transcriptionId) !== targetStr)
    }
  } catch (err) {
    console.error('削除失敗:', err)
  }
}

// Todo完了処理（ID型を一致させ、配列を更新して即座に完了タブへ飛ばす）
const handleCompleteTodo = async (targetId) => {
  const targetStr = String(targetId)

  // 1. 配列をmapで生成し直し、リアクティブ変更を確実に即時反映
  summaries.value = summaries.value.map(s => {
    if (String(s.id || s.transcriptionId) === targetStr) {
      return { ...s, status: 'completed' }
    }
    return s
  })

  // 2. 更新済みのデータを取得してAPI保存を実行
  const updatedItem = summaries.value.find(s => String(s.id || s.transcriptionId) === targetStr)
  if (updatedItem) {
    await handleSaveEdit(updatedItem)
  }

  isModalOpen.value = false
}

const handleChangeView = (viewName) => {
  if (viewName === 'upload') router.push('/')
  else if (viewName === 'table') router.push('/data')
  else if (viewName === 'todo') router.push('/todo')
}

const handleSelectTag = (tag) => {
  selectedTag.value = tag
  router.push('/data')
}
</script>

<template>
  <div class="app-layout">
    <Sidebar
      :current-view="currentView"
      :selected-tag="selectedTag"
      :is-collapsed="isSidebarCollapsed"
      :summaries="summaries"
      @change-view="handleChangeView"
      @select-tag="handleSelectTag"
      @click-recent="handleClickRecent"
      @click-todo="handleClickTodo"
      @toggle-collapse="isSidebarCollapsed = !isSidebarCollapsed"
    />

    <main class="main-content" :class="{ 'collapsed-sidebar': isSidebarCollapsed }">
      <router-view
        :api-base-url="API_BASE_URL"
        :summaries="summaries"
        :selected-tag="selectedTag"
        @analyze-complete="handleAnalyzeComplete"
        @open-edit="handleOpenEdit"
        @open-todo-edit="handleOpenTodoEdit"
      />

      <DetailModal
        :is-open="isModalOpen"
        :item-data="selectedItem"
        :mode="modalMode"
        @close="isModalOpen = false"
        @save="handleSaveEdit"
        @delete-item="handleDeleteItem"
        @complete-todo="handleCompleteTodo"
      />
    </main>
  </div>
</template>

<style>
:root {
  --primary-green: #008d4c;
  --accent-light-green: #7cb342;
  --accent-green-bg: #f0f7ed;
  --bg-gray: #f4f6f8;
  --card-bg: #ffffff;
  --border-gray: #e2e8f0;
  --text-dark: #2d3748;
  --text-sub: #718096;
}
body { font-family: 'Helvetica Neue', Arial, sans-serif; margin: 0; background-color: var(--bg-gray); color: var(--text-dark); }
.app-layout { display: flex; }
.main-content { margin-left: 260px; flex: 1; padding: 32px; min-height: 100vh; box-sizing: border-box; transition: margin-left 0.3s ease; }
.main-content.collapsed-sidebar { margin-left: 72px; }
</style>