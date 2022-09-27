<template>
  <div id="app">
    <div></div>
    <router-view />
  </div>
</template>

<script>

export default {
  name: 'App',
  data () {
    return {

    }
  },
  methods: {
  }
}

</script>

<style lang="scss">
  #app {
    width: 100%;
    height: 100%;
  }
  html, body {
    width: 100%;
    height: 100%;
    margin: 0;
    padding: 0;
  }

  .header .el-input__inner{
    height: 50px;
    line-height: 60px;
    font-size: 16px;
  }
  .header .search .el-button{
    width: 50px !important;
    height: 50px !important;
    font-size: 16px;
  }

  .header .retrieve{
    .el-form-item__label {
      font-size: 16px;
      line-height: 50px;
    }
    .el-date-editor .el-range-separator {
      line-height: 40px;
      font-size: 15px;
    }
    .el-date-editor .el-range-input {
      font-size: 15px;
    }
    .el-input__inner{
      font-size: 15px;
      height: 50px !important;
    }
    .el-date-editor--datetimerange.el-input, .el-date-editor--datetimerange.el-input__inner {
      width: 170%;
    }
    // .select .el-input__inner{
    //   width: 200%;
    // }
    // .el-input__suffix {
    //   right: -80px;
    //   transition: all .3s;
    //   pointer-events: none;
    // }
  }

  .above .el-button{
    font-size: 16px;
  }

  .el-dialog__body {
    display: flex;
    justify-content: center;
    align-content: center;
  }

  .el-cascader-menu{
    height: 200px;
  }

  .upload .el-input__inner {
    width: 160px !important;
  }

  .distribute .el-input__inner {
    width: 160px !important;
  }

  .el-radio__input.is-checked .el-radio__inner {
    background: transparent;
  }

  .el-tree-node__content{
      height: 35px !important;
  }
  /*tree组件选中背景色修改 */
  .el-tree-node.is-current > .el-tree-node__content{
      background-color: #3D59AB !important;
  }
  /*tree组件鼠标hover悬浮背景色 */
  .el-tree-node > .el-tree-node__content:hover{
      background-color: #3D59AB
  }

  .el-pagination {
    span:not([class*=suffix]) {
      font-size: 15px !important;
    }
    .el-pagination__sizes .el-input .el-input__inner {
      font-size: 15px !important;
    }
    .el-pager li {
      font-size: 15px;
    }
    .el-input__inner {
      font-size: 15px;
    }
    .el-pagination__jump{
      font-size: 15px;
    }
  }

  .el-table::before {
    height: 0 !important;
  }

</style>
