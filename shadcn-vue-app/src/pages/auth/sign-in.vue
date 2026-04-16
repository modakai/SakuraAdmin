<script setup lang="ts">
const router = useRouter()

// 兼容历史后台登录地址，实际登录入口统一收敛到 /login。
router.replace('/login')
</script>

<template>
  <div />
</template>

<route lang="yaml">
meta:
  layout: false
  guestOnly: true
</route>
