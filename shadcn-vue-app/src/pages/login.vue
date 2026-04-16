<script setup lang="ts">
import AuthShell from './auth/components/auth-shell.vue'
import LoginForm from './auth/components/login-form.vue'
</script>

<template>
  <AuthShell entry="user">
    <!-- 统一登录页固定使用同一路由，登录成功后按角色跳转。 -->
    <LoginForm entry="user" />
  </AuthShell>
</template>

<route lang="yaml">
meta:
  layout: false
  guestOnly: true
  authEntry: user
</route>
