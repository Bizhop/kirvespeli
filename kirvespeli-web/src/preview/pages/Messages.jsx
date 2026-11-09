import React from "react"

import Messages from "../../components/kirves/Messages"

const data2 = ["Hello", "Whatsapp?"]

const data5 = ["Hello", "Whatsapp?", "None", "Watchin' the game, havin' a bud", "True. True."]

export default {
  title: "Messages",
  group: "Components",
  variants: {
    two: () => <Messages messages={data2} />,
    five: () => <Messages messages={data5} />,
  },
}
