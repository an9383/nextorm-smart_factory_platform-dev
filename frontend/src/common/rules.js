/* eslint-disable no-unused-vars */
import _ from 'lodash'
import { t } from '/src/plugins/i18n'
export default {
  required: (value) => {
    if (_.isArray(value)) {
      return !_.isEmpty(value) || t('필수값 입니다.')
    } else {
      return (value !== undefined && value !== null && value !== '') || t('필수값 입니다.')
    }
  },
  ascii: (value) => {
    const pattern = /^[ -~]+$/
    return !value || pattern.test(value) || t('영문 대/소문자, 숫자, 특수문자만 입력 가능합니다.')
  },
  email: (value) => {
    const pattern =
      /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
    return !value || pattern.test(value) || t('이메일 형식이 아닙니다.')
  },
  minLength: (limit) => {
    return (value) => !value || value.length >= limit || `${limit}${t('자 이상 입력 해야합니다.')}`
  },
  maxLength: (limit) => {
    return (value) => !value || value.length <= limit || `${limit}${t('자 까지 입력이 가능합니다.')}`
  },
  regexp: (regexp, msg) => {
    return (value) => !value || regexp.test(value) || msg
  },
  dateRange: (value) => {
    return value.split('~').length === 2 || t('기간으로 선택해야 합니다.')
  },
  min: (minVal) => {
    return (value) => {
      const valueNum = Number(value === null || value === '' ? undefined : value)
      return _.isNaN(valueNum) || valueNum >= Number(minVal) || `${minVal}${t('보다 크거나 같아야 합니다.')}`
    }
  },
  max: (maxVal) => {
    return (value) => {
      const valueNum = Number(value === null || value === '' ? undefined : value)
      return _.isNaN(valueNum) || value <= Number(maxVal) || `${maxVal}${t('보다 작거나 같아야 합니다.')}`
    }
  },
  realNumber: (value) => {
    const pattern = /^[-]?[0-9]+(.[0-9]+)?$/
    return !value || pattern.test(value) || t('실수가 아닙니다.')
  },
  dateTimeFormat: (value) => {
    const pattern = /[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1]) (0[0-9]|1[0-9]|2[0-4]):(0[0-9]|[1-5][0-9])/
    return !value || pattern.test(value) || t('시간 형태가 잘못되었습니다.')
  },
  decimal: (maxDigit) => {
    return (value) => {
      const pattern = new RegExp(`(^[+-]?\\d+$)|(^[+-]?\\d+\\.\\d{0,${maxDigit}}$)`)
      return !value || pattern.test(value) || t(`소수점은 ${maxDigit}자리까지 입력 가능합니다.`)
    }
  },
  integer: (maxDigit) => {
    return (value) => {
      const pattern = new RegExp(`^-?[0-9]{0,${maxDigit}}?$`)
      return !value || pattern.test(parseInt(value)) || t(`정수는 ${maxDigit}자리까지 입력 가능합니다.`)
    }
  },
  onlyInteger: (value) => {
    const pattern = /^-?[0-9]+$/
    return !value || pattern.test(value) || t('정수만 입력 가능합니다.')
  },
  betweenVal: (min, max) => {
    const minNum = Number(min === null || min === '' ? undefined : min)
    const maxNum = Number(max === null || max === '' ? undefined : max)
    return (value) => {
      const valueNum = Number(value === null || value === '' ? undefined : value)
      if (isNaN(valueNum)) {
        return true
      } else if (!isNaN(minNum) && !isNaN(maxNum)) {
        return (minNum <= valueNum && maxNum >= valueNum) || `${min} ~ ${max} ${t('사이의 값이어야 합니다.')}`
      } else if (!isNaN(minNum)) {
        return minNum <= valueNum || `${min}${t('보다 크거나 같아야 합니다.')}`
      } else if (!isNaN(maxNum)) {
        return maxNum >= valueNum || `${max}${t('보다 작거나 같아야 합니다.')}`
      }
      return true
    }
  },
  url: (value) => {
    const pattern = /^(?:http(s)?:\/\/)?[\w.-]+(?:\.[\w\\.-]+)+[\w\-\\._~:/?#[\]@!\\$&'\\(\\)\\*\\+,;=.]+$/gm
    return !value || pattern.test(value) || t('URL 형식이 아닙니다.')
  },
  phoneNumber: (value) => {
    const pattern = /^[0-9;-]+$/
    return !value || pattern.test(value) || t('"-",숫자만 입력 가능합니다.')
  },
  companyRegNum: (value) => {
    const pattern = /^[0-9]{3}-[0-9]{2}-[0-9]{5}$/
    return !value || pattern.test(value) || t('사업자등록번호 형식이 아닙니다. XXX-XX-XXXXX')
  },
  corporateRegNum: (value) => {
    const pattern = /^[0-9]{6}-[0-9]{7}$/
    return !value || pattern.test(value) || t('법인등록번호 형식이 아닙니다. XXXXXX-XXXXXXX')
  },
  natural: (value) => {
    const pattern = /^$|^[0-9]$|^[1-9][0-9]*$/
    return !value || pattern.test(value) || t('자연수만 입력 가능합니다.')
  },
  camelCase: (value) => {
    const pattern = /^[a-z]([A-Za-z0-9])*$/
    return !value || pattern.test(value) || t('Camel Case 형식만 입력 가능합니다.')
  },
  notIn: (notInList, msg) => {
    return (value) => {
      return !value || !(notInList || []).includes(value) || msg || t('목록에 포함되지 않는 값만 입력 가능합니다.')
    }
  },
  fileUploadMaxSize: (maxVal) => {
    return (file) => {
      const maxSize = maxVal * 1024 * 1024
      return !file || !(file.size > maxSize) || t(`${maxVal}MB 이상은 업로드 할 수 없습니다.`)
    }
  },
  password: (value) => {
    const pattern = /^(?=.*[a-zA-Z0-9\W_])(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$/
    return !value || pattern.test(value) || t('패스워드 조합은 영문/숫자 조합으로 8자리 이상이여야 합니다.')
  },
  passwordConfirm: (password) => {
    return (value) => {
      return password === value || t('패스워드 와 패스워드 확인이 다릅니다.')
    }
  },
}
