import { AudioWaveformIcon, CommandIcon, GalleryVerticalEndIcon } from '@lucide/vue'
import { useI18n } from 'vue-i18n'

import { useSidebar } from '@/composables/use-sidebar'

import type { SidebarData, Team, User } from '../types'

const user: User = {
  name: 'shadcn',
  email: 'm@example.com',
  avatar: '/avatars/shadcn.jpg',
}

export function useSidebarData() {
  const { t } = useI18n()
  const { navData } = useSidebar()

  const teams = computed<Team[]>(() => [
    {
      id: 'acme-inc',
      name: 'Acme Inc',
      logo: GalleryVerticalEndIcon,
      plan: t('common.plan.enterprise'),
    },
    {
      id: 'acme-corp',
      name: 'Acme Corp.',
      logo: AudioWaveformIcon,
      plan: t('common.plan.startup'),
    },
    {
      id: 'evil-corp',
      name: 'Evil Corp.',
      logo: CommandIcon,
      plan: t('common.plan.free'),
    },
  ])

  const sidebarData = computed<SidebarData>(() => ({
    user,
    teams: teams.value,
    navMain: navData.value,
  }))

  return {
    sidebarData,
  }
}
