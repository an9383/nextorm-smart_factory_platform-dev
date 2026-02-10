<template>
  <ECharts
    id="echart"
    ref="myChart"
    :option="options"
    :resizable="true"
    autoresize
    @mouseover="mouseover"
    @globalout="mouseout"
  ></ECharts>
</template>

<script setup>
import ECharts from 'vue-echarts'
import 'echarts-gl'
import { defineProps, ref } from 'vue'
import { useWidgetRefresh, widgetProps } from 'src/common/module/widgetCommon'
import { t } from '/src/plugins/i18n'
import { date } from 'quasar'
import HealthDataService from 'src/services/parameterData/HealthDataService'

defineProps(widgetProps)
const myChart = ref()
const optionData = ref([])
const options = ref()

const refresh = async (config) => {
  const { period, parameterIds } = config
  let to, from

  if (period === 'PERIOD') {
    //기간 선택
    to = new Date(config.to)
    from = new Date(config.from)
  } else {
    to = new Date()
    from = date.subtractFromDate(to, { minutes: period })
  }

  const parametersHealthGrade = await HealthDataService.getHeatMapHealthData(
    parameterIds,
    from.toISOString(),
    to.toISOString(),
  )
  setOptions(parametersHealthGrade)
}

const setOptions = (result) => {
  const { good, normal, bad } = result.reduce(
    (acc, value) => {
      if (value.healthScore >= 90) {
        acc.good.push(value)
      } else if (value.healthScore > 50) {
        acc.normal.push(value)
      } else {
        acc.bad.push(value)
      }
      return acc
    },
    { good: [], normal: [], bad: [] },
  )
  optionData.value = [
    {
      name: t('좋음'),
      value: good.length,
      itemStyle: {
        color: '#3ad33a',
      },
    },
    {
      name: t('보통'),
      value: normal.length,
      itemStyle: {
        color: '#FF8000',
      },
    },
    {
      name: t('나쁨'),
      value: bad.length,
      itemStyle: {
        color: '#FF0000',
      },
    },
  ]
  const option = getPie3D(optionData.value, 0)
  option.series.push({
    name: 'pie2d',
    type: 'pie',
    labelLine: {
      length: 15,
      length2: 15,
    },
    startAngle: -30,
    clockwise: false,
    data: optionData.value.map((value) => (value.value !== 0 ? value : '')), //라벨 빈 배열[] 일경우 2d차트 고장 또는 빈값 리턴
    radius: 90,
    itemStyle: {
      opacity: 0,
    },
    label: {
      show: true,
      opacity: 1,
    },
  })
  options.value = option
}

const getPie3D = (pieData, internalDiameterRatio) => {
  let series = []
  let sumValue = 0
  let startValue = 0
  let endValue = 0
  let legendData = []
  let legendBfb = []
  let k = 1 - internalDiameterRatio
  pieData.sort((a, b) => {
    return b.value - a.value
  })
  for (let i = 0; i < pieData.length; i++) {
    sumValue += pieData[i].value
    let seriesItem = {
      name: typeof pieData[i].name === 'undefined' ? `series${i}` : pieData[i].name,
      type: 'surface',
      parametric: true,
      wireframe: {
        show: false,
      },
      pieData: pieData[i],
      pieStatus: {
        selected: false,
        hovered: false,
        k: k,
      },
      label: {
        show: true,
        position: 'outside',
        rich: {
          b: {
            color: '#7BC0CB',
            fontSize: 12,
            lineHeight: 20,
          },
          c: {
            fontSize: 16,
          },
        },
      },
    }

    if (typeof pieData[i].itemStyle != 'undefined') {
      let itemStyle = {}
      typeof pieData[i].itemStyle.color != 'undefined' ? (itemStyle.color = pieData[i].itemStyle.color) : null
      typeof pieData[i].itemStyle.opacity != 'undefined' ? (itemStyle.opacity = pieData[i].itemStyle.opacity) : null
      seriesItem.itemStyle = itemStyle
    }
    series.push(seriesItem)
  }

  legendData = []
  legendBfb = []
  for (let i = 0; i < series.length; i++) {
    endValue = startValue + series[i].pieData.value
    series[i].pieData.startRatio = startValue / sumValue
    series[i].pieData.endRatio = endValue / sumValue
    series[i].parametricEquation = getParametricEquation(
      series[i].pieData.startRatio,
      series[i].pieData.endRatio,
      false,
      false,
      k,
      series[i].pieData.value,
    )
    startValue = endValue
    let bfb = formatFloat(series[i].pieData.value / sumValue, 4)
    legendData.push({
      name: series[i].name,
      value: bfb,
    })
    legendBfb.push({
      name: series[i].name,
      value: bfb,
    })
  }
  // eslint-disable-next-line no-unused-vars
  let boxHeight = getHeight3D(series, 26)
  options.value = {
    legend: {
      data: legendData,
      orient: 'horizontal',
      left: 10,
      top: 10,
      itemGap: 10,
      textStyle: {
        color: '#353c4d',
      },
      show: true,
      icon: 'circle',
      formatter: (param) => {
        let item = legendBfb.filter((item) => item.name === param)[0]
        let bfs = formatFloat(item.value * 100, 2) + '%'
        return `${item.name}  ${bfs}`
      },
      selectedMode: false,
    },
    labelLine: {
      show: true,
      lineStyle: {
        color: '#7BC0CB',
      },
    },
    label: {
      show: true,
      position: 'outside',
      rich: {
        b: {
          color: '#7BC0CB',
          fontSize: 12,
          lineHeight: 20,
        },
        c: {
          fontSize: 16,
        },
      },
      formatter: '{b|{b} \n}{c|{c}}{b|  point}',
    },
    tooltip: {
      formatter: (params) => {
        if (isNaN(options.value.series[0].pieData.startRatio)) {
          return
        }
        if (params.seriesName !== 'mouseoutSeries' && params.seriesName !== 'pie2d') {
          let bfb = (
            (options.value.series[params.seriesIndex].pieData.endRatio -
              options.value.series[params.seriesIndex].pieData.startRatio) *
            100
          ).toFixed(2)
          return (
            `${params.seriesName}<br/>` +
            `<span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:${params.color};"></span>` +
            `${bfb}%`
          )
        }
      },
    },
    xAxis3D: {
      min: -1,
      max: 1,
    },
    yAxis3D: {
      min: -1,
      max: 1,
    },
    zAxis3D: {
      min: -1,
      max: 1,
    },
    grid3D: {
      show: false,
      boxHeight: 30,
      viewControl: {
        alpha: 40,
        distance: 400,
        rotateSensitivity: 0,
        zoomSensitivity: 0,
        panSensitivity: 0,
        autoRotate: false,
      },
    },
    series: series,
  }
  return options.value
}
const getHeight3D = (series, height) => {
  series.sort((a, b) => {
    return b.pieData.value - a.pieData.value
  })
  return (height * 25) / series[0].pieData.value
}
const getParametricEquation = (startRatio, endRatio, isSelected, isHovered, k /*, h*/) => {
  let midRatio = (startRatio + endRatio) / 2
  let startRadian = startRatio * Math.PI * 2
  let endRadian = endRatio * Math.PI * 2
  let midRadian = midRatio * Math.PI * 2
  if (startRatio === 0 && endRatio === 1) {
    isSelected = false
  }
  k = typeof k !== 'undefined' ? k : 1 / 3
  let offsetX = isSelected ? Math.cos(midRadian) * 0.1 : 0
  let offsetY = isSelected ? Math.sin(midRadian) * 0.1 : 0
  let hoverRate = isHovered ? 1.05 : 1
  return {
    u: {
      min: -Math.PI,
      max: Math.PI * 3,
      step: Math.PI / 32,
    },
    v: {
      min: 0,
      max: Math.PI * 2,
      step: Math.PI / 20,
    },
    x: (u, v) => {
      if (u < startRadian) {
        return offsetX + Math.cos(startRadian) * (1 + Math.cos(v) * k) * hoverRate
      }
      if (u > endRadian) {
        return offsetX + Math.cos(endRadian) * (1 + Math.cos(v) * k) * hoverRate
      }
      return offsetX + Math.cos(u) * (1 + Math.cos(v) * k) * hoverRate
    },
    y: (u, v) => {
      if (u < startRadian) {
        return offsetY + Math.sin(startRadian) * (1 + Math.cos(v) * k) * hoverRate
      }
      if (u > endRadian) {
        return offsetY + Math.sin(endRadian) * (1 + Math.cos(v) * k) * hoverRate
      }
      return offsetY + Math.sin(u) * (1 + Math.cos(v) * k) * hoverRate
    },
    // z: (u, v) => {
    //   if (u < -Math.PI * 0.5) {
    //     return Math.sin(u)
    //   }
    //   if (u > Math.PI * 2.5) {
    //     return Math.sin(u) * h * 0.1
    //   }
    //   return Math.sin(v) > 0 ? 1 * h * 0.1 : -1
    // },
    z: (u, v) => {
      return Math.sin(v) > 0 ? 1 : -1 // 고정된 높이 값을 사용
    },
  }
}
const formatFloat = (num, n) => {
  let f = parseFloat(num)
  if (isNaN(f)) {
    return false
  }
  f = Math.round(num * Math.pow(10, n)) / Math.pow(10, n) // n 幂
  let s = f.toString()
  let rs = s.indexOf('.')
  if (rs < 0) {
    rs = s.length
    s += '.'
  }
  while (s.length <= rs + n) {
    s += '0'
  }
  return s
}

let hoveredIndex = ''
const mouseover = (params) => {
  if (isNaN(options.value.series[0].pieData.startRatio)) {
    return
  }
  let isSelected
  let isHovered
  let startRatio
  let endRatio
  let k
  if (hoveredIndex === params.seriesIndex) {
    return false
  } else {
    if (hoveredIndex !== '') {
      isSelected = options.value.series[hoveredIndex].pieStatus.selected
      isHovered = false
      startRatio = options.value.series[hoveredIndex].pieData.startRatio
      endRatio = options.value.series[hoveredIndex].pieData.endRatio
      k = options.value.series[hoveredIndex].pieStatus.k
      options.value.series[hoveredIndex].parametricEquation = getParametricEquation(
        startRatio,
        endRatio,
        isSelected,
        isHovered,
        k,
        options.value.series[hoveredIndex].pieData.value,
      )
      options.value.series[hoveredIndex].pieStatus.hovered = isHovered
      hoveredIndex = ''
    }
    if (params.seriesName !== 'mouseoutSeries' && params.seriesName !== 'pie2d') {
      isSelected = options.value.series[params.seriesIndex].pieStatus.selected
      isHovered = true
      startRatio = options.value.series[params.seriesIndex].pieData.startRatio
      endRatio = options.value.series[params.seriesIndex].pieData.endRatio
      k = options.value.series[params.seriesIndex].pieStatus.k
      options.value.series[params.seriesIndex].parametricEquation = getParametricEquation(
        startRatio,
        endRatio,
        isSelected,
        isHovered,
        k,
        options.value.series[params.seriesIndex].pieData.value + 5,
      )
      options.value.series[params.seriesIndex].pieStatus.hovered = isHovered
      hoveredIndex = params.seriesIndex
    }
  }
}
const mouseout = () => {
  if (isNaN(options.value.series[0].pieData.startRatio)) {
    return
  }
  let isSelected
  let isHovered
  let startRatio
  let endRatio
  let k
  if (hoveredIndex !== '') {
    isSelected = options.value.series[hoveredIndex].pieStatus.selected
    isHovered = false
    k = options.value.series[hoveredIndex].pieStatus.k
    startRatio = options.value.series[hoveredIndex].pieData.startRatio
    endRatio = options.value.series[hoveredIndex].pieData.endRatio
    options.value.series[hoveredIndex].parametricEquation = getParametricEquation(
      startRatio,
      endRatio,
      isSelected,
      isHovered,
      k,
      options.value.series[hoveredIndex].pieData.value,
    )
    options.value.series[hoveredIndex].pieStatus.hovered = isHovered
    hoveredIndex = ''
  }
}
useWidgetRefresh(refresh)
</script>

<style scoped></style>
