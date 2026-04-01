import request from '@/utils/request.js'

export function useVoteApi() {
  return {
    createVote(data) {
      return request({ url: '/vote/create', method: 'post', data })
    },
    getVotes(chatId) {
      return request({ url: `/vote/list/${chatId}`, method: 'get' })
    },
    getVoteDetail(voteId) {
      return request({ url: `/vote/${voteId}`, method: 'get' })
    },
    doVote(voteId, optionIds) {
      return request({ url: `/vote/${voteId}/vote`, method: 'post', data: { optionIds } })
    }
  }
}
