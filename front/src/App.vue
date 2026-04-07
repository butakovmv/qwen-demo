<template>
  <div class="container">
    <h1>OTUS Application</h1>
    <button :disabled="loading" @click="fetchHelloWorld">
      {{ loading ? 'Загрузка...' : 'Получить приветствие' }}
    </button>
    <div v-if="message" class="message">
      <p>{{ message }}</p>
    </div>
    <div v-if="error" class="error">
      <p>{{ error }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface HelloResponse {
  message: string
}

const message = ref<string>('')
const error = ref<string>('')
const loading = ref<boolean>(false)

const fetchHelloWorld = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await fetch('/api/v1/hello-world')
    if (!response.ok) {
      throw new Error(`HTTP ошибка: ${response.status}`)
    }
    const data: HelloResponse = await response.json()
    message.value = data.message
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Неизвестная ошибка'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.container {
  max-width: 600px;
  margin: 40px auto;
  padding: 20px;
  font-family: Arial, sans-serif;
  text-align: center;
}

button {
  padding: 12px 24px;
  font-size: 16px;
  cursor: pointer;
  background-color: #42b883;
  color: white;
  border: none;
  border-radius: 4px;
}

button:disabled {
  background-color: #a0a0a0;
  cursor: not-allowed;
}

.message {
  margin-top: 20px;
  padding: 16px;
  background-color: #e8f5e9;
  border-radius: 4px;
  font-size: 18px;
}

.error {
  margin-top: 20px;
  padding: 16px;
  background-color: #ffebee;
  color: #c62828;
  border-radius: 4px;
}
</style>
