import { BellDotIcon, CreditCardIcon, FileTextIcon, PaletteIcon, PictureInPicture2Icon, SettingsIcon, UserIcon, UsersIcon, WrenchIcon } from '@lucide/vue'

import type { NavGroup } from '@/components/app-sidebar/types'

export function useSidebar() {
  /**
   * 系统管理的子菜单。
   * 仅保留当前项目实际需要展示的后台功能入口。
   */
  const systemManagementItems = [
    { title: '用户管理', url: '/users', icon: UsersIcon },
    { title: '字典管理', url: '/dicts', icon: WrenchIcon },
    { title: '协议管理', url: '/agreements', icon: FileTextIcon },
  ]

  /**
   * 设置页导航仍然保留给设置模块内部复用。
   */
  const settingsNavItems = [
    { title: 'Profile', url: '/settings/', icon: UserIcon },
    { title: 'Account', url: '/settings/account', icon: WrenchIcon },
    { title: 'Appearance', url: '/settings/appearance', icon: PaletteIcon },
    { title: 'Notifications', url: '/settings/notifications', icon: BellDotIcon },
    { title: 'Display', url: '/settings/display', icon: PictureInPicture2Icon },
  ]

  const navData = ref<NavGroup[]> ([
    {
      title: '主菜单',
      items: [
        {
          title: '系统管理',
          icon: SettingsIcon,
          items: systemManagementItems,
        },
      ],
    },
  ])

  const otherPages = ref<NavGroup[]>([
    {
      title: 'Other',
      items: [
        { title: 'Plans & Pricing', icon: CreditCardIcon, url: '/billing' },
      ],
    },
  ])

  return {
    navData,
    otherPages,
    settingsNavItems,
  }
}
