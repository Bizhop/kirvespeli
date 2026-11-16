import { getToken } from "../auth/token.ts"

export const getPayload = props => ({
  request: {
    url: props.url,
    method: "get",
    headers: {
      Authorization: getToken(),
    },
  },
})

export const postPayload = props => ({
  request: {
    url: props.url,
    method: "post",
    data: props.data,
    headers: {
      Authorization: getToken(),
    },
  },
})

export const putPayload = props => ({
  request: {
    url: props.url,
    method: "put",
    data: props.data,
    headers: {
      Authorization: getToken(),
    },
  },
})

export const deletePayload = props => ({
  request: {
    url: props.url,
    method: "delete",
    headers: {
      Authorization: getToken(),
    },
  },
})
