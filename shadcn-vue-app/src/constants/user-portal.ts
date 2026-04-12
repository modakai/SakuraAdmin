import {
  BookOpenCheckIcon,
  ChartColumnBigIcon,
  CircleUserRoundIcon,
  ShieldCheckIcon,
  SparklesIcon,
  TrophyIcon,
} from '@lucide/vue'

// 用户端顶部导航配置，同时用于移动端菜单和页面高亮。
export const userNavItems = [
  { label: '首页', to: '/' },
  { label: '刷题中心', to: '/practice' },
  { label: '学习记录', to: '/records' },
  { label: '个人中心', to: '/profile' },
]

// 首页指标卡演示数据。
export const userMetrics = [
  { title: '今日完成', value: '48 题', hint: '较昨日 +6', icon: BookOpenCheckIcon },
  { title: '正确率', value: '89%', hint: '近七日稳定提升', icon: ChartColumnBigIcon },
  { title: '连续学习', value: '16 天', hint: '保持打卡节奏', icon: TrophyIcon },
]

export const userFeatures = [
  { title: '专项刷题', description: '按照知识点、题型和难度快速组织训练计划。', icon: SparklesIcon },
  { title: '学习追踪', description: '沉淀个人记录、阶段成绩和复盘建议。', icon: CircleUserRoundIcon },
  { title: '后台协同', description: '具备后台角色时，可直接切换到管理端维护题库。', icon: ShieldCheckIcon },
]

export const practiceCards = [
  { title: 'Java 基础巩固', description: '集合、异常、泛型与并发基础训练。', count: 120, level: '基础' },
  { title: 'Spring Boot 实战', description: '接口、权限、事务和性能优化专题。', count: 86, level: '进阶' },
  { title: '前端工程化', description: 'Vue、构建工具与组件设计综合训练。', count: 64, level: '综合' },
]

export const recordRows = [
  { title: 'Java 集合框架测验', score: '92 分', progress: '24 / 24', updatedAt: '今天 11:35' },
  { title: 'Spring Security 权限设计', score: '85 分', progress: '16 / 20', updatedAt: '昨天 20:18' },
  { title: 'Vue Router 嵌套路由', score: '88 分', progress: '12 / 15', updatedAt: '昨天 18:02' },
]

export const userHighlights = [
  { title: '算法专项训练已更新', content: '新增 30 道二叉树与图论题目。', time: '10 分钟前' },
  { title: '本周学习周报已生成', content: '可在个人中心查看最近练习概览。', time: '今天 09:20' },
  { title: '后台审核任务待处理', content: '管理员可前往后台处理待审核题目。', time: '今天 08:00' },
]
