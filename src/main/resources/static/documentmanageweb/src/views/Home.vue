<template>
  <el-container direction="vertical" class="homePage">
    <Top ref="topComponent" />
    <el-container direction="horizontal">
      <Sidebar/>
      <Content ref="contentComponent"/>
    </el-container>
  </el-container>
</template>

<script>
import Sidebar from '../components/Sidebar.vue'
import Top from '../components/Top.vue'
import Content from '../components/Content.vue'
export default {
  name: 'Home',
  components: { Sidebar, Top, Content },

  data () {
    return {

    }
  },
  methods: {
  },
  mounted () {
    // 点击其他地方弹窗关闭
    // const topCom = this.$refs.topComponent
    // document.addEventListener('click', function (e) {
    //   if (topCom.isShowRetrieve) {
    //     topCom.$refs.switchDirection.src = require('../assets/img/bottom.svg')
    //     topCom.$refs.switchDirection.style.top = '28px'
    //     topCom.isShowRetrieve = false
    //   }
    // })
  },
  beforeDestroy () {

  }
}
</script>

<style lang="scss">
  // .infoTable {
  //   height: 580px;
  //   element.style{
  //     height: 580px;
  //   }
  // }
</style>
