import { BellDotIcon, CreditCardIcon, FileCode2Icon, FileTextIcon, PaletteIcon, SettingsIcon, UserIcon, UsersIcon, WrenchIcon } from '@lucide/vue'
import { useI18n } from 'vue-i18n'

import type { NavGroup } from '@/components/app-sidebar/types'

/**
 * 构建设置页导航，方便在测试中复用。
 */
export function buildSettingsNavItems(t: (key: string) => string) {
  return [
    { id: 'profile', title: t('menu.settings.profile'), url: '/settings/', icon: UserIcon },
    { id: 'account', title: t('menu.settings.account'), url: '/settings/account', icon: WrenchIcon },
    { id: 'appearance', title: t('menu.settings.appearance'), url: '/settings/appearance', icon: PaletteIcon },
    { id: 'notifications', title: t('menu.settings.notifications'), url: '/settings/notifications', icon: BellDotIcon },
  ]
}

export function useSidebar() {
  const { t } = useI18n()

  /**
   * 系统管理的子菜单。
   * 仅保留当前项目实际需要展示的后台功能入口。
   */
  const systemManagementItems = computed(() => [
    { id: 'users', title: t('menu.system.users'), url: '/users', icon: UsersIcon },
    { id: 'dicts', title: t('menu.system.dicts'), url: '/dicts', icon: WrenchIcon },
    { id: 'agreements', title: t('menu.system.agreements'), url: '/agreements', icon: FileTextIcon },
    { id: 'notifications', title: '通知公告', url: '/notifications', icon: BellDotIcon },
    { id: 'notification-templates', title: '消息模板', url: '/notification-templates', icon: FileCode2Icon },
    // 外观偏好属于后台管理端体验配置，放在系统管理入口下更容易发现。
    { id: 'appearance-preferences', title: t('menu.settings.appearance'), url: '/settings/appearance', icon: PaletteIcon },
  ])

  /**
   * 设置页导航仍然保留给设置模块内部复用。
   */
  const settingsNavItems = computed(() => buildSettingsNavItems(t))

  const navData = computed<NavGroup[]>(() => [
    {
      id: 'main',
      title: t('menu.main'),
      items: [
        {
          id: 'system',
          title: t('menu.system.title'),
          icon: SettingsIcon,
          items: systemManagementItems.value,
        },
      ],
    },
  ])

  const otherPages = computed<NavGroup[]>(() => [
    {
      id: 'other',
      title: t('menu.other'),
      items: [
        { id: 'billing', title: t('pages.billing.title'), icon: CreditCardIcon, url: '/billing' },
      ],
    },
  ])

  return {
    navData,
    otherPages,
    settingsNavItems,
  }
}
