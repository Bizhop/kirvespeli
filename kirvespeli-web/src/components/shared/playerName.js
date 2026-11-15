// Players and users are shown by nickname, or by email when no nickname is set.
export const playerName = person => person?.nickname || person?.email || ""
