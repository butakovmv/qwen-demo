<template>
  <div class="container">
    <h1>Естественный язык</h1>

    <div ref="historyRef" class="history">
      <div v-if="history.length === 0" class="empty-state">
        Введите команду на естественном языке и нажмите «Сделать».
      </div>
      <div
        v-for="entry in history"
        :key="entry.id"
        class="history-entry"
        :class="{ pending: entry.loading }"
      >
        <div class="entry-row entry-request">
          <span class="label">Запрос:</span>
          <span class="value">{{ entry.requestText }}</span>
        </div>
        <div v-if="entry.loading" class="entry-row entry-pending">
          <span class="label">Ожидание ответа...</span>
        </div>
        <div v-else-if="entry.error" class="entry-row entry-response entry-error">
          <span class="label">Ошибка:</span>
          <span class="value">{{ entry.error }}</span>
        </div>
        <div v-else class="entry-row entry-response" :class="{ failure: !entry.responseSuccess }">
          <span class="label">Результат:</span>
          <span class="value">{{ entry.responseText }}</span>
        </div>
      </div>
    </div>

    <div class="input-area">
      <input
        v-model="inputText"
        type="text"
        placeholder="Введите команду на естественном языке..."
        :disabled="loading"
        @keyup.enter="sendCommand"
      />
      <button :disabled="loading || !inputText.trim()" @click="sendCommand">
        {{ loading ? 'Отправка...' : 'Сделать' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { sendNaturalLanguageCommand } from '../api/naturalLanguage'

interface HistoryEntry {
  id: number
  requestText: string
  responseSuccess: boolean | null
  responseText: string | null
  error: string | null
  loading: boolean
}

const STORAGE_KEY = 'naturalLangHistory'

const history = ref<HistoryEntry[]>([])
const inputText = ref<string>('')
const loading = ref<boolean>(false)
const historyRef = ref<HTMLElement | null>(null)
let nextId = 0

function loadHistory(): HistoryEntry[] {
  try {
    const data = sessionStorage.getItem(STORAGE_KEY)
    if (!data) return []
    const parsed = JSON.parse(data) as HistoryEntry[]
    if (parsed.length > 0) {
      nextId = Math.max(...parsed.map((e) => e.id)) + 1
    }
    return parsed.map((e) => ({ ...e, loading: false }))
  } catch {
    return []
  }
}

function saveHistory() {
  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(history.value))
}

function scrollToBottom() {
  nextTick(() => {
    if (historyRef.value) {
      historyRef.value.scrollTop = historyRef.value.scrollHeight
    }
  })
}

onMounted(() => {
  history.value = loadHistory()
  scrollToBottom()
})

async function sendCommand() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  inputText.value = ''

  const entry: HistoryEntry = {
    id: nextId++,
    requestText: text,
    responseSuccess: null,
    responseText: null,
    error: null,
    loading: true,
  }

  history.value.push(entry)
  saveHistory()
  scrollToBottom()

  loading.value = true

  try {
    const response = await sendNaturalLanguageCommand(text)
    entry.responseSuccess = response.success
    entry.responseText = response.text
  } catch (e) {
    entry.error = e instanceof Error ? e.message : 'Неизвестная ошибка'
  } finally {
    entry.loading = false
    loading.value = false
    saveHistory()
    scrollToBottom()
  }
}
</script>

<style scoped>
.container {
  max-width: 700px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
}

h1 {
  text-align: center;
  flex-shrink: 0;
}

.history {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 12px;
}

.empty-state {
  text-align: center;
  color: #999;
  margin-top: 40px;
}

.history-entry {
  margin-bottom: 12px;
  padding: 10px;
  border-radius: 6px;
  background-color: #f9f9f9;
}

.history-entry.pending {
  opacity: 0.6;
}

.entry-row {
  margin-bottom: 4px;
  line-height: 1.5;
}

.entry-row:last-child {
  margin-bottom: 0;
}

.label {
  font-weight: bold;
  margin-right: 6px;
  color: #555;
}

.value {
  word-break: break-word;
}

.entry-request .value {
  color: #333;
}

.entry-response .value {
  color: #2e7d32;
}

.entry-response.failure .value {
  color: #c62828;
}

.entry-error .value {
  color: #c62828;
}

.entry-pending .label {
  color: #999;
  font-style: italic;
}

.input-area {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.input-area input {
  flex: 1;
  padding: 12px;
  font-size: 16px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.input-area button {
  padding: 12px 24px;
  font-size: 16px;
  cursor: pointer;
  background-color: #42b883;
  color: white;
  border: none;
  border-radius: 4px;
  flex-shrink: 0;
}

.input-area button:disabled {
  background-color: #a0a0a0;
  cursor: not-allowed;
}
</style>
