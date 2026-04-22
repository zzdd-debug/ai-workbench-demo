<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'

import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const { errorMessage, loggingIn } = storeToRefs(authStore)

const form = reactive({
  username: 'demo',
  password: '123456',
})

const submit = async () => {
  const success = await authStore.loginWithPassword(form.username, form.password)
  if (success) {
    await router.replace({ name: 'home' })
  }
}
</script>

<template>
  <div class="auth-shell">
    <section class="auth-panel">
      <div class="auth-copy">
        <p class="eyebrow">
          Demo Auth
        </p>
        <h1>登录后进入工作台</h1>
        <p>
          本轮只提供演示级登录闭环。请使用 mock 账号登录进入 AI 研发任务工作台。
        </p>
      </div>

      <el-card shadow="hover" class="auth-card">
        <template #header>
          <div class="card-header">
            <span>登录</span>
            <el-tag type="info" effect="light">Mock</el-tag>
          </div>
        </template>

        <div class="auth-form">
          <el-input v-model="form.username" placeholder="用户名" />
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="密码"
            @keyup.enter="submit"
          />

          <el-alert
            v-if="errorMessage"
            title="登录失败"
            type="error"
            :description="errorMessage"
            :closable="false"
            show-icon
          />

          <el-button type="primary" :loading="loggingIn" @click="submit">
            登录并进入工作台
          </el-button>

          <div class="auth-helper">
            <span>用户名：`demo`</span>
            <span>密码：`123456`</span>
          </div>
        </div>
      </el-card>
    </section>
  </div>
</template>
