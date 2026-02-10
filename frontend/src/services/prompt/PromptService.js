import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  // User Prompt 조회
  getUserPrompt() {
    return http.get(`/api/user-prompt`).then(unWrapData)
  },

  // User Prompt 추가, 수정, 삭제
  modifyUserPrompt(addedPrompts, updatedPrompts, removedPromptIds) {
    return http.post('/api/user-prompt', { addedPrompts, updatedPrompts, removedPromptIds }).then(unWrapData)
  },

  // System Prompt 조회
  getSystemPrompt() {
    return http.get(`/api/system-prompt`).then(unWrapData)
  },
}
