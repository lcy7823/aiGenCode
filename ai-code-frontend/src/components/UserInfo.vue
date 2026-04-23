<template>
  <div class="user-info">
    <a-avatar :src="userVo?.userAvatar" :size="size">
      {{ userVo?.userName?.charAt(0) || 'U' }}
    </a-avatar>
    <span v-if="showName" class="user-name">{{ userVo?.userName || '未知用户' }}</span>
  </div>
</template>

<script setup lang="ts">
interface Props {
  userVo?: API.UserVo
  size?: number | 'small' | 'default' | 'large'
  showName?: boolean
}

withDefaults(defineProps<Props>(), {
  size: 'default',
  showName: true,
})
</script>

<style scoped>
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-size: 14px;
  color: #1a1a1a;
}
</style>
