<template>
  <div>
    <div class="header">
      <div class="container">
        <el-row :gutter="20">
          <el-col class="title" span="6"> 用户管理 </el-col>
          <el-col class="nav" span="14">
            <el-input
              class="search"
              v-model="searchUser"
              placeholder="请输入内容"
            ></el-input>
            <el-button icon="el-icon-search" circle></el-button>
          </el-col>
        </el-row>
      </div>
      <!-- <div class="container">
          <div class="title">
            用户管理
          </div>
          <div class="nav">
            <el-input
              class="search"
              v-model="searchUser"
              placeholder="请输入内容"
            ></el-input>
            <el-button
              icon="el-icon-search"
              circle
            ></el-button>
            <div class="other">
            </div>
          </div>
        </div> -->
    </div>
    <div class="page">
      <div class="scrollbar"></div>

      <div class="content">
        <el-table
          class="userTable"
          ref="userTable"
          :data="userData"
          :cell-style="rowStyle"
          :style="{ 'max-height': this.timeLineHeight + 'px' }"
          style="overflow-y: auto"
        >
          <el-table-column type="selection" min-width="5%"> </el-table-column>
          <el-table-column type="index" label="序号" width="60px">
          </el-table-column>
          <el-table-column
            prop="username"
            label="用户名"
            min-width="15%"
            show-overflow-tooltip
          >
          </el-table-column>
          <el-table-column prop="registertime" label="注册时间" min-width="15%">
          </el-table-column>
          <el-table-column prop="role" label="用户角色" min-width="8%">
          </el-table-column>
          <el-table-column label="权限管理" min-width="8%">
            <template slot-scope="scope">
              <el-select v-model="scope.row.limit" placeholder="请选择">
                <el-option
                  v-for="item in scope.row.options"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                >
                </el-option>
              </el-select>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
import Server from "@/utils/server";
export default {
  data() {
    return {
      options: [
        {
          label: "审核",
          value: "examine",
        },
        {
          label: "查看",
          value: "check",
        },
        {
          label: "编辑",
          value: "edit",
        },
      ],
      userData: [],
      value: "",
    };
  },
  created() {
    this.getUserList();
  },
  methods: {
    getUserList() {
      Server.get(process.env.VUE_APP_SERVER + "/user/search").then((json) => {
        if (json.code === 200) {
          this.userData = json.data.map((item) => {
            return {
              id: item.id,
              username: item.username,
              role: item.role,
              options: this.options,
              limit: "",
            };
          });
          console.log(this.userData);
        }
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.header {
  height: 80px;
  color: #333;
  top: 0;
  left: 0;
  width: 100%;
  line-height: 80px;
  z-index: 100;
  position: relative;
  margin-bottom: 20px;

  .container {
    height: 100%;
    width: 1140px;
    margin: 0 auto;
    .title {
      float: left;
      font-size: 20px;
    }
    .nav {
      display: flex;
      float: right;
      height: 80px;
      .search {
        float: left;
      }
      .el-button {
        height: 50px;
        width: 50px;
        margin-top: 15px;
        margin-left: 10px;
      }
    }
  }
}
.page {
  box-sizing: border-box;
  height: calc(100vh-80px);
  width: 1140px;
  margin: 0 auto;
}
</style>
