<template>
  <div class="container">
    <h1>OTUS Application</h1>

    <section class="section">
      <h2>Вопросы</h2>
      <button :disabled="loadingQuestions" @click="loadQuestions">
        {{ loadingQuestions ? 'Загрузка...' : 'Загрузить вопросы' }}
      </button>
      <ul v-if="questions.length" class="questions-list">
        <li v-for="q in questions" :key="q.id">
          <strong>{{ q.text }}</strong>
          <input
            :id="'answer-' + q.id"
            v-model="answers[q.id]"
            type="text"
            placeholder="Ваш ответ"
          />
        </li>
      </ul>
      <button v-if="questions.length" :disabled="loadingSend" @click="sendAnswersHandler">
        {{ loadingSend ? 'Отправка...' : 'Отправить ответы' }}
      </button>
      <div v-if="sendSuccess" class="message">
        <p>Ответы успешно отправлены!</p>
      </div>
    </section>

    <div v-if="error" class="error">
      <p>{{ error }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { fetchQuestions, sendAnswers, type Question } from './api'

const error = ref<string>('')
const loadingQuestions = ref<boolean>(false)
const loadingSend = ref<boolean>(false)
const questions = ref<Question[]>([])
const answers = reactive<Record<string, string>>({})
const sendSuccess = ref<boolean>(false)

const loadQuestions = async () => {
  loadingQuestions.value = true
  error.value = ''
  sendSuccess.value = false
  try {
    const data = await fetchQuestions()
    questions.value = data.questions
    for (const q of data.questions) {
      answers[q.id] = ''
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Неизвестная ошибка'
  } finally {
    loadingQuestions.value = false
  }
}

const sendAnswersHandler = async () => {
  loadingSend.value = true
  error.value = ''
  sendSuccess.value = false
  try {
    const payload = questions.value.map((q) => ({
      id: q.id,
      text: answers[q.id] || '',
    }))
    await sendAnswers(payload)
    sendSuccess.value = true
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Неизвестная ошибка'
  } finally {
    loadingSend.value = false
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

.section {
  margin-bottom: 32px;
  padding: 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
}

h2 {
  margin-top: 0;
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

.questions-list {
  list-style: none;
  padding: 0;
  text-align: left;
}

.questions-list li {
  margin: 12px 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.questions-list input {
  padding: 8px;
  font-size: 14px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
</style>
