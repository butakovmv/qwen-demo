import { request } from './index'

export interface Answer {
  id: string
  text: string
}

export interface AnswersResponse {
  success: boolean
}

export function sendAnswers(answers: Answer[]): Promise<AnswersResponse> {
  return request<AnswersResponse>('/api/v1/answers', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ answers }),
  })
}
