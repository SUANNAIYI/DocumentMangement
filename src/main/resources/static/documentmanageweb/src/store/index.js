import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    Authorization: '',
    currentuser: {},
    infoSelection: [],
    options: [],
    refreshCount: 0,
    currentId: '',
    currentPage: 0,
    pageSize: 0,
    showInfo: [],
    dataCount: 0,
    isSearch: false,
    isRetrieve: false
  },
  mutations: {
    setAuthorization (state, data) {
      state.Authorization = data
    },
    setCurrentuser (state, data) {
      state.currentuser = data
    },
    setInfoSelection (state, data) {
      state.infoSelection = data
    },
    setOptions (state, data) {
      state.options = data
    },
    setFilter (state, data) {
      state.filter_tableInfo = data
    },
    refresh (state) {
      state.refreshCount++
    },
    setCurrentId (state, data) {
      state.currentId = data
    },
    setCurrentPage (state, data) {
      state.currentPage = data
    },
    setPageSize (state, data) {
      state.pageSize = data
    },
    setShowInfo (state, data) {
      state.showInfo = data
    },
    setDataCount (state, data) {
      state.dataCount = data
    },
    setIsSearch (state, data) {
      state.isSearch = data
    },
    setIsRetrieve (state, data) {
      state.isRetrieve = data
    }
  },
  actions: {
  },
  getters: {
  },
  modules: {
  }
})
