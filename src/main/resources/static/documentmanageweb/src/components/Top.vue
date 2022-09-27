<template>
  <div class="header">
    <el-row :gutter="20">
      <el-col :span="17">
        <div class="search">
          <div>
            <el-input
              placeholder="请输入关键词"
              v-model="searchInput"
              class="searchInput"
              @keyup.enter.native="searchInfo()"
            >
            </el-input>
            <el-button
              icon="el-icon-search"
              circle
              @click="searchInfo()"
            ></el-button>
          </div>
          <div>
            <img
              class="switch"
              ref="switchDirection"
              src="../assets/img/bottom.svg"
              @click.stop="SwitchSearch"
            />
          </div>
        </div>
        <div class="retrieve" v-show="isShowRetrieve">
          <el-form
            class="retrieveForm"
            :model="retrieveForm"
            label-width="80px"
            :rules="rules"
            ref="retrieveForm"
          >
            <el-form-item label="上传时间">
              <el-date-picker
                v-model="retrieveForm.time"
                type="datetimerange"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                value-format="yyyy-MM-dd HH:mm:ss"
                :default-time="['00:00:00', '23:59:59']"
              >
              </el-date-picker>
            </el-form-item>
            <el-form-item label="上传人员">
              <el-select
                v-model="retrieveForm.uploader"
                filterable
                clearable
                placeholder="请选择"
              >
                <el-option
                  v-for="item in uploaderOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.label"
                >
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="发送人员">
              <el-select
                v-model="retrieveForm.sender"
                filterable
                clearable
                placeholder="请选择"
              >
                <el-option
                  v-for="item in senderOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.label"
                >
                </el-option>
              </el-select>
            </el-form-item>
            <!-- <el-form-item label="当前状态">
              <el-select class="select" v-model="retrieveForm.status" placeholder="请选择">
                <el-option label="未读" value="nono"></el-option>
                <el-option label="已阅" value="read"></el-option>
                <el-option label="编辑" value="edit"></el-option>
                <el-option label="审核" value="check"></el-option>
                <el-option label="发布" value="publish"></el-option>
              </el-select>
            </el-form-item> -->
            <el-form-item class="select" clearable label="文件类型">
              <el-select v-model="retrieveForm.type" placeholder="请选择">
                <el-option label=".txt" value=".txt"></el-option>
                <el-option label=".docx" value=".docx"></el-option>
                <el-option label=".pdf" value=".pdf"></el-option>
                <el-option label=".pptx" value=".pptx"></el-option>
                <el-option label=".xlsx" value=".xlsx"></el-option>
                <el-option label=".jpeg" value=".jpeg"></el-option>
                <el-option label=".jpg" value=".jpg"></el-option>
                <el-option label=".png" value=".png"></el-option>
                <el-option label=".gif" value=".gif"></el-option>
                <el-option label=".mp4" value=".mp4"></el-option>
                <el-option label=".flv" value=".flv"></el-option>
                <el-option label=".mp3" value=".mp3"></el-option>
              </el-select>
            </el-form-item>
          </el-form>
          <div class="retrieveBtn">
            <el-button type="primary" @click="toRetrieve('retrieveForm')"
              >检索</el-button
            >
            <el-button @click="SwitchSearch()">取消</el-button>
          </div>
        </div>
      </el-col>
      <el-col :span="7"
        ><div class="person_center">
          <div @click="uploadVisible = true" style="margin-right: 30px">
            <img title="上传" src="../assets/img/upload.svg" />
          </div>
          <div style="margin-right: 30px">
            <el-badge
              :value="filter(messageList)"
              :hidden="hiddenMessageNum"
              class="item"
            >
              <el-popover
                placement="top-start"
                width="450"
                trigger="click"
                :disabled="messageList.length === 0"
              >
                <div class="messageGroups">
                  <div
                    class="message"
                    :class="[
                      item.readFlag === 'true'
                        ? 'message_read'
                        : 'message_unread',
                    ]"
                    v-for="(item, index) in messageList"
                    :key="index"
                    @click="readMessage(item)"
                  >
                    <div class="date">{{ item.date }}</div>
                    <div class="content">{{ item.content }}</div>
                    <div class="title" :title="item.title">
                      <strong>标题:</strong> {{ item.title }}
                    </div>
                  </div>
                </div>
                <div
                  style="text-align: right; margin-top: 10px"
                  v-show="messageList.length > 0"
                >
                  <el-button type="primary" @click="clearMessage()"
                    >一键清空</el-button
                  >
                </div>
                <img
                  :title="hiddenMessageNum ? '暂无数据' : '消息中心'"
                  slot="reference"
                  src="../assets/img/message.svg"
                  style="width: 42px"
                />
              </el-popover>
            </el-badge>
          </div>
          <div>
            <el-dropdown>
              <div style="display: flex">
                <img
                  src="../assets/img/person.svg"
                  style="height: 36px; margin-top: 10px"
                />
                <div style="cursor: pointer; font-size: 20px">
                  {{ currentuser.username }}
                </div>
              </div>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item
                  v-if="this.currentuser.username === 'admin'"
                  @click.native="toPersonal()"
                  >个人中心</el-dropdown-item
                >
                <el-dropdown-item @click.native="logoutVisible = true"
                  >退出登录</el-dropdown-item
                >
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </div></el-col
      >
    </el-row>
    <el-dialog
      class="upload"
      title="上传"
      :visible.sync="uploadVisible"
      width="35%"
      center
    >
      <div>
        <el-button @click="upload" type="primary">点击上传</el-button>
        <div v-for="(file, index) in selectedFile" :key="index">
          <i class="el-icon-document"></i>{{ file.name }}
          <el-button
            type="danger"
            icon="el-icon-delete"
            circle
            class="btnDel"
            size="mini"
            @click="delSelected(index)"
          ></el-button>
        </div>
      </div>
      <div slot="footer" class="upload-footer">
        <div>
          <span>位置选择</span>
          <el-cascader
            class="selectedFolder"
            v-model="selectedFolder"
            :options="options"
            :props="folderProps"
          >
          </el-cascader>
        </div>
        <el-button @click="uploadVisible = false">取 消</el-button>
        <el-button type="primary" @click="(uploadVisible = false), toUpload()"
          >确 定</el-button
        >
      </div>
    </el-dialog>

    <el-dialog
      class="logout"
      title="提示"
      :visible.sync="logoutVisible"
      width="30%"
      height="50%"
    >
      <span>确认退出登录？</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="logoutVisible = false">取 消</el-button>
        <el-button type="primary" @click="(logoutVisible = false), toLogout()"
          >确 定</el-button
        >
      </span>
    </el-dialog>
  </div>
</template>

<script>
import Server from "@/utils/server";
import Cookie from "@/utils/cookie";
import { mapState } from "vuex";

export default {
  name: "Top",
  data() {
    var validateTime = (rule, value, callback) => {
      if (value < this.retrieveForm.start) {
        callback(new Error("结束日期不能小于开始日期"));
      } else {
        callback();
      }
    };
    return {
      username: "",

      searchInput: "",
      isShowRetrieve: false,

      retrieveForm: {
        uploader: "",
        time: [],
        sender: "",
        type: "",
      },
      uploaderOptions: [],
      senderOptions: [],
      rules: {
        end: [{ validator: validateTime, trigger: "blur" }],
      },

      logoutVisible: false,
      uploadVisible: false,
      selectedFile: [],
      selectedFolder: [],

      messageList: [
        // {
        //   date: '2022年8月11日 22:44',
        //   content: '哇哈哈啊哈哈哈',
        //   title: '这是我的测试文件',
        //   readFlag: false
        // },
        // {
        //   date: '2022年8月11日 22:44',
        //   content: '哇哈哈啊哈哈哈',
        //   title: '这是我的测试文件',
        //   readFlag: false
        // }
      ],
      hiddenMessageNum: true,

      folderProps: {
        label: "foldername",
        value: "id",
        checkStrictly: true,
      },

      wsUrl: (process.env.VUE_APP_WS_SERVER + "")
        .replace("https", "wss")
        .replace("http", "ws"), // ws地址
      ws: null, // 建立的连接
      lockReconnect: false, // 是否真正建立连接
      timeout: 5 * 1000, // 58秒一次心跳
      timeoutObj: null, // 心跳心跳倒计时
      serverTimeoutObj: null, // 心跳倒计时
      timeoutnum: null, // 断开 重连倒计时
    };
  },
  computed: {
    ...mapState({
      currentuser: "currentuser",
      infoSelection: "infoSelection",
      options: "options",
      currentId: "currentId",
      currentPage: "currentPage",
      pageSize: "pageSize",
      showInfo: "showInfo",
      dataCount: "dataCount",
      isSearch: "isSearch",
      isRetrieve: "isRetrieve",
    }),
  },

  created() {
    this.initWebSocket();
    // 获取已有消息列表
    this.getMessageList();
  },
  watch: {
    currentPage() {
      if (this.isSearch) {
        this.searchInfo();
      } else if (this.isRetrieve) {
        this.toRetrieve();
      }
    },
    pageSize() {
      if (this.isSearch) {
        this.searchInfo();
      } else if (this.isRetrieve) {
        this.toRetrieve();
      }
    },
  },

  methods: {
    // 获取消息列表
    getMessageList() {
      Server.get(
        process.env.VUE_APP_SERVER + "/message/show/" + this.currentuser.id
      ).then((json) => {
        if (json.code === 200) {
          if (json.data.length > 0) {
            this.messageList = json.data.map((item) => {
              return {
                id: item.id,
                date: item.date,
                content: item.content,
                title: item.title,
                readFlag: item.flag
              };
            });
            // console.log(this.messageList);
          }
          else{
            this.messageList = []
          }
        }
      });
    },

    // 清空消息列表
    clearMessage() {
      Server.get(
        process.env.VUE_APP_SERVER + "/message/delete/" + this.currentuser.id
      ).then((json) => {
        // this.$message({
        //   type: json.code === 200 ? "success" : "error",
        //   message: json.msg,
        // });
        this.getMessageList();
      });
    },

    initWebSocket() {
      if (typeof WebSocket === "undefined") {
        return console.log("您的浏览器不支持websocket");
      }
      this.ws = new WebSocket(
        this.wsUrl + "/web/ws/projectId/" + this.currentuser.username
      );
      this.ws.onmessage = this.websocketonmessage;
      this.ws.onopen = this.websocketonopen;
      this.ws.onerror = this.websocketonerror;
      this.ws.onclose = this.websocketclose;
    },
    reconnect() {
      // 重新连接
      var that = this;
      if (that.lockReconnect) {
        return;
      }
      that.lockReconnect = true;
      // 没连接上会一直重连，设置延迟避免请求过多
      that.timeoutnum && clearTimeout(that.timeoutnum);
      that.timeoutnum = setTimeout(function () {
        // 新连接
        that.initWebSocket();
        that.lockReconnect = false;
      }, 5000);
    },
    reset() {
      // 重置心跳
      var that = this;
      // 清除时间
      clearTimeout(that.timeoutObj);
      clearTimeout(that.serverTimeoutObj);
      // 重启心跳
      that.start();
    },
    start() {
      // 开启心跳
      console.log("开启心跳");
      var self = this;
      self.timeoutObj && clearTimeout(self.timeoutObj);
      self.serverTimeoutObj && clearTimeout(self.serverTimeoutObj);
      self.timeoutObj = setTimeout(function () {
        // 这里发送一个心跳，后端收到后，返回一个心跳消息，
        if (self.ws.readyState === 1) {
          // 如果连接正常
          self.ws.send("ping"); // 这里可以自己跟后端约定
        } else {
          // 否则重连
          self.reconnect();
        }
        self.serverTimeoutObj = setTimeout(function () {
          // 超时关闭
          self.ws.close();
        }, self.timeout);
      }, self.timeout);
    },
    websocketonopen() {
      console.log("websocket连接成功");
      // 连接建立之后执行send方法发送数据
      // 开启心跳
      this.start();
    },
    websocketonerror() {
      // 连接建立失败重连
      console.log("WebSocket连接发生错误");
      // 重连
      this.reconnect();
    },
    websocketonmessage(e) {
      // 数据接收
      if (e.data === "pong") {
      } else {
        console.log(e.data);
        const data = JSON.parse(e.data);
        this.messageList.unshift(data);
      }
      // 收到服务器信息，心跳重置
      this.reset();
    },
    websocketsend(Data) {
      // 数据发送
      this.websock.send(Data);
    },
    websocketclose(e) {
      // 关闭
      console.log("断开连接", e);
    },

    filter(data) {
      var newArrays = this.messageList.filter(function (res) {
        return res.readFlag === "false";
      });
      var length = newArrays.length;
      if (length === 0) {
        this.hiddenMessageNum = true;
      } else {
        this.hiddenMessageNum = false;
      }
      return newArrays.length;
    },

    SwitchSearch() {
      if (!this.isShowRetrieve) {
        // require 动态引入图片
        this.$refs.switchDirection.src = require("../assets/img/top.svg");
        this.$refs.switchDirection.style.top = "426px";
        this.isShowRetrieve = true;
        // 获取用户列表
        Server.get(process.env.VUE_APP_SERVER + "/user/search").then((json) => {
          console.log(json.data);
          if (json.code === 200) {
            this.uploaderOptions = json.data.map((item) => ({
              value: item.id,
              label: item.username,
            }));
            this.senderOptions = this.uploaderOptions;
          }
        });
      } else {
        this.$refs.switchDirection.src = require("../assets/img/bottom.svg");
        this.$refs.switchDirection.style.top = "34px";
        // 如果把this.retrieveForm设置为空，retrieveForm.uploader 就是 undefined，此时就会赋值不上
        // this.retrieveForm = ''
        // this.retrieveForm = {
        //   uploader: '',
        //   time: [],
        //   sender: '',
        //   status: '',
        //   type: ''
        // }
        this.isShowRetrieve = false;
      }
    },

    upload() {
      const input = document.createElement("input");
      input.type = "file";
      input.multiple = true;
      input.onchange = (e) => {
        for (const file of e.target.files) {
          this.selectedFile.push(file);
        }
      };
      // 文件夹选择
      input.click();
    },
    toUpload() {
      const fd = new FormData();
      if (!this.selectedFile) {
        this.$message({
          message: "请选择文件",
          type: "error",
        });
      } else if (!this.selectedFolder) {
        this.$message({
          message: "请选择文件存放位置",
          type: "error",
        });
      } else {
        fd.append("folder", this.selectedFolder);
        for (const file of this.selectedFile) {
          fd.append("file", file);
        }
        const index = this.selectedFolder[this.selectedFolder.length - 1];
        this.$store.commit("setCurrentId", index);
        Server.postformdata(process.env.VUE_APP_SERVER + "/upload", fd).then(
          (json) => {
            this.$message({
              type: json.code === 200 ? "success" : "error",
              message: json.msg,
            });
            if (json.code === 200) {
              this.$store.commit("refresh");
            }
          }
        );
      }
    },
    delSelected(index) {
      this.selectedFile.splice(index, 1);
    },

    // handleSelect (key) {
    //   Server.post(process.env.VUE_APP_SERVER + '/docs/folder', { id: key }).then((json) => {
    //     this.$store.commit('setFilter', json.data)
    //   })
    // },

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

    searchInfo() {
      if (!this.searchInput.trim()) {
        this.$message({
          message: "请输入内容",
          duration: "1000",
        });
      }
      var body = {
        texts: this.searchInput,
        currentPage: this.currentPage,
        pageSize: this.pageSize,
      };
      Server.post(process.env.VUE_APP_SERVER + "/docs/keySearch", body).then(
        (json) => {
          console.log(json.data);
          if (json.code === 200) {
            this.$store.commit("setShowInfo", json.data.records);
            this.$store.commit("setDataCount", json.data.total);
            this.$store.commit("setCurrentPage", json.data.current);
            this.$store.commit("setPageSize", json.data.size);
          }
        }
      );
    },
    toRetrieve() {
      this.$store.commit("setIsSearch", true);
      var body = {
        currentPage: this.currentPage,
        pageSize: this.pageSize,
        uploader: this.retrieveForm.uploader,
        time: this.retrieveForm.time,
        sender: this.retrieveForm.sender,
        status: this.retrieveForm.status,
        type: this.retrieveForm.type,
      };
      Server.post(process.env.VUE_APP_SERVER + "/docs/search", body).then(
        (json) => {
          console.log(json.data);
          if (json.code === 200) {
            this.$refs.switchDirection.src = require("../assets/img/bottom.svg");
            this.$refs.switchDirection.style.top = "34px";
            // this.retrieveForm = {
            //   uploader: '',
            //   time: [],
            //   sender: '',
            //   status: '',
            //   type: ''
            // }
            this.isShowRetrieve = false;
            this.$store.commit("setShowInfo", json.data.records);
            this.$store.commit("setDataCount", json.data.total);
            this.$store.commit("setCurrentPage", json.data.current);
            this.$store.commit("setPageSize", json.data.size);
          }
        }
      );
    },

    toPersonal() {
      this.$router.push("/personal");
    },

    toLogout() {
      this.$store.commit("setAuthorization", "");
      this.$store.commit("setCurrentuser", {});
      Cookie.setCookie("Authorization", "", 0);
      Cookie.setCookie("userinfo", "", 0);
      this.$router.push("/");
    },
    readMessage(item) {
      item.readFlag = "true";
      Server.get(process.env.VUE_APP_SERVER + "/message/read/" + item.id);
    },
  },
};
</script>

<style lang="scss" scoped>
.header {
  height: 60px;
  line-height: 60px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.header .search {
  display: flex;
  float: right;
  width: auto;

  .searchInput {
    width: 36vw;
  }

  .el-button {
    width: 40px;
    height: 40px;
    margin-left: 5px;
  }
}

.retrieve {
  width: 33.3vw;
  height: 350px;
  background-color: #fff;
  position: relative;
  float: right;
  right: 74px;
  top: -5px;
  padding: 20px;
  z-index: 999;
  font-size: 16px;
  position: relative;
  border-style: inset;
  border-color: #333;
  border-width: 1px;

  .retrieveForm {
    position: absolute;
    top: 45%;
    left: 50%;
    transform: translate(-75%, -50%);
  }

  .retrieveBtn {
    // position: relative;
    // left: 212px;
    position: absolute; /*在子元素中使用绝对定位*/
    top: 90%; /*距离相对于父元素的高*/
    left: 50%;
    -webkit-transform: translate(
      -50%,
      -50%
    ); /*CSS3的样式，:translate(-50%,-50%)相对于自己距离x轴和y轴的-50%*/
    transform: translate(-50%, -50%);
  }
}

.switch {
  width: 20px;
  position: relative;
  cursor: pointer;
  right: calc(19vw + 50px);
  z-index: 1000;
  top: 34px;
}

.el-dialog .el-cascader {
  margin-left: 5px;
  margin-bottom: 20px;
}

.header .person_center {
  float: right;
  margin: 0 50px;
  display: flex;
  height: 60px;
}

.person_center img {
  width: 40px;
  margin-top: 7px;
  cursor: pointer;
}

::v-deep .el-badge__content.is-fixed {
  top: 14px;
  right: 18px;
}
.header .el-dropdown {
  font-size: 20px;
}

.messageGroups {
  max-height: 375px;
  overflow: auto;
}

.message {
  height: 100px;
  padding: 10px;
  margin-bottom: 10px;
  cursor: pointer;

  .date {
    font-weight: 800;
    font-size: 18px;
  }
  .content {
    font-size: 18px;
    padding: 10px 0 10px 20px;
  }
  .title {
    white-space: nowrap; //禁止换行
    overflow: hidden;
    text-overflow: ellipsis; //...
    font-size: 18px;
  }
}

.message_unread {
  background-color: #acc2e1;
}

.message_read {
  background-color: #96989c;
}
</style>
