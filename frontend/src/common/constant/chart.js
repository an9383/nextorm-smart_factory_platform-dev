export const COLORS = [
  '#3366CC',
  '#FF9900',
  '#109618',
  '#990099',
  '#0099C6',
  '#66AA00',
  '#316395',
  '#22AA99',
  '#6633CC',
  '#E67300',
  '#994499',
  '#AAAA11',
  '#329262',
  '#5574A6',
  '#3B3EAC',
  '#B77322',
  '#7A68A6',
  '#A6761D',
  '#666666',
  '#17BECF',
]

export const pickColor = () => {
  return COLORS[Math.floor(Math.random() * COLORS.length)]
}

export const getColor = (zeroBaseIndex) => {
  return COLORS[zeroBaseIndex % COLORS.length]
}

export const specLimitColor = '#ED5565'
export const controlLimitColor = '#FF7012'
