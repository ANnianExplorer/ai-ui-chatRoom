export function useAIApi() {
  return {
    send() {
      return import.meta.env.VITE_APP_API_URL + 'ai/send'
    }
  }
}
