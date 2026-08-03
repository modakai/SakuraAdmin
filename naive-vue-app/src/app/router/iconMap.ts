import type { Component } from 'vue'
import {
  AlertCircleOutline,
  AnalyticsOutline,
  BookOutline,
  CloudUploadOutline,
  DocumentTextOutline,
  HomeOutline,
  KeyOutline,
  NotificationsOutline,
  PeopleOutline,
  PersonCircleOutline,
  PulseOutline,
  ServerOutline,
  SettingsOutline,
  ShieldOutline,
} from '@vicons/ionicons5'

// 后端 permission.icon 字符串 → 图标组件。动态菜单的图标来源。
export const iconMap: Record<string, Component> = {
  HomeOutline,
  SettingsOutline,
  PeopleOutline,
  PulseOutline,
  BookOutline,
  DocumentTextOutline,
  CloudUploadOutline,
  ShieldOutline,
  KeyOutline,
  AnalyticsOutline,
  ServerOutline,
  AlertCircleOutline,
  NotificationsOutline,
  PersonCircleOutline,
}

export function resolveIcon(icon?: string): Component | undefined {
  return icon ? iconMap[icon] : undefined
}
