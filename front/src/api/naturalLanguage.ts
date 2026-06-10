import { request } from './index'

export interface NaturalLanguageRequest {
  text: string
}

export interface NaturalLanguageResponse {
  success: boolean
  text: string
}

export function sendNaturalLanguageCommand(text: string): Promise<NaturalLanguageResponse> {
  return request<NaturalLanguageResponse>('/api/v1/natural-language/commands', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ text } as NaturalLanguageRequest),
  })
}
