import http from 'src/common/http.js'

export default {
  sendMessage(threadId, message) {
    return http.post('/api/open-ai/chat/message', { threadId, message }).then((res) => res.data)
  },
}
