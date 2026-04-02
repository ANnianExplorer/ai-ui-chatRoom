<template>
  <nav class="app-sidebar">
    <!-- 导航项 -->
    <div class="nav-list">
      <div
        v-for="item in navItems"
        :key="item.path"
        class="nav-item"
        :class="{ active: activePath === item.path }"
        @click="handleNavClick(item.path)"
        :title="item.label"
      >
        <div class="nav-icon-wrap">
          <component :is="item.icon" class="nav-svg" />
          <!-- 消息未读徽标占位（可扩展） -->
        </div>
        <span class="nav-label">{{ item.label }}</span>
        <!-- 激活指示条 -->
        <div class="active-bar" v-if="activePath === item.path"></div>
      </div>

      <div class="nav-divider"></div>

      <div
        class="nav-item"
        :class="{ active: activePath === '/settings' }"
        @click="handleNavClick('/settings')"
        title="设置"
      >
        <div class="nav-icon-wrap">
          <SettingsSvg class="nav-svg" />
        </div>
        <span class="nav-label">设置</span>
        <div class="active-bar" v-if="activePath === '/settings'"></div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { computed, defineComponent, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const activePath = computed(() => route.path)

const handleNavClick = (path) => {
  if (route.path !== path) router.push(path)
}

/* ── SVG 图标组件 ── */
const ChatSvg = defineComponent({
  render: () => h('svg', { viewBox: '0 0 24 24', fill: 'none', xmlns: 'http://www.w3.org/2000/svg' }, [
    h('path', { d: 'M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' })
  ])
})

const ContactSvg = defineComponent({
  render: () => h('svg', { viewBox: '0 0 24 24', fill: 'none', xmlns: 'http://www.w3.org/2000/svg' }, [
    h('circle', { cx: '9', cy: '7', r: '4', stroke: 'currentColor', 'stroke-width': '2' }),
    h('path', { d: 'M3 21v-2a4 4 0 0 1 4-4h4a4 4 0 0 1 4 4v2M16 3.13a4 4 0 0 1 0 7.75M21 21v-2a4 4 0 0 0-3-3.87', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' })
  ])
})

const GroupSvg = defineComponent({
  render: () => h('svg', { viewBox: '0 0 24 24', fill: 'none', xmlns: 'http://www.w3.org/2000/svg' }, [
    h('rect', { x: '3', y: '3', width: '7', height: '7', rx: '1.5', stroke: 'currentColor', 'stroke-width': '2' }),
    h('rect', { x: '14', y: '3', width: '7', height: '7', rx: '1.5', stroke: 'currentColor', 'stroke-width': '2' }),
    h('rect', { x: '3', y: '14', width: '7', height: '7', rx: '1.5', stroke: 'currentColor', 'stroke-width': '2' }),
    h('rect', { x: '14', y: '14', width: '7', height: '7', rx: '1.5', stroke: 'currentColor', 'stroke-width': '2' })
  ])
})

const StarSvg = defineComponent({
  render: () => h('svg', { viewBox: '0 0 24 24', fill: 'none', xmlns: 'http://www.w3.org/2000/svg' }, [
    h('polygon', { points: '12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' })
  ])
})

const DocSvg = defineComponent({
  render: () => h('svg', { viewBox: '0 0 24 24', fill: 'none', xmlns: 'http://www.w3.org/2000/svg' }, [
    h('path', { d: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }),
    h('polyline', { points: '14 2 14 8 20 8', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }),
    h('line', { x1: '16', y1: '13', x2: '8', y2: '13', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round' }),
    h('line', { x1: '16', y1: '17', x2: '8', y2: '17', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round' })
  ])
})

const SettingsSvg = defineComponent({
  render: () => h('svg', { viewBox: '0 0 24 24', fill: 'none', xmlns: 'http://www.w3.org/2000/svg' }, [
    h('circle', { cx: '12', cy: '12', r: '3', stroke: 'currentColor', 'stroke-width': '2' }),
    h('path', { d: 'M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' })
  ])
})

const navItems = [
  { path: '/index',       label: '消息',  icon: ChatSvg },
  { path: '/qq-contact',  label: '联系人', icon: ContactSvg },
  { path: '/qq-group',    label: '群聊',  icon: GroupSvg },
  { path: '/favorite',    label: '收藏',  icon: StarSvg },
  { path: '/document',    label: '文档',  icon: DocSvg },
]
</script>

<style lang="scss" scoped>
.app-sidebar {
  width: 68px;
  height: 100%;
  background: var(--sidebar-bg);
  backdrop-filter: blur(24px) saturate(180%);
  -webkit-backdrop-filter: blur(24px) saturate(180%);
  border-right: 1px solid var(--sidebar-border);
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  box-shadow: 2px 0 20px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  z-index: 50;
}

.nav-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 0;
  gap: 2px;
  width: 100%;
  overflow-y: auto;
  scrollbar-width: none;
  &::-webkit-scrollbar { display: none; }
}

.nav-item {
  position: relative;
  width: 52px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 8px 0;
  border-radius: 14px;
  cursor: pointer;
  color: var(--sidebar-icon);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: visible;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: 14px;
    background: var(--sidebar-item-hover);
    opacity: 0;
    transition: opacity 0.2s ease;
  }

  &:hover {
    color: var(--color-primary);
    transform: translateY(-2px);

    &::before { opacity: 1; }

    .nav-icon-wrap {
      transform: scale(1.1);
    }
  }

  &.active {
    color: var(--sidebar-icon-active);

    &::before {
      opacity: 1;
      background: var(--sidebar-item-active);
      box-shadow: inset 0 0 0 1px var(--border-accent);
    }

    .nav-icon-wrap {
      filter: drop-shadow(0 0 8px rgba(124, 58, 237, 0.6));
      animation: icon-glow 2.5s ease-in-out infinite;
    }

    .nav-label {
      background: var(--brand-gradient-3);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      font-weight: 700;
    }

    .active-bar {
      opacity: 1;
      box-shadow: 0 0 8px rgba(124, 58, 237, 0.8), 0 0 16px rgba(124, 58, 237, 0.4);
      animation: bar-pulse 2s ease-in-out infinite;
    }
  }
}

@keyframes icon-glow {
  0%, 100% { filter: drop-shadow(0 0 6px rgba(124, 58, 237, 0.5)); }
  50%       { filter: drop-shadow(0 0 14px rgba(124, 58, 237, 0.85)) drop-shadow(0 0 20px rgba(167, 139, 250, 0.4)); }
}

@keyframes bar-pulse {
  0%, 100% { height: 60%; box-shadow: 0 0 6px rgba(124, 58, 237, 0.7); }
  50%       { height: 70%; box-shadow: 0 0 12px rgba(124, 58, 237, 1), 0 0 24px rgba(124, 58, 237, 0.5); }
}

.nav-icon-wrap {
  width: 26px; height: 26px;
  display: flex; align-items: center; justify-content: center;
  transition: transform 0.25s ease;
  position: relative;
  z-index: 1;
}

.nav-svg {
  width: 22px; height: 22px;
  pointer-events: none;
}

.nav-label {
  font-size: 10px;
  font-weight: 500;
  line-height: 1;
  letter-spacing: 0.02em;
  position: relative;
  z-index: 1;
}

/* 激活指示条 */
.active-bar {
  position: absolute;
  left: -8px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 60%;
  min-height: 20px;
  background: var(--brand-gradient-2);
  border-radius: 0 4px 4px 0;
  opacity: 0;
  transition: opacity 0.25s ease;
}

.nav-divider {
  width: 36px;
  height: 1px;
  background: var(--border-default);
  margin: 6px 0;
  flex-shrink: 0;
}
</style>
