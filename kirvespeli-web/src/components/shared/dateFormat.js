const dateFormat = new Intl.DateTimeFormat("fi-FI", {
  year: "numeric",
  month: "2-digit",
  day: "2-digit",
})

export const formatString = dateString => dateFormat.format(new Date(dateString))
