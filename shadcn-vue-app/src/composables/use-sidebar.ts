import { BellDotIcon, CreditCardIcon, FileTextIcon, PaletteIcon, PictureInPicture2Icon, SettingsIcon, UserIcon, UsersIcon, WrenchIcon } from '@lucide/vue'
import { useI18n } from 'vue-i18n'

import type { NavGroup } from '@/components/app-sidebar/types'

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
  ])

  /**
   * 设置页导航仍然保留给设置模块内部复用。
   */
  const settingsNavItems = computed(() => [
    { id: 'profile', title: t('menu.settings.profile'), url: '/settings/', icon: UserIcon },
    { id: 'account', title: t('menu.settings.account'), url: '/settings/account', icon: WrenchIcon },
    { id: 'appearance', title: t('menu.settings.appearance'), url: '/settings/appearance', icon: PaletteIcon },
    { id: 'notifications', title: t('menu.settings.notifications'), url: '/settings/notifications', icon: BellDotIcon },
    { id: 'display', title: t('menu.settings.display'), url: '/settings/display', icon: PictureInPicture2Icon },
  ])

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
