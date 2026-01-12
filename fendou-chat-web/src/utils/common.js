/**
 * 获取当前时间
 *
 * @returns {string}
 */
export function getFormatDate() {
  const date = new Date()
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/**
 * 生成随机字符串
 *
 * @param {number} length 长度
 * @returns {string}
 */
export function generateRandomString(length) {
  const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let result = ''
  for (let i = 0; i < length; i++) {
    const randomIndex = Math.floor(Math.random() * characters.length)
    result += characters[randomIndex]
  }
  return result
}

/**
 * 格式化文件大小
 *
 * @param byte 字节
 * @returns {string}
 */
export function formatFileSize(byte) {
  if (byte === 0 || isNaN(byte)) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(byte) / Math.log(k))
  return parseFloat((byte / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
