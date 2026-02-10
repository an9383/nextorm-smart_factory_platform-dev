import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  createChat(chatFlowId, sessionId, title) {
    return http
      .post(`/api/chat-sessions`, {
        chatFlowId,
        sessionId,
        title,
      })
      .then(unWrapData)
  },
  // updateChat(userId, chatFlowId, chatId) {
  //   return http
  //     .patch(`/api/chat/recent-chat`, {
  //       userId,
  //       chatFlowId,
  //       chatId,
  //     })
  //     .then(unWrapData)
  // },
  updateChatTitle(sessionId, title) {
    return http
      .put(`/api/chat-sessions/${sessionId}`, {
        title,
      })
      .then(unWrapData)
  },
  updateChatFavorite(sessionId, stared) {
    return http
      .put(`/api/chat-sessions/${sessionId}/favorite`, {
        isFavorite: stared,
      })
      .then(unWrapData)
  },
  deleteChat(sessionId) {
    return http.delete(`/api/chat-sessions/${sessionId}`, {}).then(unWrapData)
  },
  getChatSessions() {
    return http.get(`/api/chat-sessions`).then(unWrapData)
  },
  // getStaredChatList(userId, chatFlowId) {
  //   return http
  //     .get(`/api/chat/stared-chat`, {
  //       params: {
  //         userId,
  //         chatFlowId,
  //       },
  //     })
  //     .then(unWrapData)
  // },
}
