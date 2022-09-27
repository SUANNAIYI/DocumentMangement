<template>
  <div class="signup-wrap" :style="{width: fullWidth+'px', height:fullHeight+'px'}">
    <el-container direction="vertical" class="wjSignup" :style="{width: fullWidth+'px'}">
      <div class="signupTitle"><h3>注册账号</h3></div>
      <el-form :model="signupForm" :rules="signupRules" ref="signupForm" label-width="100px" class="signupForm">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="signupForm.username"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="signupForm.password" show-password></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="re_pwd">
          <el-input v-model="signupForm.re_pwd" show-password></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="signupForm.email"></el-input>
        </el-form-item>
      </el-form>
      <div class="signup-btn">
        <el-button @click="toLogin()">返回</el-button>
        <el-button type="primary" @click="submitSignup('signupForm')">注册</el-button>
      </div>
    </el-container>
  </div>
</template>

<script>
import Server from '@/utils/server'
export default {
  name: 'Signup',
  data () {
    var validatePassword = (rule, value, callback) => {
      if (value !== '') {
        if (value.length < 6) {
          callback(new Error('请输入至少6位的密码'))
          return false
        } else if (
          !/^(?=.*[A-Za-z])(?=.*\d)(?=.*[@!*#$%&_=])[A-Za-z\d@!*#$%&_=]{8,18}$/.test(value)
        ) {
          callback(new Error('密码必须包含字母、数字和特殊字符'))
          return false
        } else {
          callback()
        }
      }
    }
    var validateRepwd = (rule, value, callback) => {
      if (value !== '') {
        if (value.length < 6) {
          callback(new Error('请输入至少6位确认密码'))
          return false
        } else if (value !== this.signupForm.password) {
          callback(new Error('两次输入的新密码不一致'))
          return false
        } else {
          callback()
        }
      }
    }
    return {
      fullWidth: document.documentElement.clientWidth,
      fullHeight: document.documentElement.clientHeight,

      signupForm: {
        username: '',
        password: '',
        re_pwd: '',
        email: ''
      },
      signupRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 2, max: 5, message: '长度在2到5个字符之间', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { validator: validatePassword, trigger: 'blur' }
        ],
        re_pwd: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: validateRepwd, trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入您的邮箱', trigger: 'blur' },
          { required: true, message: '请输入正确的邮箱', pattern: /^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(\.[a-zA-Z0-9_-])+/, trigger: 'blur' }
        ]
      }
    }
  },

  created () {
    window.addEventListener('resize', this.handleResize)
  },
  beforeUnmount () {
    window.removeEventListener('resize', this.handleResize)
  },

  methods: {
    toLogin () {
      this.$router.push('/')
      this.signupForm = {
        username: '',
        password: '',
        re_pwd: '',
        email: ''
      }
    },
    submitSignup (formName) {
      this.$refs[formName].validate((valid) => {
        if (!valid) {
          return
        }
        Server.post(process.env.VUE_APP_SERVER + 'user/register', this.signupForm).then(json => {
          console.log(json.data)
          if (json.code === 200) {
            this.$message({
              message: '注册成功！',
              type: 'success'
            })
            this.$router.push('/home')
          } else {
            this.$message({
              message: '注册失败！',
              type: 'error'
            })
          }
        })
      })
    }
  }
}
</script>

<style lang="scss">
  .signup-wrap {/*整个大的界面样式*/
    position: absolute;
    top: 0;
    left: 0;
    width: 101%;
    height: 101%;
    background-image: url('../assets/img/background.jpg');
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center center;
    opacity:0.8;/*透明度*/
  }
  .wjSignup {/*登录框的样式*/
    position: absolute;/*position 属性规定元素的定位类型；absolute生成绝对定位的因素*/
    left: 50%;
    top: 50%;
    width: 350px;
    transform: translate(-50%, -50%);
    overflow: hidden;

    .signupTitle {/*登录框的样式*/
      width: 500px;
      position: relative;
      left: 50%;
      top: 50%;
      transform: translate(-50%, -25%);
      line-height: 70px;
      text-align: center;
      font-size: 20px;
      color: #fff;
      border-bottom: 1px solid ;
    }
  }
  .signupForm {/*输入框的内边距*/
    padding: 30px 30px;
    margin: 0 auto;
  }
  .signupForm .el-form-item__label {
    color: #fff !important;
  }
  .signup-btn{
    margin: 0 auto;
    margin-bottom: 30px;
  }
</style>
