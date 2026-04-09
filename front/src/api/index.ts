export class ApiError extends Error {
  status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

export async function request<T>(url: string, init?: globalThis.RequestInit): Promise<T> {
  const response = await fetch(url, init)

  if (!response.ok) {
    throw new ApiError(response.status, `HTTP ошибка: ${response.status}`)
  }

  return (await response.json()) as T
}

export { fetchQuestions, type Question, type QuestionsResponse } from './questions'
export { sendAnswers, type Answer, type AnswersResponse } from './answers'
