<!--onlyoffice 编辑器-->
<template>
  <div
    v-title data-title="编辑文件"
    style="height: 100vh"
    id="editorDiv"
    :option="option"
    :document-url="documentUrl"></div>
</template>

<script>

import { handleDocType } from '../common/utils'
import Server from '@/utils/server'
import { mapState } from 'vuex'
import { option } from 'yargs'
import { json } from 'body-parser'

export default {
  name: 'OnlyOffice',
  data () {
    return {
      doctype: '',
      documentUrl: '',
      option: {
        url: '',
        title: '',
        fileType: '',
        isEdit: true,
        key: '',
        isPrint: '',
        user: {
          id: '',
          name: ''
        },
        token: '',
        height: '100%',
        width: '100%',
        permissions: {
          copy: true, // 定义内容是否可以复制到剪贴板。如果该参数设置为false，则只能在当前文档编辑器中粘贴内容。默认值为true。
          download: true, // 定义文档是可以下载还是只能在线查看或编辑。如果下载权限设置为“假”的下载为...菜单选项将是缺席的文件菜单。默认值为true。
          edit: '', // 定义文档是可以编辑还是只能查看。如果编辑权限设置为“true”，则文件菜单将包含编辑文档菜单选项；请注意，如果编辑权限设置为“false”，文档将在查看器中打开，即使模式参数设置为edit，您也无法将其切换到编辑器。默认值为true。
          print: true // 定义是否可以打印文档。如果打印权限设置为“false”的打印菜单选项将是缺席的文件菜单。默认值为true
        },
        editorConfig: {
          // 语言：zh-CN简体中文/en英文
          lang: "zh",
          // 阅读状态 view/edit
          mode: 'edit',
          customization: {
            // 是否显示插件
          plugins: false
          }
        }
      }
    }
  },
  computed: {
    ...mapState({
      currentuser: 'currentuser'
    })
  },

  created () {
    const script = document.createElement('script')
    script.type = 'text/javascript'
    script.src = process.env.VUE_APP_ONLYOFFICE_SERVER + '/web-apps/apps/api/documents/api.js'
    document.getElementsByTagName('head')[0].appendChild(script)
    console.log(document.getElementsByTagName('head')[0])
  },

  mounted () {
    Server.post(process.env.VUE_APP_SERVER + '/docs/edit', { id: this.$route.query.id }).then((json) => {
      this.$message({
          type: json.code === 200 ? 'success' : 'error',
          message: json.msg
        })
        if (json.code === 200) {
          console.log('返回的数据' + json.data)
          var url = process.env.VUE_APP_SERVER + '/' + json.data[0]
          this.option.url = url
          this.option.title = json.data[1]
          this.option.fileType = json.data[2]
          this.option.key = json.data[3]
          this.option.permissions.isEdit = true
          this.option.user.name = this.currentuser.username
        }
      })
      if (this.option.url) {
        this.setEditor(this.option)
      }
  },
  methods: {
    setEditor (option) {
      this.doctype = handleDocType(option.fileType)
      console.log('url:' + option.url)
      var arr = this.$route.query.id + ',' + this.currentuser.username
      const config = {
        document: {
          fileType: option.fileType,
          key: option.key,
          title: option.title,
          permissions: {
            comment: true,
            download: true,
            modifyContentControl: true,
            modifyFilter: true,
            print: true,
            edit: option.isEdit,
            fillForms: true,
            review: true
          },
          url: option.url
        },
        documentType: this.doctype,
        editorConfig: {
          callbackUrl: process.env.VUE_APP_SERVER +'/docs/saveFile/' + arr,
          lang: option.editorConfig.lang,
          customization: {
            commentAuthorOnly: false,
            comments: true,
            compactHeader: false,
            compactToolbar: true,
            feedback: false,
            plugins: option.editorConfig.customization.plugins,
            autosave: false,
            forcesave: false,
            features: {
                spellcheck: {
                    mode: false,
                }
            },
            services: {
              CoAuthoring:{
                server:{
                  savetimeoutdelay: 50
                }
              }
            }
          },
          user: option.user,
          mode: option.model ? option.model : 'edit'
        },
        width: '100%',
        height: '100%',
        token: option.token
      }
      console.log('setEditor ==> option:', option, 'config:', config)
      this.$nextTick(() => {
        setTimeout(function () {
          const docEditor = new window.DocsAPI.DocEditor('editorDiv', config)
          console.log('docEditor:' + docEditor)
        }, 1500)
      })
    }
  },
  watch: {
    option: {
      handler: function (n, o) {
        this.setEditor(n)
        this.doctype = handleDocType(n.fileType)
      },
      deep: true
    }
  }
}
</script>

<style lang="scss" scoped>
#app-title{
      height: 40px !important;
      element.style{
        height: 40px !important;
      }
    }

</style>
