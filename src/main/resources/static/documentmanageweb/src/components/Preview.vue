<template>
  <div>
    <template v-if="fileType == 'text'">
      <div></div>
    </template>
    <template v-else-if="fileType == 'pdf'">
      <iframe :src="src" frameborder="0"></iframe>
    </template>
    <template v-else-if="fileType == 'image'">
      <img :src="src" alt="">
    </template>
  </div>
</template>

<script>
// import Server from '@/utils/server'

const fileTypeMap = {
  text: ['txt'],
  pdf: ['pdf'],
  image: ['png', 'jpg', 'bmp']
}
export default {
  name: 'Preview',
  computed: {
    extra () {
      return this.src.split('.').pop()
    },
    fileType () {
      for (const key in fileTypeMap) {
        if (fileTypeMap[key].indexOf(this.extra) > -1) return key
      }
      return 'others'
    }
  },
  data () {
    return {
      src: '',
      text: ''
    }
  },
  beforeMount () {
    this.src = this.$route.query.src
    if (this.fileType === 'text') {
      fetch(this.src).then(res => res.text()).then(text => { this.text = text })
    }
  },
  methods: {
    showPreview () {
      fetch.get(this.src).then(res => {
        console.log(res)
        res.text()
      }).then()
    }
  }
}
</script>

<style>

</style>
