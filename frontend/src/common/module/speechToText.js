import { watch, onUnmounted } from 'vue'

export default function useSpeechToText(isListening, options = {}) {
  // 기본 문법 정의
  const {
    onResult = () => {},
    onError = () => {},
    lang = 'ko-KR',
    continuous = true,
    interimResults = true,
    grammarList = [], // 기본 문법을 기본값으로 설정
    weight = 1, // 문법의 가중치 (0.0 ~ 1.0)
  } = options

  let recognition = null

  // Speech Recognition 초기화
  const initializeSpeechRecognition = () => {
    // 브라우저 호환성 체크
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
    const SpeechGrammarList = window.SpeechGrammarList || window.webkitSpeechGrammarList

    if (!SpeechRecognition) {
      onError('이 브라우저는 Speech Recognition을 지원하지 않습니다.')
      return false
    }

    recognition = new SpeechRecognition()
    recognition.lang = lang
    recognition.continuous = continuous
    recognition.interimResults = interimResults

    // Grammar 설정
    if (SpeechGrammarList && grammarList.length > 0) {
      const speechGrammarList = new SpeechGrammarList()

      const grammarSpec = `#JSGF V1.0 KR;
      grammar process;
      public <process> = ${grammarList.join(' | ')};`
      speechGrammarList.addFromString(grammarSpec, weight)
      recognition.grammars = speechGrammarList
    }

    // 음성 인식 결과 처리
    recognition.onresult = (event) => {
      const result = event.results[0]
      const transcript = result[0].transcript

      onResult(transcript, result.isFinal)
    }

    // 음성 인식이 끝나면 자동으로 다시 시작
    recognition.onend = () => {
      if (isListening.value) {
        try {
          recognition.start()
          console.log('음성 인식 재시작')
        } catch (e) {
          onError('음성 인식 재시작 실패')
        }
      }
    }

    // 에러 처리
    recognition.onerror = (event) => {
      onError(`음성 인식 오류: ${event.error}`)
      if (event.error === 'no-speech' && isListening.value) {
        try {
          recognition.stop()
          recognition.start()
          console.log('no-speech 후 재시작')
        } catch (e) {
          onError('음성 인식 재시작 실패')
        }
      }
    }

    return true
  }

  // isListening 값 변경 감지
  watch(
    () => isListening.value,
    (newValue) => {
      if (newValue) {
        if (!recognition && !initializeSpeechRecognition()) {
          return
        }
        try {
          recognition.start()
          console.log('음성 인식 시작')
        } catch (e) {
          onError('음성 인식을 시작하는데 실패했습니다.')
        }
      } else {
        if (recognition) {
          try {
            recognition.stop()
            console.log('음성 인식 종료')
          } catch (e) {
            onError('음성 인식을 중지하는데 실패했습니다.')
          }
        }
      }
    },
    { immediate: true },
  )

  // 컴포넌트 언마운트 시 자동 정리
  onUnmounted(() => {
    if (recognition) {
      recognition.stop()
      recognition = null
    }
  })
}
