export const loginPayload = props => ({
  request: {
    url: "api/auth",
    headers: {
      Authorization: props.tokenId,
    },
  },
})

export const getPayload = props => ({
  request: {
    url: props.url,
    method: "get",
    headers: {
      Authorization: localStorage.getItem("kirves-token"),
    },
  },
})

export const postPayload = props => ({
  request: {
    url: props.url,
    method: "post",
    data: props.data,
    headers: {
      Authorization: localStorage.getItem("kirves-token"),
    },
  },
})

export const putPayload = props => ({
  request: {
    url: props.url,
    method: "put",
    data: props.data,
    headers: {
      Authorization: localStorage.getItem("kirves-token"),
    },
  },
})

export const deletePayload = props => ({
  request: {
    url: props.url,
    method: "delete",
    headers: {
      Authorization: localStorage.getItem("kirves-token"),
    },
  },
})
