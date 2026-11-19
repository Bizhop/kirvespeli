// Players and users are shown by nickname, or by email when no nickname is set.
export const playerName = (
  person: { nickname?: string | null; email?: string } | null | undefined,
) => person?.nickname || person?.email || ""
