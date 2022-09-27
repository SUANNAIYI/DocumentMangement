<template>
  <div id="time-line">
    <el-timeline>
      <el-timeline-item
        v-for="(activity, index) in activities"
        :key="index"
        :icon="activity.icon"
        :type="activity.type || 'primary'"
        :color="activity.color || '#409EFF'"
        :size="activity.size || 'large'"
        :timestamp="activity.timestamp">
        <!--  对象  -->
        <div v-if="typeof activity.content === 'object'">
          <div v-for="(value, i) in activity.content" :key="i" v-html="value"></div>
        </div>
        <!--  html字符串  -->
        <div v-else>
          <div v-html="activity.content"></div>
        </div>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<script>
export default {
  name: 'Timeline',
  props: {
    /**
       * 映射工具
       * 当content为Object类型是，用此对象映射content的参数名称
       */
    mapper: Object,
    /**
       * 数据结构：
       *     结构1：
       *         {
       *           content: {
       *             key: value
       *           },
       *           timestamp: date,
       *           // 非必填，默认#409EFF
       *           color: hsl / hsv / hex / rgb,
       *           // 非必填，默认primary
       *           type: primary / success / warning / danger / info,
       *           // 非必填，默认large
       *           size: normal / large,
       *           // 非必填，默认''
       *           icon: string
       *         }
       *     结构2：
       *         {
       *           content: html / string,
       *           timestamp: date,
       *           // 非必填，默认#409EFF
       *           color: hsl / hsv / hex / rgb,
       *           // 非必填，默认primary
       *           type: primary / success / warning / danger / info,
       *           // 非必填，默认large
       *           size: normal / large,
       *           // 非必填，默认''
       *           icon: string
       *         }
       */
    activities: Array
  }
}
</script>

<style lang="scss" type="text/scss">
  #time-line {
    .el-timeline {
      margin-left: 150px;

      .el-timeline-item {
        .el-timeline-item__wrapper {
          .el-timeline-item__timestamp {
            margin-top: 8px;
            position: absolute !important;
            left: -150px;
            top: -5px;
            font-size: 14px !important;
          }

          .el-timeline-item__content {
            color: #909399;
          }
        }
      }
    }
  }
</style>
