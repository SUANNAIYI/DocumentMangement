<template>
  <el-aside>
    <div class="tree_container">
      <el-tree
        :default-active="'2'"
        class="custom-class"
        :data="menuList"
        :props="props"
        default-expand-all
        :expand-on-click-node="false"
        ref="tree"
        highlight-current
        @node-click="handleSelect"
        @node-contextmenu="rightClick"
      >
        <span class="custom-tree-node" slot-scope="{ node }">
          <span>
            <el-icon name="folder-opened"></el-icon>{{ node.label }}
          </span>
        </span>
      </el-tree>
    </div>

    <div
      id="menu"
      v-show="menuVisible"
      @mouseleave="menuVisible = !menuVisible"
    >
      <el-card class="box-card">
        <div class="text item">
          <el-link
            icon="el-icon-circle-plus-outline"
            :underline="false"
            @click="createFolderVisible = true"
            >新建</el-link
          >
        </div>
        <div class="text item">
          <el-link
            icon="el-icon-edit-outline"
            :underline="false"
            @click="renameFolderVisible = true"
            >重命名</el-link
          >
        </div>
        <div class="text item">
          <el-link
            icon="el-icon-delete"
            :underline="false"
            @click="deleteFolderVisible = true"
            >删除</el-link
          >
        </div>
      </el-card>
    </div>

    <el-dialog
      title="删除文件夹"
      :visible.sync="deleteFolderVisible"
      width="30%"
      height="50%">
      <span>确认删除该文件夹及其包含的所有文件？</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="deleteFolderVisible = false">取 消</el-button>
        <el-button
          type="primary"
          @click="(deleteFolderVisible = false), deleteFolder()"
          >确 定</el-button
        >
      </span>
    </el-dialog>

    <el-dialog
      title="新建文件夹"
      :visible.sync="createFolderVisible"
      width="30%"
      height="50%">
      <el-input
        v-model="nameInput"
        placeholder="请输入名称"
        @keyup.enter.native="(createFolderVisible = false), createFolder()">
      </el-input>
      <span slot="footer" class="dialog-footer">
        <el-button @click="createFolderVisible = false">取 消</el-button>
        <el-button
          type="primary"
          @click="(createFolderVisible = false), createFolder()"
          >确 定</el-button
        >
      </span>
    </el-dialog>

    <el-dialog
      title="重命名文件夹"
      :visible.sync="renameFolderVisible"
      width="30%"
      height="50%">
      <el-input
        v-model="renameInput"
        placeholder="请输入名称"
        @keyup.enter.native="(renameFolderVisible = false), renameFolder()">
      </el-input>
      <span slot="footer" class="dialog-footer">
        <el-button @click="renameFolderVisible = false">取 消</el-button>
        <el-button
          type="primary"
          @click="(renameFolderVisible = false), renameFolder()"
          >确 定</el-button
        >
      </span>
    </el-dialog>
  </el-aside>
</template>

<script>
import Server from '@/utils/server'
import { mapState } from 'vuex'

const dfs = (obj, callback) => {
  callback(obj)
  if (obj.children instanceof Array) {
    for (const item of obj.children) {
      dfs(item, callback)
    }
  }
  return obj
}

export default {
  name: 'Sidebar',
  data () {
    return {
      menuList: [],
      folderName: '',
      nameInput: '',
      renameInput: '',

      menuVisible: false,
      deleteFolderVisible: false,
      createFolderVisible: false,
      renameFolderVisible: false,

      props: {
        label: 'foldername',
        id: 'id'
      }
    }
  },
  computed: {
    ...mapState({
      refreshCount: 'refreshCount',
      currentId: 'currentId',
      options: 'options',
      currentPage: 'currentPage',
      pageSize: 'pageSize',
      dataCount: 'dataCount',
      showInfo: 'showInfo',
      isSearch: 'isSearch',
      isRetrieve: 'isRetrieve'
    })
  },
  watch: {
    refreshCount () {
      this.handleSelect({ id: this.currentId })
    },

    currentPage () {
      if (!this.isSearch && !this.isRetrieve) {
        this.handleSelect({ id: this.currentId })
      }
    },
    pageSize () {
      if (!this.isSearch && !this.isRetrieve) {
        this.handleSelect({ id: this.currentId })
      }
    },
    currentId () {
      this.$store.commit('setCurrentPage', 1)
    }
  },
  methods: {
    rightClick (MouseEvent, object, Node, element) {
      this.$store.commit('setCurrentId', object.id)
      console.log(this.currentId)
      this.renameInput = object.foldername
      this.menuVisible = true
      const menu = document.querySelector('#menu')
      menu.style.cssText =
        'position: fixed; left: ' +
        (MouseEvent.clientX - 10) +
        'px' +
        '; top: ' +
        (MouseEvent.clientY - 25) +
        'px; z-index: 999; cursor:pointer;'
    },

    deleteFolder () {
      Server.post(process.env.VUE_APP_SERVER + '/folder/delete', {
        id: this.currentId
      }).then((json) => {
        console.log(json.data)
        this.$message({
          type: json.code === 200 ? 'success' : 'error',
          message: json.msg
        })
        this.searchFolder()
      })
    },
    createFolder () {
      Server.post(process.env.VUE_APP_SERVER + '/folder/create', {
        id: this.currentId,
        name: this.nameInput
      }).then((json) => {
        this.$message({
          type: json.code === 200 ? 'success' : 'error',
          message: json.msg
        })
        if (json.code === 200) {
          this.nameInput = ''
          this.searchFolder()
        }
      })
    },
    renameFolder () {
      Server.post(process.env.VUE_APP_SERVER + '/folder/rename', {
        id: this.currentId,
        name: this.renameInput
      }).then((json) => {
        this.$message({
          type: json.code === 200 ? 'success' : 'error',
          message: json.msg
        })
        if (json.code === 200) {
          this.renameInput = ''
          this.searchFolder()
        }
      })
    },

    handleSelect (node) {
      this.$store.commit('setIsSearch', false)
      this.$store.commit('setCurrentId', node.id)
      Server.post(process.env.VUE_APP_SERVER + '/docs/folder',
        { id: node.id, currentPage: this.currentPage, pageSize: this.pageSize }).then((json) => {
        // console.log('文件列表')
        // console.log(json.data)
        this.$store.commit('setShowInfo', json.data.records)
        this.$store.commit('setDataCount', json.data.total)
      })
    },

    searchFolder () {
      this.$store.commit('setCurrentPage', 1)
      this.$store.commit('setPageSize', 10)
      Server.get(process.env.VUE_APP_SERVER + '/folder/search').then((json) => {
        if (json.code === 200) {
          this.menuList = json.data.map((menu) => {
            return dfs(menu, (item) => {
              item.id = String(item.id)
            })
          })
          this.$store.commit('setOptions', this.menuList)
        } else {
          this.$message({
            message: '接口错误!',
            type: 'error'
          })
        }
        this.handleSelect(this.menuList[0])
      })
    }
  },
  mounted () {
    this.searchFolder()
  }
}
</script>

<style lang="scss" scoped>
.el-aside {
  background-color: #fafafa;
  height: 100%;
  max-width: 260px !important;
  border-right: 1px solid rgba(0, 0, 0, 0.1);
}
::v-deep .el-card__body {
  padding: 10px;
}
.box-card .item {
  margin: 5px;
}

.tree_container {
  height: calc(100vh - 60px);
  max-height: calc(100vh - 60px);
  max-width: 280px;
  overflow: auto;
}
.el-tree {
  background: transparent;
  /*改变高度*/
  .el-tree-node__content {
    height: 35px !important;
    padding: 5px !important;
  }
  .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 18px;
    margin: 5px;
    padding-right: 20px;
  }
}
::v-deep .el-tree-node__content {
  padding: 5px;
}
/*tree组件选中背景色修改 */
::v-deep .el-tree-node > .el-tree-node__content:hover {
  color: rgb(22, 113, 218);
  background-color: transparent !important;
}
::v-deep .el-tree-node > .el-tree-node__content:focus {
  color: #3d59ab;
  background-color: transparent !important;
}
::v-deep .el-tree-node.is-current > .el-tree-node__content {
    background-color: transparent !important;
    color: #e91878;
}
</style>
