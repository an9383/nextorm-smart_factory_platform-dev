import { t } from '/src/plugins/i18n'

class ErrorCode {
  constructor(errorCode, message) {
    this.errorCode = errorCode
    this.message = message
  }

  getCode() {
    return this.errorCode
  }

  getMessage() {
    return this.message
  }
}

export const ERROR_CODES = [
  new ErrorCode('INVALID_REQUEST_DATA', t('요청 데이터가 유효하지 않습니다.')),
  new ErrorCode('INVALID_CONDITION', t('요청 데이터 Condition이 유효하지 않습니다.')),
  new ErrorCode('FORMULA_MISMATCH', t('요청 데이터가 정의된 모델 Formula와 맞지 않습니다.')),
  new ErrorCode('FORMULA_EXECUTION_ERROR', t('모델 Formula 실행 중 에러가 발생하였습니다.')),
  new ErrorCode('EXTERNAL_SERVER_COMMUNICATION_ERROR', t('외부 서버와 통신 중 에러가 발생하였습니다.')),
  new ErrorCode('INTERNAL_SERVER_ERROR', t('요청 처리 중 서버 에러가 발생하였습니다.')),
]

export default ERROR_CODES

export const getErrorMsgByCode = (errorCode) => {
  return ERROR_CODES.find((it) => errorCode === it.getCode())?.getMessage() || errorCode
}

export const APC_STATUS = {
  WAITING: {
    icon: 'pending',
    color: 'grey',
    text: t('대기'),
    isFinishStatus: false,
  },
  RUNNING: {
    icon: 'more_horiz',
    color: 'secondary',
    text: t('진행중'),
    isFinishStatus: false,
  },
  ERROR: {
    icon: 'error',
    color: 'red',
    text: t('에러'),
    isFinishStatus: true,
  },
  NOT_FOUND: {
    icon: 'search_off',
    color: 'info',
    text: t('모델 없음'),
    isFinishStatus: true,
  },
  SUCCESS: {
    icon: 'check',
    color: 'primary',
    text: t('완료'),
    isFinishStatus: true,
  },
  COMPLETE: {
    icon: 'check',
    color: 'primary',
    text: t('완료'),
    isFinishStatus: true,
  },
  CANCEL: {
    icon: 'close',
    color: 'orange',
    text: t('취소됨'),
    isFinishStatus: true,
  },
}
