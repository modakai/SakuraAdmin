import { BellDotIcon, CreditCardIcon, FileCode2Icon, FileTextIcon, LayoutDashboardIcon, PaletteIcon, SettingsIcon, SlidersHorizontalIcon, UserIcon, UsersIcon, WrenchIcon } from '@lucide/vue'
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

/**
 * 构建后台侧边栏主导航，保持菜单结构可测试、可复用。
 */
export function buildAdminNavGroups(t: (key: string) => string): NavGroup[] {
  return [
    {
      id: 'main',
      title: t('menu.main'),
      items: [
        {
          id: 'dashboard',
          title: t('menu.dashboard'),
          url: '/dashboard',
          icon: LayoutDashboardIcon,
        },
        {
          id: 'system',
          title: t('menu.system.title'),
          icon: SettingsIcon,
          items: [
            { id: 'users', title: t('menu.system.users'), url: '/users', icon: UsersIcon },
            { id: 'dicts', title: t('menu.system.dicts'), url: '/dicts', icon: WrenchIcon },
            { id: 'agreements', title: t('menu.system.agreements'), url: '/agreements', icon: FileTextIcon },
            {
              id: 'system-settings',
              title: t('menu.settings.title'),
              icon: SlidersHorizontalIcon,
              items: [
                { id: 'notifications', title: t('menu.settings.notificationAnnouncements'), url: '/notifications', icon: BellDotIcon },
                { id: 'notification-templates', title: t('menu.settings.notificationTemplates'), url: '/notification-templates', icon: FileCode2Icon },
              ],
            },
          ],
        },
      ],
    },
  ]
}

/**
 * 构建未归入主导航的页面入口，供侧边栏、命令面板和页面标题统一复用。
 */
export function buildOtherNavGroups(t: (key: string) => string): NavGroup[] {
  return [
    {
      id: 'other',
      title: t('menu.other'),
      items: [
        { id: 'billing', title: t('pages.billing.title'), icon: CreditCardIcon, url: '/billing' },
      ],
    },
  ]
}

export function useSidebar() {
  const { t } = useI18n()

  /**
   * 设置页导航仍然保留给设置模块内部复用。
   */
  const settingsNavItems = computed(() => buildSettingsNavItems(t))
  const navData = computed<NavGroup[]>(() => buildAdminNavGroups(t))
  const otherPages = computed<NavGroup[]>(() => buildOtherNavGroups(t))

  return {
    navData,
    otherPages,
    settingsNavItems,
  }
}
