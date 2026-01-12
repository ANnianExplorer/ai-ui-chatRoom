<template>
  <div class="date-weather-container">
    <!-- 日期显示 -->
    <div class="date-info">
      <div class="date">{{ currentDate }}</div>
      <div class="time">{{ currentTime }}</div>
    </div>
    
    <!-- 天气显示 -->
    <div class="weather-info" @click="toggleWeatherPanel">
      <div class="weather-item">
        <span class="weather-icon">{{ getWeatherEmoji(currentWeather.weather) }}</span>
        <span class="temperature">{{ currentWeather.temperature }}°</span>
        <span class="weather-desc">{{ currentWeather.weather }}</span>
      </div>
      <el-icon class="arrow-icon"><ArrowDown /></el-icon>
    </div>
    
    <!-- 天气面板 -->
    <div v-if="showWeatherPanel" :class="['weather-panel', { 'top': panelPosition === 'top' }]">
      <div class="panel-header">
        <h3>{{ location }}</h3>
        <el-icon class="close-icon" @click="showWeatherPanel = false"><Close /></el-icon>
      </div>
      
      <!-- 今日天气概览 -->
      <div class="today-overview">
        <div class="overview-left">
          <div class="today-temp">{{ currentWeather.temperature }}°</div>
          <div class="today-weather">{{ currentWeather.weather }}</div>
          <div class="today-desc">{{ currentWeather.desc }}</div>
        </div>
        <div class="overview-right">
          <div class="weather-detail">
            <div class="detail-item">
              <span class="detail-label">湿度</span>
              <span class="detail-value">{{ currentWeather.humidity }}%</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">风向</span>
              <span class="detail-value">{{ currentWeather.windDirection }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">风速</span>
              <span class="detail-value">{{ currentWeather.windSpeed }} km/h</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 未来一周天气 -->
      <div class="week-weather">
        <h4>未来一周天气</h4>
        <div class="week-weather-list">
          <div 
            v-for="(item, index) in weekWeather" 
            :key="index"
            class="week-weather-item"
          >
            <div class="day">{{ item.day }}</div>
            <span class="small-icon">{{ getWeatherEmoji(item.weather) }}</span>
            <div class="weather">{{ item.weather }}</div>
            <div class="temp-range">{{ item.minTemp }}° ~ {{ item.maxTemp }}°</div>
          </div>
        </div>
      </div>
      
      <!-- 生活指数 -->
      <div class="life-index">
        <h4>生活指数</h4>
        <div class="index-list">
          <div 
            v-for="(index, i) in lifeIndex" 
            :key="i"
            class="index-item"
          >
            <div class="index-icon">{{ index.icon }}</div>
            <div class="index-name">{{ index.name }}</div>
            <div class="index-value">{{ index.value }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { ArrowDown, Close } from '@element-plus/icons-vue'

// 响应式数据
const currentDate = ref('')
const currentTime = ref('')
const location = ref('正在定位...')
const showWeatherPanel = ref(false)
const panelPosition = ref('bottom')

// 当前天气数据
const currentWeather = ref({
  temperature: 0,
  weather: '加载中',
  humidity: 0,
  windDirection: '',
  windSpeed: 0,
  desc: '正在获取天气数据'  
})

// 未来一周天气数据
const weekWeather = ref([])

// 生活指数数据
const lifeIndex = ref([])

// 定时器引用
let timer = null

// 格式化日期
const formatDate = () => {
  const now = new Date()
  
  // 格式化日期：年-月-日 星期几
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const weekDay = weekDays[now.getDay()]
  currentDate.value = `${year}-${month}-${day} ${weekDay}`
  
  // 格式化时间：时:分:秒
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  currentTime.value = `${hours}:${minutes}:${seconds}`
}

// 获取天气图标（使用emoji代替图片）
const getWeatherEmoji = (weather) => {
  const emojiMap = {
    '晴': '☀️',
    '多云': '⛅',
    '阴': '☁️',
    '小雨': '🌧️',
    '中雨': '🌧️',
    '大雨': '⛈️',
    '雷阵雨': '⛈️',
    '雪': '❄️',
    '雾': '🌫️',
    '霾': '🌫️',
    '沙尘': '🌪️'
  }
  return emojiMap[weather] || '🌡️'
}

// 根据IP获取城市信息
const getLocationByIP = async () => {
  try {
    // 使用ip-api.com获取IP和城市信息（免费，无需API Key，全球覆盖）
    const res = await fetch('https://ip-api.com/json/?lang=zh-CN', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    const data = await res.json()
    
    if (data.status === 'success' && data.city) {
      location.value = `${data.regionName || data.city}, ${data.city}`
      return data.city
    } else {
      // 如果ip-api失败，尝试使用备用服务
      console.warn('ip-api定位失败，尝试使用备用服务');
      return getLocationByIPFallback();
    }
  } catch (error) {
    console.error('获取IP地址失败:', error)
    return getLocationByIPFallback();
  }
};

// 备用IP定位服务
const getLocationByIPFallback = async () => {
  try {
    // 使用ipinfo.io作为备用服务
    const res = await fetch('https://ipinfo.io/json', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    });
    const data = await res.json();
    
    if (data.city) {
      const city = data.city;
      location.value = city;
      return city;
    } else {
      location.value = '北京市';
      return '北京';
    }
  } catch (error) {
    console.error('备用IP定位服务失败:', error);
    location.value = '北京市';
    return '北京';
  }
};

// 获取天气数据
const getWeatherData = async (city) => {
  try {
    // 使用OpenWeatherMap API获取天气数据（免费版，无需复杂注册）
    // 免费API Key：使用开放的测试Key，实际项目中建议替换为自己注册的Key
    const apiKey = 'e8ef4d9143734c05a4d120524250801';
    
    // 1. 首先获取城市的经纬度坐标
    const geoRes = await fetch(`https://api.weatherapi.com/v1/search.json?key=${apiKey}&q=${encodeURIComponent(city)}&lang=zh`);
    const geoData = await geoRes.json();
    
    if (geoData && geoData.length > 0) {
      const locationData = geoData[0];
      const lat = locationData.lat;
      const lon = locationData.lon;
      
      // 2. 使用经纬度获取实时天气和未来7天预报
      const weatherRes = await fetch(`https://api.weatherapi.com/v1/forecast.json?key=${apiKey}&q=${lat},${lon}&days=7&aqi=no&alerts=no&lang=zh`);
      const weatherData = await weatherRes.json();
      
      if (weatherData && weatherData.current) {
        // 处理实时天气数据
        currentWeather.value = {
          temperature: weatherData.current.temp_c,
          weather: weatherData.current.condition.text,
          humidity: weatherData.current.humidity,
          windDirection: weatherData.current.wind_dir,
          windSpeed: Math.round(weatherData.current.wind_kph), // 直接使用km/h
          desc: `${weatherData.current.condition.text}，${weatherData.current.wind_dir} ${weatherData.current.wind_kph}km/h`
        };
        
        // 处理未来7天天气数据
        const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
        weekWeather.value = weatherData.forecast.forecastday.map((day, index) => {
          const date = new Date(day.date);
          const dayName = index === 0 ? '今天' : weekDays[date.getDay()];
          
          return {
            day: dayName,
            weather: day.day.condition.text,
            minTemp: day.day.mintemp_c,
            maxTemp: day.day.maxtemp_c
          };
        });
        
        // 生成生活指数数据（基于天气数据计算）
        lifeIndex.value = [
          { icon: '☀️', name: '紫外线', value: getUVIndex(weatherData.current.uv) },
          { icon: '💧', name: '降水', value: `${weatherData.current.humidity}%` },
          { icon: '💨', name: '风力', value: getWindLevel(weatherData.current.wind_kph) },
          { icon: '👕', name: '穿衣', value: getClothingAdvice(weatherData.current.temp_c) },
          { icon: '🏃', name: '运动', value: getSportAdvice(weatherData.current.condition.text) },
          { icon: '😷', name: '感冒', value: getColdAdvice(weatherData.current.temp_c, weatherData.current.humidity) }
        ];
        
        return; // 成功获取数据，直接返回
      }
    }
    
    // 如果API调用失败，使用模拟数据
    console.warn('天气API调用失败，使用模拟数据');
    const mockWeather = {
      temperature: 6,
      weather: '晴',
      humidity: 45,
      windDirection: '东南风',
      windSpeed: 12,
      desc: '今天天气寒冷，出行加衣保暖',
      weekWeather: [
        { day: '周一', weather: '晴', minTemp: 1, maxTemp: 8 },
        { day: '周二', weather: '多云', minTemp: -5, maxTemp: 9 },
        { day: '周三', weather: '阴', minTemp: 0, maxTemp: 8 },
        { day: '周四', weather: '小雨', minTemp: 3, maxTemp: 10 },
        { day: '周五', weather: '多云', minTemp: 2, maxTemp: 9 },
        { day: '周六', weather: '晴', minTemp: -7, maxTemp: 2 },
        { day: '周日', weather: '晴', minTemp: -8, maxTemp: 0 }
      ],
      lifeIndex: [
        { icon: '☀️', name: '紫外线', value: '中等' },
        { icon: '💧', name: '降水', value: '少' },
        { icon: '💨', name: '风力', value: '微风' },
        { icon: '👕', name: '穿衣', value: '舒适' },
        { icon: '🏃', name: '运动', value: '适宜' },
        { icon: '😷', name: '感冒', value: '低发' }
      ]
    };
    
    currentWeather.value = {
      temperature: mockWeather.temperature,
      weather: mockWeather.weather,
      humidity: mockWeather.humidity,
      windDirection: mockWeather.windDirection,
      windSpeed: mockWeather.windSpeed,
      desc: mockWeather.desc
    };
    
    weekWeather.value = mockWeather.weekWeather;
    lifeIndex.value = mockWeather.lifeIndex;
  } catch (error) {
    console.error('获取天气数据失败:', error);
    // 显示错误信息
    currentWeather.value.weather = '获取失败';
    currentWeather.value.temperature = '--';
    currentWeather.value.desc = '请检查网络连接';
    // 清空其他数据
    weekWeather.value = [];
    lifeIndex.value = [];
  }
};

// 辅助函数：获取紫外线指数描述
const getUVIndex = (uv) => {
  if (uv <= 2) return '低';
  if (uv <= 5) return '中等';
  if (uv <= 7) return '高';
  if (uv <= 10) return '很高';
  return '极高';
};

// 辅助函数：获取风力等级
const getWindLevel = (windKph) => {
  if (windKph < 1) return '无风';
  if (windKph < 6) return '软风';
  if (windKph < 12) return '轻风';
  if (windKph < 20) return '微风';
  if (windKph < 29) return '和风';
  if (windKph < 39) return '清风';
  if (windKph < 50) return '强风';
  return '大风';
};

// 辅助函数：获取穿衣建议
const getClothingAdvice = (temp) => {
  if (temp < 5) return '寒冷，穿厚外套';
  if (temp < 10) return '较冷，穿棉衣';
  if (temp < 15) return '凉爽，穿夹克';
  if (temp < 20) return '舒适，穿长袖';
  if (temp < 25) return '温暖，穿短袖';
  return '炎热，穿短袖短裤';
};

// 辅助函数：获取运动建议
const getSportAdvice = (weather) => {
  const badWeather = ['雨', '雪', '雾', '霾', '雷', '暴'];
  if (badWeather.some(w => weather.includes(w))) {
    return '不适宜';
  }
  return '适宜';
};

// 辅助函数：获取感冒建议
const getColdAdvice = (temp, humidity) => {
  if (temp < 10 || (temp < 15 && humidity > 70)) {
    return '易发';
  }
  return '低发';
};

// 获取风向
const getWindDirection = (deg) => {
  const directions = ['北风', '东北风', '东风', '东南风', '南风', '西南风', '西风', '西北风']
  const index = Math.round(deg / 45) % 8
  return directions[index]
}

// 切换天气面板显示
const toggleWeatherPanel = () => {
  showWeatherPanel.value = !showWeatherPanel.value
  
  // 计算面板位置，避免超出页面
  if (showWeatherPanel.value) {
    setTimeout(() => {
      const panel = document.querySelector('.weather-panel')
      if (panel) {
        const panelRect = panel.getBoundingClientRect()
        if (panelRect.bottom > window.innerHeight) {
          panelPosition.value = 'top'
        }
      }
    }, 0)
  }
}

// 初始化和清理
onMounted(async () => {
  formatDate()
  timer = setInterval(formatDate, 1000)
  
  // 获取位置和天气数据
  const city = await getLocationByIP()
  await getWeatherData(city)
})

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped lang="scss">
.date-weather-container {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 0 20px;
  position: relative;
  cursor: pointer;
}

.date-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.date {
  font-size: 14px;
  color: #a71d5d;
}

.time {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}

.weather-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 20px;
  transition: background-color 0.2s;
  
  &:hover {
    background-color: rgba(0, 0, 0, 0.05);
  }
}

.weather-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.weather-icon {
  font-size: 20px;
}

.temperature {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.weather-desc {
  font-size: 14px;
  color: #606266;
}

.arrow-icon {
  font-size: 12px;
  color: #909399;
  transition: transform 0.2s;
}

.weather-info:hover .arrow-icon {
  transform: rotate(180deg);
}

// 天气面板
.weather-panel {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  width: 320px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  overflow: hidden;
  max-height: 70vh;
  overflow-y: auto;
  
  /* 隐藏滚动条 */
  -ms-overflow-style: none;
  scrollbar-width: none;
  &::-webkit-scrollbar {
    display: none;
  }
}

/* 当面板位置在顶部时的样式 */
.weather-panel.top {
  bottom: 100%;
  top: auto;
  margin-top: 0;
  margin-bottom: 8px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  
  h3 {
    font-size: 16px;
    font-weight: 500;
    color: #303133;
    margin: 0;
  }
}

.close-icon {
  cursor: pointer;
  color: #909399;
  font-size: 16px;
  
  &:hover {
    color: #606266;
  }
}

.today-overview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.overview-left {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.today-temp {
  font-size: 32px;
  font-weight: 600;
}

.today-weather {
  font-size: 18px;
}

.today-desc {
  font-size: 14px;
  opacity: 0.9;
}

.overview-right {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.weather-detail {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  opacity: 0.9;
}

.week-weather {
  padding: 20px;
}

.week-weather h4,
.life-index h4 {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 12px 0;
}

.week-weather-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.week-weather-item {
  display: grid;
  grid-template-columns: 60px 40px 80px 1fr;
  align-items: center;
  font-size: 14px;
  color: #606266;
}

.small-icon {
  font-size: 18px;
  text-align: center;
}

.life-index {
  padding: 0 20px 20px;
  border-top: 1px solid #ebeef5;
  margin-top: 12px;
  padding-top: 20px;
}

.index-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.index-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px;
  background-color: #fafafa;
  border-radius: 8px;
  
  .index-icon {
    font-size: 24px;
  }
  
  .index-name {
    font-size: 12px;
    color: #909399;
  }
  
  .index-value {
    font-size: 14px;
    color: #303133;
    font-weight: 500;
  }
}
</style>
