/* eslint-disable no-unused-vars */
/* eslint-disable no-inner-declarations */
/* eslint-disable no-undef */
import * as THREE from 'three'
import { REVISION } from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls'
import { ImprovedNoise } from './ImprovedNoise.js'

export default class Gio3DMap {
  constructor(target) {
    console.log('Three Version: ', REVISION)
    this.init(target, 500, 500)
    this.initAxesHelp()
    // this.initGridHelp();
    this.initController()
    this.addLights()
    this.render()
    this.animate()
  }

  init(target, width, height) {
    this.worldWidth = width
    this.worldDepth = height
    this.worldHalfWidth = this.worldWidth / 2
    this.worldHalfDepth = this.worldDepth / 2

    this.target = target
    const innerWidth = target.offsetWidth
    const innerHeight = target.offsetHeight
    this.aspect = innerWidth / innerHeight

    this.scene = new THREE.Scene()
    this.scene.background = new THREE.Color(0xbfd1e5)

    this.camera = new THREE.PerspectiveCamera(60, this.aspect, 10, 20000)

    this.aspect = innerWidth / innerHeight
    this.renderer = new THREE.WebGLRenderer({
      antialias: true,
      alpha: true,
    })

    this.renderer.setSize(innerWidth, innerHeight)
    this.render()
    target.append(this.renderer.domElement)

    if (this.baseHeight == null) {
      this.baseHeight = 0
    }

    this.data = []

    this.data = this.generateHeight(this.worldWidth, this.worldDepth)

    this.createMesh()
  }

  async initData(minLong, minLat, maxLong, maxLat, width, height, data) {
    this.worldWidth = width
    this.worldDepth = height
    this.worldHalfWidth = this.worldWidth / 2
    this.worldHalfDepth = this.worldDepth / 2

    this.data = []
    this.data = await this.getHeightData(minLong, minLat, maxLong, maxLat, this.worldWidth, this.worldDepth, data)
    this.data = this.smoothData(this.data, this.worldWidth, this.worldDepth) // Add smoothing
    this.createMesh()
  }

  getAreaCal(area) {
    let minLong = null,
      minLat = null,
      maxLong = null,
      maxLat = null

    for (let i = 0; i < area.length; i++) {
      let value = area[i]
      if (minLong == null) {
        minLong = value[1]
      } else if (minLong > value[1]) {
        minLong = value[1]
      }
      if (minLat == null) {
        minLat = value[0]
      } else if (minLat > value[0]) {
        minLat = value[0]
      }
      if (maxLong == null) {
        maxLong = value[1]
      } else if (maxLong < value[1]) {
        maxLong = value[1]
      }
      if (maxLat == null) {
        maxLat = value[0]
      } else if (maxLat < value[0]) {
        maxLat = value[0]
      }
    }

    let width = Math.floor((maxLong - minLong) * 10000)
    let height = Math.floor((maxLat - minLat) * 10000)

    return { minLong, minLat, maxLong, maxLat, width, height }
  }

  setData(area, data) {
    let { minLong, minLat, maxLong, maxLat, width, height } = this.getAreaCal(area)

    this.initData(minLong, minLat, maxLong, maxLat, width, height, data)
    this.createPolygon(area, minLong, minLat, maxLong, maxLat)
  }

  createMesh() {
    this.createTerrain(this.data)
  }

  initAxesHelp() {
    this.axesHelper = new THREE.AxesHelper(10000)
    this.scene.add(this.axesHelper)
  }

  initGridHelp() {
    this.gridHelper = new THREE.GridHelper(10000, 10000)
    this.scene.add(this.gridHelper)
  }

  initController() {
    this.controller = new OrbitControls(this.camera, this.renderer.domElement)

    this.controller.minDistance = 1
    this.controller.maxDistance = 10000

    // 평면 밑으로 안내려 가게 설정
    this.controller.maxPolarAngle = Math.PI

    this.controller.target.y = -1000 // 초기 타겟을 더 아래로 설정
    // this.controller.target.set(0,-2500, 0)
    this.camera.position.set(8000, 500, 0) // 초기 카메라 위치 설정
    this.camera.lookAt(this.controller.target)

    this.controller.update()
  }

  addLights() {
    const ambientLight = new THREE.AmbientLight(0x404040, 1.5) // 강한 주변 광원
    this.scene.add(ambientLight)

    const directionalLight1 = new THREE.DirectionalLight(0xffffff, 1.5) // 방향성 광원
    directionalLight1.position.set(1, 1, 1).normalize()
    this.scene.add(directionalLight1)

    const directionalLight2 = new THREE.DirectionalLight(0xffffff, 0.75) // 추가 방향성 광원
    directionalLight2.position.set(-1, -1, -1).normalize()
    this.scene.add(directionalLight2)
  }

  render() {
    this.renderer.render(this.scene, this.camera)
  }

  createTerrain(coords) {
    const geometry = new THREE.PlaneGeometry(7500, 7500, this.worldWidth - 1, this.worldDepth - 1)
    geometry.rotateX((-90 * Math.PI) / 180)
    geometry.rotateY((90 * Math.PI) / 180)

    let vertices = geometry.attributes.position.array
    const alphas = new Float32Array(vertices.length / 3)
    for (let i = 0, j = 0, l = vertices.length; i < l; i++, j += 3) {
      vertices[j + 1] = -coords[i] * 5 // Y축 값을 반전시키고, 값을 확대합니다.
      alphas[i] = coords[i] === 0 ? 0 : 1
    }

    geometry.setAttribute('alpha', new THREE.BufferAttribute(alphas, 1))
    let texture = new THREE.CanvasTexture(this.generateTexture(coords, this.worldWidth, this.worldDepth))
    texture.wrapS = THREE.ClampToEdgeWrapping
    texture.wrapT = THREE.ClampToEdgeWrapping
    texture.colorSpace = THREE.SRGBColorSpace

    let material = new THREE.MeshStandardMaterial({
      map: texture,
      side: THREE.DoubleSide,
      opacity: 0.7,
      transparent: true,
      onBeforeCompile: (shader) => {
        shader.vertexShader = `
          attribute float alpha;
          varying float vAlpha;
          ${shader.vertexShader}
        `.replace(
          `#include <begin_vertex>`,
          `#include <begin_vertex>
          vAlpha = alpha;`,
        )
        shader.fragmentShader = `
          varying float vAlpha;
          ${shader.fragmentShader}
        `.replace(
          `#include <dithering_fragment>`,
          `#include <dithering_fragment>
          gl_FragColor.a *= vAlpha;`,
        )
      },
    })
    let mesh = new THREE.Mesh(geometry, material)
    mesh.name = 'myMap'
    let obj = this.scene.getObjectByName(mesh.name)
    if (obj != null) {
      this.scene.remove(obj)
    }
    this.scene.add(mesh)

    // this.renderer.render(this.scene, this.camera)
  }

  createPolygon(area, minLong, minLat, maxLong, maxLat) {
    const existingPolygon = this.scene.getObjectByName('polygon')
    if (existingPolygon) {
      this.scene.remove(existingPolygon)
    }
    const shape = new THREE.Shape()
    area.forEach(([lat, lng], index) => {
      const pos = this.latLngToPosition(lat, lng, minLong, minLat, maxLong, maxLat)
      if (index === 0) {
        shape.moveTo(pos.x, pos.z)
      } else {
        shape.lineTo(pos.x, pos.z)
      }
    })

    // 다각형을 닫습니다.
    shape.closePath()
    const geometry = new THREE.ShapeGeometry(shape)
    const material = new THREE.MeshBasicMaterial({
      color: 0x0098b3,
      side: THREE.DoubleSide,
      opacity: 0.5,
      transparent: true,
    })
    const mesh = new THREE.Mesh(geometry, material)
    mesh.name = 'polygon'
    mesh.rotation.x = Math.PI / 2
    mesh.rotation.z = -Math.PI / 2
    mesh.position.y = 0
    this.scene.add(mesh)
  }

  latLngToPosition(lat, lng, minLong, minLat, maxLong, maxLat) {
    const scaleX = 7500 / (maxLong - minLong)
    const scaleZ = 7500 / (maxLat - minLat)
    return {
      x: (lng - minLong) * scaleX - 3750,
      z: -(lat - minLat) * scaleZ + 3750,
    }
  }

  generateHeight(width, height) {
    const size = width * height,
      data = new Uint8Array(size),
      perlin = new ImprovedNoise(),
      z = Math.random() * 100

    let quality = 1

    for (let j = 0; j < 4; j++) {
      for (let i = 0; i < size; i++) {
        const x = i % width,
          y = ~~(i / width)
        data[i] += Math.abs(perlin.noise(x / quality, y / quality, z) * quality * 1.75)
      }

      quality *= 5
    }

    return data
  }

  async getHeightData(minLong, minLat, maxLong, maxLat, width, height, srcData) {
    let size = width * height,
      data = new Uint8Array(size)

    let maxDepth = null,
      minDepth = null

    for (let i = 0; i < srcData.length; i++) {
      let depth = srcData[i].depthData
      if (minDepth == null) {
        minDepth = depth
      } else if (minDepth > depth) {
        minDepth = depth
      }
      if (maxDepth == null) {
        maxDepth = depth
      } else if (maxDepth < depth) {
        maxDepth = depth
      }
    }

    for (let i = 0; i < srcData.length; i++) {
      let longitude = srcData[i].longitude // 경도
      let latitude = srcData[i].latitude // 위도
      let depth = srcData[i].depthData
      let x = Math.floor(((longitude - minLong) * (width - 1)) / (maxLong - minLong))
      let y = Math.floor(((maxLat - latitude) * (height - 1)) / (maxLat - minLat))
      const pos = x + y * width

      if (pos >= 0 && pos < size) {
        let normalizedDepth = this.normalizeDepth(depth, minDepth, maxDepth)
        if (normalizedDepth == 0) {
          normalizedDepth = 256 / 2
        }
        data[pos] = normalizedDepth
      }
    }

    // 깊이 값을 보간합니다.
    data = this.interpolateDepths(data, width, height)

    // 깊이 값을 반전시킵니다.
    for (let i = 0; i < data.length; i++) {
      data[i] = -data[i] // 깊이 값을 음수로 반전시킵니다.
    }

    return data
  }

  interpolateDepths(data, width, height) {
    const newData = data.slice()

    for (let y = 1; y < height - 1; y++) {
      for (let x = 1; x < width - 1; x++) {
        const pos = x + y * width
        if (data[pos] === 0) {
          const neighbors = this.getNeighbors(data, x, y, width, height)
          if (neighbors.length > 0) {
            newData[pos] = this.calculateCatmullRom(neighbors)
          }
        }
      }
    }

    return newData
  }

  getNeighbors(data, x, y, width, height) {
    const neighbors = []

    if (x > 0 && data[x - 1 + y * width] !== 0) neighbors.push(data[x - 1 + y * width])
    if (x < width - 1 && data[x + 1 + y * width] !== 0) neighbors.push(data[x + 1 + y * width])
    if (y > 0 && data[x + (y - 1) * width] !== 0) neighbors.push(data[x + (y - 1) * width])
    if (y < height - 1 && data[x + (y + 1) * width] !== 0) neighbors.push(data[x + (y + 1) * width])

    return neighbors
  }

  calculateCatmullRom(points) {
    points.sort((a, b) => a - b)
    const p0 = points[0]
    const p1 = points[1]
    const p2 = points[2] || p1
    const p3 = points[3] || p2

    const t = 0.5 // Catmull-Rom parameter

    const v0 = (p2 - p0) * t
    const v1 = (p3 - p1) * t

    const a = 2 * p1 - 2 * p2 + v0 + v1
    const b = -3 * p1 + 3 * p2 - 2 * v0 - v1
    const c = v0
    const d = p1

    return a * Math.pow(t, 3) + b * Math.pow(t, 2) + c * t + d
  }

  smoothData(data, width, height) {
    const smoothedData = new Uint8Array(data)
    const kernel = [
      [1, 2, 1],
      [2, 4, 2],
      [1, 2, 1],
    ]
    const kernelWeight = 16

    for (let y = 1; y < height - 1; y++) {
      for (let x = 1; x < width - 1; x++) {
        let sum = 0
        for (let ky = -1; ky <= 1; ky++) {
          for (let kx = -1; kx <= 1; kx++) {
            const pos = x + kx + (y + ky) * width
            sum += data[pos] * kernel[ky + 1][kx + 1]
          }
        }
        const pos = x + y * width
        smoothedData[pos] = sum / kernelWeight
      }
    }
    return smoothedData
  }

  normalizeDepth(depth, minDepth, maxDepth) {
    if (depth < minDepth || depth > maxDepth) {
      return 0 // Return 0 if depth is out of range
    }

    return 255 - Math.floor(((depth - minDepth) / (maxDepth - minDepth)) * 255)
  }

  generateTexture(data, width, height) {
    let context, image, imageData, shade

    const vector3 = new THREE.Vector3(0, 0, 0)
    const sun = new THREE.Vector3(1, 1, 1)
    sun.normalize()

    const canvas = document.createElement('canvas')
    canvas.width = width
    canvas.height = height

    context = canvas.getContext('2d')
    context.fillStyle = '#000'
    context.fillRect(0, 0, width, height)

    image = context.getImageData(0, 0, canvas.width, canvas.height)
    imageData = image.data

    for (let i = 0, j = 0, l = imageData.length; i < l; i += 4, j++) {
      vector3.x = data[j - 2] - data[j + 2]
      vector3.y = 2
      vector3.z = data[j - width * 2] - data[j + width * 2]
      vector3.normalize()

      shade = vector3.dot(sun)

      const depthValue = data[j] / 255

      // 깊이 값에 따라 색상을 다르게 설정
      if (depthValue < 0.3) {
        imageData[i] = 0 // Red
        imageData[i + 1] = 255 - Math.floor(depthValue * 255) // Green
        imageData[i + 2] = 255 // Blue
      } else if (depthValue < 0.6) {
        imageData[i] = 255 - Math.floor((depthValue - 0.3) * 255) // Red
        imageData[i + 1] = 128 // Green
        imageData[i + 2] = 255 - Math.floor((depthValue - 0.3) * 255) // Blue
      } else {
        imageData[i] = 255 // Red
        imageData[i + 1] = 0 // Green
        imageData[i + 2] = 128 + Math.floor((depthValue - 0.6) * 255) // Blue
      }
    }

    context.putImageData(image, 0, 0)

    const canvasScaled = document.createElement('canvas')
    canvasScaled.width = width * 4
    canvasScaled.height = height * 4

    context = canvasScaled.getContext('2d')
    context.scale(4, 4)
    context.drawImage(canvas, 0, 0)

    image = context.getImageData(0, 0, canvasScaled.width, canvasScaled.height)
    imageData = image.data

    for (let i = 0, l = imageData.length; i < l; i += 4) {
      const v = ~~(Math.random() * 5)

      imageData[i] += v
      imageData[i + 1] += v
      imageData[i + 2] += v
    }

    context.putImageData(image, 0, 0)

    return canvasScaled
  }

  animate() {
    requestAnimationFrame(() => {
      this.animate()
    })

    this.renderer.render(this.scene, this.camera)
  }
}
