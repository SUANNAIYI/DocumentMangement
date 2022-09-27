<template>
  <el-main>
    <el-dialog
      class="send"
      title="发送文件"
      :visible.sync="sendVisible"
      center
      width="30%"
    >
      <div>
        <span>用户</span>
        <el-select
          v-model="selectedSend"
          filterable
          multiple
          placeholder="请选择"
        >
          <el-option
            v-for="item in sendOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          >
          </el-option>
        </el-select>
      </div>
      <div slot="footer" class="send-footer">
        <el-button @click="sendVisible = false">取 消</el-button>
        <el-button type="primary" @click="toSend(), (sendVisible = false)"
          >发送</el-button
        >
      </div>
    </el-dialog>

    <el-dialog
      class="check"
      title="审核联系人"
      :visible.sync="checkVisible"
      center
      width="30%"
    >
      <div>
        <span>用户</span>
        <el-select
          v-model="selectedCheck"
          filterable
          multiple
          placeholder="请选择"
        >
          <el-option
            v-for="item in checkOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          >
          </el-option>
        </el-select>
      </div>
      <div slot="footer" class="check-footer">
        <el-button @click="checkVisible = false">取 消</el-button>
        <el-button type="primary" @click="toCheck(), (checkVisible = false)"
          >发送</el-button
        >
      </div>
    </el-dialog>

    <el-dialog
      class="delete"
      title="提示"
      :visible.sync="deleteVisible"
      width="30%"
      height="50%"
    >
      <span>确认删除？</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="deleteVisible = false">取 消</el-button>
        <el-button type="primary" @click="(deleteVisible = false), toDelete()"
          >确 定</el-button
        >
      </span>
    </el-dialog>

    <el-dialog
      title="版本记录"
      :visible.sync="recordVisible"
      center
      width="60%"
    >
      <el-table
        ref="recordTable"
        :data="recordData"
        tooltip-effect="dark"
        style="width: 100%"
        @selection-change="recordSelectionChange"
        @row-click="recordRowClick"
        :header-cell-style="headStyle"
        :cell-style="rowStyle"
        :default-sort="{ prop: 'time', order: 'descending' }"
      >
        <el-table-column type="selection" min-width="5%"> </el-table-column>
        <el-table-column
          type="index"
          :index="reIndexMethod"
          label="版本号"
          width="80"
        >
        </el-table-column>
        <el-table-column
          prop="docName"
          label="文件名称"
          min-width="20%"
          show-overflow-tooltip
        >
        </el-table-column>
        <el-table-column prop="editor" label="修改人" min-width="10%">
        </el-table-column>
        <el-table-column prop="time" label="修改时间" min-width="15%">
        </el-table-column>
        <el-table-column prop="operation" label="操作" min-width="10%">
          <template slot-scope="scope">
            <div>
              <el-tooltip
                class="item"
                effect="dark"
                content="查看"
                placement="top"
              >
                <el-button
                  icon="el-icon-view"
                  circle
                  @click="toLook(scope.row)"
                ></el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <span slot="footer" class="dialog-footer">
        <el-button
          @click="toggleRecord()"
          :disabled="recordSelection.length === 0"
          >取消选择</el-button
        >
        <el-button
          type="danger"
          :disabled="recordSelection.length === 0"
          @click="deleteRecord()"
          >删 除</el-button
        >
        <!-- <el-button type="primary" @click="recordVisible = false, toPreview()"
          >查 看</el-button
        > -->
      </span>
    </el-dialog>

    <div
      class="above"
      style="display: flex; line-height: 40px; margin-bottom: 20px"
    >
      <div class="count" style="font-size: 18px">
        当前已选择<strong>{{ this.multipleSelection.length }}</strong
        >条
      </div>
      <div style="position: absolute; right: 45px">
        <el-button
          type="warning"
          @click="rejectVisible = true"
          :disabled="this.multipleSelection.length === 0"
          >审毕</el-button
        >
        <el-button
          type="primary"
          @click="toDownload()"
          :disabled="this.multipleSelection.length === 0"
          >下载</el-button
        >
        <el-button
          type="danger"
          @click="deleteVisible = true"
          :disabled="this.multipleSelection.length === 0"
          >删除</el-button
        >
      </div>
    </div>

    <el-dialog
      title="请选择退回类型"
      :visible.sync="rejectVisible"
      center
      width="30%"
    >
      <el-radio v-model="rejectType" label="通过">通过</el-radio>
      <el-radio v-model="rejectType" label="不通过">不通过</el-radio>
      <div slot="footer" class="dialog-footer">
        <el-button @click="rejectVisible = false">取 消</el-button>
        <el-button type="primary" @click="toReject()">确 定</el-button>
      </div>
    </el-dialog>

    <el-table
      class="infoTable"
      ref="infoTable"
      :data="showInfo"
      tooltip-effect="dark"
      @selection-change="handleSelectionChange"
      @row-click="handleRowClick"
      @row-contextmenu="tableRight"
      :header-cell-style="headStyle"
      :cell-style="rowStyle"
      :style="{ 'max-height': this.timeLineHeight + 'px' }"
      style="overflow-y: auto"
    >
      <el-table-column type="selection" min-width="5%"> </el-table-column>
      <el-table-column
        type="index"
        label="序号"
        :index="indexMethod"
        width="60px"
      >
      </el-table-column>
      <el-table-column
        prop="docname"
        label="文件名称"
        min-width="15%"
        show-overflow-tooltip
      >
      </el-table-column>
      <el-table-column
        prop="uploader"
        label="上传人员"
        min-width="8%"
        show-overflow-tooltip
      >
      </el-table-column>
      <el-table-column prop="uploadtime" label="上传时间" min-width="15%">
      </el-table-column>
      <el-table-column
        prop="sender"
        label="发送人员"
        min-width="8%"
        show-overflow-tooltip
      >
      </el-table-column>
      <el-table-column prop="status" label="当前状态" min-width="8%">
      </el-table-column>
      <el-table-column prop="type" label="文件类型" min-width="8%">
      </el-table-column>
      <el-table-column prop="operation" label="操作" min-width="20%">
        <template slot-scope="scope">
          <div>
            <el-tooltip
              class="item"
              effect="dark"
              content="预览"
              placement="top"
            >
              <el-button
                icon="el-icon-view"
                circle
                @click="toPreview(scope.row)"
                v-show="isShowAction('预览', scope.row.action)"
              ></el-button>
            </el-tooltip>
            <el-tooltip
              class="item"
              effect="dark"
              content="编辑"
              placement="top"
            >
              <el-button
                icon="el-icon-edit"
                circle
                @click="toEdit(scope.row)"
                v-show="isShowAction('编辑', scope.row.action)"
              ></el-button>
            </el-tooltip>
            <el-tooltip
              class="item"
              effect="dark"
              content="发送"
              placement="top"
            >
              <el-button
                icon="el-icon-s-promotion"
                circle
                @click="(sendVisible = true), showSend(scope.row)"
                v-show="isShowAction('发送', scope.row.action)"
              ></el-button>
            </el-tooltip>
            <el-tooltip
              class="item"
              effect="dark"
              content="审核"
              placement="top"
            >
              <el-button
                icon="el-icon-s-check"
                circle
                @click="(checkVisible = true), showCheck(scope.row)"
                v-show="isShowAction('审核', scope.row.action)"
              ></el-button>
            </el-tooltip>
            <el-tooltip
              class="item"
              effect="dark"
              content="共享"
              placement="top"
            >
              <el-button
                icon="el-icon-connection"
                circle
                @click="toShare(scope.row)"
                v-show="isShowAction('共享', scope.row.action)"
              ></el-button>
            </el-tooltip>
            <el-tooltip
              class="item"
              effect="dark"
              content="发布"
              placement="top"
            >
              <el-button
                icon="el-icon-unlock"
                circle
                @click="toPublish(scope.row)"
                v-show="isShowAction('发布', scope.row.action)"
              ></el-button>
            </el-tooltip>
            <el-tooltip
              class="item"
              effect="dark"
              content="版本记录"
              placement="top"
            >
              <el-button
                icon="el-icon-tickets"
                circle
                @click="(recordVisible = true), showRecord(scope.row)"
                v-show="isShowAction('版本记录', scope.row.action)"
              ></el-button>
            </el-tooltip>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div
      id="tableMenu"
      v-show="tableMenuVisible"
      @mouseleave="tableMenuVisible = !tableMenuVisible"
    >
      <el-card class="box-card">
        <div class="text item">
          <!-- <el-icon class="el-icon-star-off" v-if="!isCollected"></el-icon>
          <el-icon class="el-icon-star-on" v-else></el-icon> -->
          <el-link
            icon="el-icon-star-off"
            :underline="false"
            @click="toCollect()"
            v-if="!isCollected"
            >收藏</el-link
          >
          <el-link
            icon="el-icon-star-on"
            :underline="false"
            :disabled="isCollected"
            v-else
            >已收藏</el-link
          >
        </div>
        <div class="text item">
          <el-icon class="el-icon-data-analysis"></el-icon>
          <el-link :underline="false" @click="getTransInfo()">文件流转</el-link>
        </div>
      </el-card>
    </div>

    <el-dialog
      title="文件流转"
      :visible.sync="circleVisible"
      center
      width="30%"
    >
      <Timeline :mapper="mapper" :activities="activities" />
    </el-dialog>

    <div class="pagination">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="dataCount"
      >
      </el-pagination>
    </div>
  </el-main>
</template>

<script>
import Server from "@/utils/server";
import { mapState } from "vuex";
import Timeline from "@/components/Timeline.vue";

export default {
  name: "Content",
  components: { Timeline },
  data() {
    return {
      tableMenuVisible: false,
      tableInfo: [
        {
          index: "1",
          docname: "777777.docx",
          uploader: "hxy",
          uploadtime: "2022-11-1 13:00",
          sender: "hxy",
          status: "已读",
          type: "docx",
        },
      ],
      multipleSelection: [],

      isCollected: false,

      circleVisible: false,
      // reverse: true,
      mapper: {
        user: "用户",
        describe: "描述",
        state: "状态",
      },
      activities: [],

      deleteVisible: false,

      rejectVisible: false,
      rejectType: "通过",

      recordVisible: false,
      recordData: [
        {
          docName: "文件",
          editor: "张三",
          time: "2022-07-26 17:00",
          index: "1",
        },
      ],
      recordId: "",
      recordSelection: [],

      selectedRow: {
        id: "",
        isCollected: false,
      },

      sendVisible: false,
      sendOptions: [],
      selectedSend: [],

      checkVisible: false,
      checkOptions: [],
      selectedCheck: [],

      shareSelection: [],

      timeLineHeight: "",
    };
  },

  computed: {
    ...mapState({
      currentId: "currentId",
      currentPage: "currentPage",
      pageSize: "pageSize",
      showInfo: "showInfo",
      dataCount: "dataCount",
    }),
  },
  mounted() {
    this.timeLineHeight = document.documentElement.clientHeight - 210;
    window.onresize = () => {
      this.timeLineHeight = document.documentElement.clientHeight - 210;
    };
  },

  methods: {
    indexMethod(index) {
      return (this.currentPage - 1) * this.pageSize + index + 1;
    },

    tableRight(row, column, MouseEvent) {
      this.selectedRow.id = row.id;
      this.tableMenuVisible = true;
      MouseEvent.preventDefault();
      const tableMenu = document.querySelector("#tableMenu");
      tableMenu.style.cssText =
        "position: fixed; left: " +
        (MouseEvent.clientX - 10) +
        "px" +
        "; top: " +
        (MouseEvent.clientY - 25) +
        "px; z-index: 999; cursor:pointer;";
    },

    // 收藏文件
    toCollect() {
      Server.get(
        process.env.VUE_APP_SERVER + "/upload/collect/" + this.selectedRow.id
      ).then((json) => {
        console.log(json.data);
        this.$message({
          type: json.code === 200 ? "success" : "error",
          message: json.msg,
        });
      });
    },

    // 获取流转信息
    getTransInfo() {
      this.activities = [
        { content: "暂无流转信息", timestamp: "2022年9月16日 14:21" },
      ];
      Server.get(
        process.env.VUE_APP_SERVER + "/transfer/show/" + this.selectedRow.id
      ).then((json) => {
        if (json.code === 200) {
          if (json.data.length > 0) {
            this.activities = json.data.map((item) => {
              return {
                content: { user: item.operator, description: item.action },
                timestamp: item.time,
              };
            });
          }
          this.circleVisible = true;
        }
      });
    },

    isShowAction(opear, opears) {
      var flag = false;
      opears.forEach((item) => {
        if (opear === item) {
          flag = true;
        }
      });
      return flag;
    },

    toReject() {
      this.rejectVisible = false;
      var arr = "";
      this.multipleSelection.forEach((item) => {
        arr = arr + item.id + ",";
      });
      arr = arr.substring(0, arr.lastIndexOf(","));
      let body = {
        docIds: arr,
        type: this.rejectType,
      };
      Server.post(process.env.VUE_APP_SERVER + "/upload/back", body).then(
        (json) => {
          this.$message({
            type: json.code === 200 ? "success" : "error",
            message: json.msg,
          });
        }
      );
    },

    toDownload() {
      var arr = "";
      this.multipleSelection.forEach((item) => {
        arr = arr + item.id + ",";
      });
      arr = arr.substring(0, arr.lastIndexOf(","));
      window.open(process.env.VUE_APP_SERVER + "/upload/download/" + arr);
    },

    toDelete() {
      var arr = "";
      this.multipleSelection.forEach((item) => {
        arr = arr + item.id + ",";
      });
      arr = arr.substring(0, arr.lastIndexOf(","));
      Server.get(process.env.VUE_APP_SERVER + "/upload/delete/" + arr).then(
        (json) => {
          this.$message({
            type: json.code === 200 ? "success" : "error",
            message: json.msg,
          });
          if (json.code === 200) {
            this.$store.commit("refresh");
          }
          // setTimeout(() => {
          //   this.$store.commit('refresh')
          // }, 3000)
        }
      );
    },
    showSend(row) {
      this.selectedRow.id = row.id;
      Server.get(process.env.VUE_APP_SERVER + "/user/search").then((json) => {
        console.log(json.data);
        if (json.code === 200) {
          // json.data.forEach((item) => {
          //   this.sendOptions.push({ label: item.username, value: item.id })
          // })
          this.sendOptions = json.data.map((item) => ({
            value: item.id,
            label: item.username,
          }));
        }
      });
    },

    toSend() {
      if (!this.selectedSend) {
        this.$message({
          message: "请选择用户",
          type: "error",
        });
      }
      var str = "";
      this.selectedSend.forEach((item) => {
        str = str + item + ",";
      });
      str = str.substring(0, str.lastIndexOf(","));
      console.log("str:" + str);
      console.log("选择的文件id" + this.selectedRow.id);
      var body = { docIds: this.selectedRow.id, userIds: str };
      Server.post(process.env.VUE_APP_SERVER + "/upload/send", body).then(
        (json) => {
          this.$message({
            type: json.code === 200 ? "success" : "error",
            message: json.msg,
          });
        }
      );
    },

    showCheck(row) {
      this.selectedRow.id = row.id;
      Server.get(process.env.VUE_APP_SERVER + "/user/admin").then((json) => {
        console.log(json.data);
        if (json.code === 200) {
          this.checkOptions = json.data.map((item) => ({
            value: item.id,
            label: item.username,
          }));
          console.log(this.checkOptions);
        }
      });
    },
    toCheck() {
      if (!this.selectedCheck) {
        this.$message({
          message: "请选择用户",
          type: "error",
        });
      }
      var str = "";
      this.selectedCheck.forEach((item) => {
        str = str + item + ",";
      });
      str = str.substring(0, str.lastIndexOf(","));
      console.log("str:" + str);
      console.log("选择的文件id" + this.selectedRow.id);
      var body = { docIds: this.selectedRow.id, userIds: str };
      Server.post(process.env.VUE_APP_SERVER + "/upload/review", body).then(
        (json) => {
          console.log(json.data);
          this.$message({
            type: json.code === 200 ? "success" : "error",
            message: json.msg,
          });
        }
      );
    },

    // 获取多选框选中的值
    handleSelectionChange(val) {
      this.multipleSelection = val;
      // 将值传递给Top组件
      this.$store.commit("setInfoSelection", this.multipleSelection);
    },
    // 取消选择
    toggleSelection(rows) {
      if (rows) {
        rows.forEach((row) => {
          this.$refs.infoTable.toggleRowSelection(row);
        });
      } else {
        this.$refs.infoTable.clearSelection();
      }
    },
    handleRowClick(row, column, event) {
      this.$refs.infoTable.toggleRowSelection(row);
    },

    // 获取每页条数
    handleSizeChange(val) {
      this.$store.commit("setPageSize", val);
    },
    // 分页，获取当前页码
    handleCurrentChange(val) {
      this.$store.commit("setCurrentPage", val);
    },

    headStyle() {
      return "text-align:center";
    },
    rowStyle() {
      return "text-align:center";
    },

    handleClose(done) {
      this.$confirm("确认关闭？")
        .then((_) => {
          done();
        })
        .catch((_) => {});
    },

    toShare(row) {
      Server.post(process.env.VUE_APP_SERVER + "/upload/share", {
        id: row.id,
      }).then((json) => {
        this.$message({
          type: json.code === 200 ? "success" : "error",
          message: json.msg,
        });
        if (json.code === 200) {
          this.$store.commit("refresh");
        }
      });
    },

    toPublish(row) {
      Server.post(process.env.VUE_APP_SERVER + "/upload/release", {
        id: row.id,
      }).then((json) => {
        this.$message({
          type: json.code === 200 ? "success" : "error",
          message: json.msg,
        });
        if (json.code === 200) {
          this.$store.commit("refresh");
        }
      });
    },

    toEdit(row) {
      const routeUrl = this.$router.resolve({
        name: "OnlyOffice",
        query: {
          id: row.id,
        },
      });
      window.open(routeUrl.href, "_blank");
    },

    toPreview(row) {
      const base64 = require('js-base64').Base64;
      Server.get(process.env.VUE_APP_SERVER + "/docs/preview/" + row.id).then(
        (json) => {
          var url = process.env.VUE_APP_SERVER + "/" + json.data[0];
          if (json.code === 200) {
            window.open(
              process.env.VUE_APP_ONLYOFFICE_SERVER +
                ":8012/onlinePreview?url=" +
                encodeURIComponent(base64.encode(url))
            );
            this.$store.commit("refresh");
          }
        }
      );
    },

    // 获取版本记录选择值
    recordSelectionChange(val) {
      this.recordSelection = val;
    },
    recordRowClick(row, column, event) {
      this.$refs.recordTable.toggleRowSelection(row);
    },
    // 取消选择
    toggleRecord(rows) {
      if (rows) {
        rows.forEach((row) => {
          this.$refs.recordTable.toggleRowSelection(row);
        });
      } else {
        this.$refs.recordTable.clearSelection();
      }
    },
    reIndexMethod(index) {
      return this.recordData.length - index;
    },
    showRecord(row) {
      Server.get(process.env.VUE_APP_SERVER + "/version/" + row.id).then(
        (json) => {
          console.log(json.data);
          if (json.code === 200) {
            this.recordData = json.data;
          }
        }
      );
    },
    deleteRecord() {
      var arr = "";
      this.recordSelection.forEach((item) => {
        arr = arr + item.id + ",";
      });
      arr = arr.substring(0, arr.lastIndexOf(","));
      console.log(arr);
      Server.get(process.env.VUE_APP_SERVER + "/version/delete/" + arr).then(
        (json) => {
          console.log(json.data);
          this.$message({
            type: json.code === 200 ? "success" : "error",
            message: json.msg,
          });
          if (json.code === 200) {
            this.recordData = json.data;
          }
        }
      );
    },
    toLook(row) {
      console.log(row.id);
      Server.get(
        process.env.VUE_APP_SERVER + "/docs/previewVer/" + row.id
      ).then((json) => {
        console.log(json.data);
        var url = process.env.VUE_APP_SERVER + "/" + json.data;
        if (json.code === 200) {
          window.open(
            process.env.VUE_APP_ONLYOFFICE_SERVER +
              ":8012/onlinePreview?url=" +
              encodeURIComponent(url)
          );
          this.$store.commit("refresh");
        }
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.el-main {
  display: flex;
  flex-direction: column;
  min-height: 100%;
}

.infoTable {
  min-height: 75%;
}
.pagination {
  width: 100%;
  min-height: 10%;
  flex: 0;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: center;
  align-items: center;
  // .el-pagination{
  //   // min-width: 60%;
  // }
}

.send .el-select {
  margin-left: 10px;
  margin-bottom: 20px;
}
.limitSelect {
  margin-bottom: 30px;
  margin-left: 5px;
}
.check .el-select {
  margin-left: 10px;
  margin-bottom: 20px;
}

::v-deep .el-table th.el-table__cell > .cell {
  font-size: 18px;
}

::v-deep .el-card__body {
  padding: 10px;
}
.box-card .item {
  margin: 5px;
  .el-icon {
    margin-right: 5px;
  }
}

// 时间线
::v-deep .el-timeline {
  // 每一个item
  .el-timeline-item {
    //   线
    .el-timeline-item__tail {
      border-left: 2px solid #28567f;
    }
    // 圆点颜色
    .el-timeline-item__node {
      background-color: rgb(72, 147, 255);
    }
    // 圆点大小与位置
    .el-timeline-item__node--normal {
      left: -3px;
      width: 18px;
      height: 18px;
    }
    // 右边文字
    .el-timeline-item__wrapper {
      // 标题
      .el-timeline-item__content {
        color: #00e9ea;
      }
      //   内容
      .el-timeline-item__timestamp {
        color: #8393b0;
      }
    }
  }
}
</style>
