export default {
  regist(data) {
    return _axios({
      url: `/guestbook`,
      method: "post",
      data: data,
    });
  },
  select(data) {
    return _axios({
      url: `/guestbook`,
      method: "get",
      params: {
        date: data.date,
      },
    });
  },
  modify(data) {
    return _axios({
      url: `/guestbook`,
      method: "put",
      data: data,
    });
  },
};