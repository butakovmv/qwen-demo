import { request } from './index'

export interface Question {
  id: string
  text: string
}

export interface QuestionsResponse {
  questions: Question[]
}

export function fetchQuestions(): Promise<QuestionsResponse> {
  return request<QuestionsResponse>('/api/v1/questions')
}
