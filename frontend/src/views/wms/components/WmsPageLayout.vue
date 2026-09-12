<template>
  <div class="wms-page-layout" :class="{ 'is-dense': dense }">
    <div class="wms-page-layout__header">
      <div class="wms-page-layout__title">
        <h3 class="wms-page-layout__title-text">{{ title }}</h3>
        <div v-if="subtitle" class="wms-page-layout__subtitle">{{ subtitle }}</div>
      </div>
      <div v-if="hasActions" class="wms-page-layout__actions">
        <slot name="actions" />
      </div>
    </div>

    <el-card v-if="hasFilters" class="wms-page-layout__filters" shadow="hover">
      <slot name="filters" />
    </el-card>

    <div class="wms-page-layout__content">
      <slot />
    </div>

    <div v-if="hasFooter" class="wms-page-layout__footer">
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, useSlots } from 'vue'

type Props = {
  title: string
  subtitle?: string
  dense?: boolean
}

const props = defineProps<Props>()
const slots = useSlots()

const hasActions = computed(() => Boolean(slots.actions))
const hasFilters = computed(() => Boolean(slots.filters))
const hasFooter = computed(() => Boolean(slots.footer))

const dense = computed(() => Boolean(props.dense))
</script>

<style scoped>
.wms-page-layout {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.wms-page-layout.is-dense {
  gap: 12px;
}

.wms-page-layout__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.wms-page-layout__title {
  min-width: 0;
}

.wms-page-layout__title-text {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.2;
}

.wms-page-layout__subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.3;
}

.wms-page-layout__actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.wms-page-layout__filters :deep(.el-card__body) {
  padding: 16px;
}

.wms-page-layout.is-dense .wms-page-layout__filters :deep(.el-card__body) {
  padding: 12px;
}

.wms-page-layout__content {
  min-width: 0;
}

.wms-page-layout__footer {
  display: flex;
  justify-content: flex-end;
}
</style>

