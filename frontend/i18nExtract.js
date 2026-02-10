const axios = require('axios')
const fs = require('fs')
const { execSync } = require('child_process')

const LANGUAGES_API_URL = 'http://localhost:8081/api/meta/i18n/languages'
const MESSAGES_API_URL = 'http://localhost:8081/api/meta/i18n/messages?lang='

const WORK_DIR = './i18n'
const VUE_I18N_EXTRACT_REPORT_FILE_PATH = `${WORK_DIR}/report.json`
const EXTRACT_MISSING_KEYS_FILE_PATH = `${WORK_DIR}/missingKeys.result`

const getLanguages = async () => {
  let { data } = await axios.get(LANGUAGES_API_URL)
  return data
}

const getMessages = async (lang) => {
  const { data } = await axios.get(`${MESSAGES_API_URL}${lang}`)
  return data.messages
}

const writeLanguageFile = async (lang, messages) => {
  const prettyJsonStringMessage = JSON.stringify(messages, null, 2)
  await fs.writeFileSync(`${WORK_DIR}/${lang}.json`, prettyJsonStringMessage, { encoding: 'utf-8' })
}

const executeVueI18nExtract = () => {
  execSync(`npm run vue-i18n-extract`, { stdio: 'inherit' })
}

const removeReportFile = async () => {
  try {
    await fs.unlinkSync(VUE_I18N_EXTRACT_REPORT_FILE_PATH)
  } catch (error) {}
}

const parseReportAndExtractMissingKeys = async () => {
  const report = await fs.readFileSync(VUE_I18N_EXTRACT_REPORT_FILE_PATH, { encoding: 'utf-8' })
  const parsedReport = JSON.parse(report)

  const missingKeys = new Set(parsedReport.missingKeys.map((info) => info.path))
  const dynamicKeys = new Set(parsedReport.maybeDynamicKeys.map((info) => info.path))

  const mergedKeys = new Set([...missingKeys, ...dynamicKeys])

  console.log(
    `total missing keys: ${mergedKeys.size}, missing keys: ${missingKeys.size}, dynamic keys: ${dynamicKeys.size}`,
  )

  await fs.writeFileSync(EXTRACT_MISSING_KEYS_FILE_PATH, new Array(...mergedKeys).join('\n'), { encoding: 'utf-8' })

  console.log(`Extracted missing keys are saved in ${EXTRACT_MISSING_KEYS_FILE_PATH}`)
}

const run = async () => {
  await fs.mkdirSync(WORK_DIR, { recursive: true })
  await removeReportFile()
  let languages = await getLanguages()

  for (let lang of languages) {
    const messages = await getMessages(lang)
    await writeLanguageFile(lang, messages)
  }

  await executeVueI18nExtract()
  await parseReportAndExtractMissingKeys()
}

run()
