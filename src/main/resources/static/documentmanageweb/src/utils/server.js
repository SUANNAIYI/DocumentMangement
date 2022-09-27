import store from '@/store'

const getToken = () => {
  return store.state.Authorization
}

const headers = {
  'Content-Type': 'application/json'
}

const _get = (url) => {
  return fetch(url, {
    headers: {
      ...headers,
      Authorization: getToken()
    }
  }).then(res => res.json())
}

const _post = (url, body) => {
  return fetch(url, {
    method: 'POST',
    headers: {
      ...headers,
      Authorization: getToken()
    },
    body: JSON.stringify(body)
  }).then(res => res.json())
}

const _postformdata = (url, body) => {
  return fetch(url, {
    method: 'POST',
    body,
    headers: {
      Authorization: getToken()
    }
  }).then(res => res.json())
}

const _delete = (url, body) => {
  return fetch(url, {
    method: 'DELETE',
    headers: {
      ...headers,
      Authorization: getToken()
    },
    body: JSON.stringify(body)
  }).then(res => res.json())
}

export default {
  get: _get,
  post: _post,
  postformdata: _postformdata,
  delete: _delete
}
