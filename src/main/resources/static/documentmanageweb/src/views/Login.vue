<template>
  <div class="login-wrap" :style="{width: fullWidth+'px', height:fullHeight+'px'}">
    <el-container direction="vertical" class="wjLogin" :style="{width: fullWidth+'px'}">
      <div class="loginTitle"><h3>文件整编系统</h3></div>
      <el-form :model="loginForm" :rules="loginRules" ref="loginForm" label-width="80px" class="loginForm">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" ></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" show-password
          @keyup.enter.native="submitLogin('loginForm')">
          </el-input>
        </el-form-item>
      </el-form>
      <div class="login-btn">
        <el-button @click="toSignup()">注册</el-button>
        <el-button type="primary" @click="submitLogin('loginForm')">登录</el-button>
      </div>
    </el-container>
  </div>
</template>

<script>
import Server from '@/utils/server'
import Cookie from '@/utils/cookie'
export default {
  name: 'Login',
  data () {
    return {
      fullWidth: document.documentElement.clientWidth,
      fullHeight: document.documentElement.clientHeight,

      loginForm: {
        username: '',
        password: ''
      },
      loginRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' }
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

  // mounted () {
  //   this.addEnterListener()
  // },
  // destroyed () {
  //   this.removeEnterListener()
  // },

  methods: {
    // addEnterListener () {
    //   if (window.__completeEnterBind__) return
    //   window.addEventListener('keydown', this.enterCallback)
    //   window.__completeEnterBind__ = true
    // },
    // removeEnterListener () {
    //   window.removeEventListener('keydown', this.enterCallback)
    //   window.__completeEnterBind__ = false
    // },
    // enterCallback (e) {
    //   function findFormItem (el) {
    //     const parent = el.parentElement
    //     if (!parent) return document.body
    //     if (
    //       parent.className.includes('el-form-item') &&
    //       parent.className.includes('el-form-item--small')
    //     ) {
    //       return parent
    //     }
    //     return findFormItem(parent)
    //   }
    //   function findInput (container) {
    //     let nextEl = container.nextElementSibling
    //     if (!nextEl) return
    //     let input = nextEl.querySelector('input')
    //     while (input.id === 'el-select') {
    //       nextEl = nextEl.nextElementSibling
    //       if (!nextEl) return
    //       input = nextEl.querySelector('input')
    //     }
    //     if (input.className.includes('el-input__inner')) return input
    //   }
    //   if (e.keyCode === 13) {
    //     const container = findFormItem(document.activeElement)
    //     findInput(container) && findInput(container).focus()
    //   }
    // },

    handleResize () {
      this.fullWidth = document.documentElement.clientWidth
      this.fullHeight = document.documentElement.clientHeight
    },

    toSignup () {
      this.$router.push('/signup')
      this.loginForm = {
        username: '',
        password: ''
      }
    },
    submitLogin (formName) {
      this.$refs[formName].validate((valid) => {
        if (!valid) {
          return
        }
        Server.post(process.env.VUE_APP_SERVER + '/user/login', this.loginForm).then(json => {
          if (json.code === 200) {
            this.$message({
              message: '登录成功!',
              type: 'success'
            })
            this.$store.commit('setAuthorization', json.data.token)
            this.$store.commit('setCurrentuser', {id: json.data.id, username: json.data.username})
            Cookie.setCookie('Authorization', json.data.token, 1)
            Cookie.setCookie('userinfo', JSON.stringify({id: json.data.id, username: json.data.username}), 1)
            this.$router.push('/home')
          } else {
            this.$message({
              message: '账号不存在或密码错误!',
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

  .login-wrap {/*整个大的界面样式*/
    position: absolute;
    top: 0;
    left: 0;
    width: 101%;
    height: 101%;
    background-image: url(../assets/img/background.jpg);
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center center;
    opacity:0.8;
  }

  .wjLogin {/*登录框的样式*/
    position: absolute;/*position 属性规定元素的定位类型；absolute生成绝对定位的因素*/
    left: 50%;
    top: 50%;
    width: 350px;
    transform: translate(-50%, -50%);
    overflow: hidden;

    .loginTitle {
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
  .loginForm {/*输入框的内边距*/
    padding: 30px 30px;
    margin: 0 auto;
  }
  .loginForm .el-form-item__label {
    color: #fff !important;
  }
  .login-btn{
    margin: 0 auto;
    margin-bottom: 30px;
  }
</style>
