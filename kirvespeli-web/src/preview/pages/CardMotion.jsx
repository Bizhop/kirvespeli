import React, { useEffect, useRef, useState } from "react"
import { Button, Stack } from "@mui/material"
import RenderGame from "../../components/kirves/RenderGame"
import { logAction } from "../logAction"

// Short snapshot sequences for checking card motion. Every step is a complete confirmed
// snapshot, as the backend would send it; motion comes only from comparing snapshots.

const VILLE = "ville.piispa@gmail.com"
const VESA = "vesa@example.com"
const TERHI = "terhi@example.com"
const seats = [
  [VILLE, "Ville"],
  [VESA, "Vesa"],
  [TERHI, "Terhi"],
]

const props = {
  action: logAction("action"),
  join: logAction("join"),
  showAllCards: logAction("showAllCards"),
}

// Public table state plus every player's private hand; `view` turns it into one viewer's
// snapshot (seats rotated to start from the viewer, only the viewer's own cards included).
const table = ({ hands, played = {}, won = {}, extra = {}, actions = {}, ...rest }) => ({
  hands,
  played,
  won,
  extra,
  actions,
  public: {
    id: 900,
    messages: [],
    cardsInDeck: 37,
    turn: VILLE,
    dealer: TERHI,
    canJoin: false,
    canDeclineCut: false,
    trumpCard: "3S",
    trump: "SPADES",
    cutCard: "",
    secondCutCard: "",
    playersTotal: 3,
    firstCardSuit: "",
    scores: seats.map(([email, nickname]) => ({ email, nickname, score: 0 })),
    scoresHistory: [],
    ...rest,
  },
})

const view = (state, viewer) => {
  const players = seats.map(([email, nickname]) => ({
    email,
    nickname,
    cardsInHand: state.hands[email]?.length ?? 0,
    playedCards: state.played[email] ?? [],
    roundsWon: state.won[email] ?? [],
    availableActions: state.actions[email] ?? [],
    extraCard: null,
    declaredPlayer: false,
    folded: false,
    inGame: true,
    speak: null,
  }))
  const start = Math.max(
    0,
    players.findIndex(p => p.email === viewer),
  )
  const rotated = [...players.slice(start), ...players.slice(0, start)]
  return {
    ...state.public,
    players: rotated,
    numOfPlayedRounds: Object.values(state.won).reduce((sum, rounds) => sum + rounds.length, 0),
    myCardsInHand: state.hands[viewer] ?? [],
    myExtraCard: state.extra[viewer] ?? null,
    myAvailableActions: state.actions[viewer] ?? [],
  }
}

const hands = {
  [VILLE]: ["QC", "QH", "4S", "4D", "QS"],
  [VESA]: ["7H", "KD", "9S", "2C", "TD"],
  [TERHI]: ["AH", "8C", "JS", "3D", "6H"],
}
const without = (list, card) => list.filter(c => c !== card)

const playSequence = [
  table({ hands, actions: { [VILLE]: ["PLAY_CARD"] } }),
  table({
    hands: { ...hands, [VILLE]: without(hands[VILLE], "QH") },
    played: { [VILLE]: ["QH"] },
    actions: { [VESA]: ["PLAY_CARD"] },
    turn: VESA,
    firstCardSuit: "HEARTS",
  }),
  table({
    hands: { ...hands, [VILLE]: without(hands[VILLE], "QH"), [VESA]: without(hands[VESA], "7H") },
    played: { [VILLE]: ["QH"], [VESA]: ["7H"] },
    actions: { [TERHI]: ["PLAY_CARD"] },
    turn: TERHI,
    firstCardSuit: "HEARTS",
  }),
  table({
    hands: {
      [VILLE]: without(hands[VILLE], "QH"),
      [VESA]: without(hands[VESA], "7H"),
      [TERHI]: without(hands[TERHI], "AH"),
    },
    played: { [VILLE]: ["QH"], [VESA]: ["7H"], [TERHI]: ["AH"] },
    won: { [TERHI]: [0] },
    actions: { [TERHI]: ["PLAY_CARD"] },
    turn: TERHI,
  }),
  table({
    hands: {
      [VILLE]: without(hands[VILLE], "QH"),
      [VESA]: without(hands[VESA], "7H"),
      [TERHI]: without(without(hands[TERHI], "AH"), "8C"),
    },
    played: { [VILLE]: ["QH"], [VESA]: ["7H"], [TERHI]: ["AH", "8C"] },
    won: { [TERHI]: [0] },
    actions: { [VILLE]: ["PLAY_CARD"] },
    firstCardSuit: "CLUBS",
  }),
  table({
    hands: {
      [VILLE]: without(without(hands[VILLE], "QH"), "QC"),
      [VESA]: without(hands[VESA], "7H"),
      [TERHI]: without(without(hands[TERHI], "AH"), "8C"),
    },
    played: { [VILLE]: ["QH", "QC"], [VESA]: ["7H"], [TERHI]: ["AH", "8C"] },
    won: { [TERHI]: [0] },
    actions: { [VESA]: ["PLAY_CARD"] },
    turn: VESA,
    firstCardSuit: "CLUBS",
  }),
  // the hand ended and a new one was dealt within one action
  table({
    hands: {
      [VILLE]: ["2H", "JD", "AS", "9C", "TS"],
      [VESA]: ["5C", "6C", "7C", "8D", "9D"],
      [TERHI]: ["KS", "KH", "2S", "3C", "4H"],
    },
    actions: { [VILLE]: ["PLAY_CARD"] },
    dealer: VILLE,
    trumpCard: "5S",
    cardsInDeck: 37,
  }),
]

const extraHands = { ...hands, [VILLE]: ["4D", "QS", "QC", "4S", "QH"] }
const handSequence = [
  table({ hands: extraHands, trump: "", trumpCard: "", actions: { [VILLE]: ["SPEAK"] } }),
  table({ hands: extraHands, trump: "HEARTS", trumpCard: "3H" }),
  table({ hands: extraHands, trump: "SPADES", trumpCard: "3S" }),
  table({
    hands: extraHands,
    extra: { [VILLE]: "AS" },
    actions: { [VILLE]: ["ACE_OR_TWO_DECISION"] },
  }),
  table({
    hands: { ...extraHands, [VILLE]: [...extraHands[VILLE], "AS"] },
    actions: { [VILLE]: ["DISCARD"] },
  }),
  table({
    hands: { ...extraHands, [VILLE]: [...without(extraHands[VILLE], "4D"), "AS"] },
    actions: { [VILLE]: ["PLAY_CARD"] },
  }),
]

const cutSequence = [
  table({ hands: {}, trump: "", trumpCard: "", cardsInDeck: 52, actions: { [VILLE]: ["CUT"] } }),
  table({ hands: {}, trump: "", trumpCard: "", cardsInDeck: 52, cutCard: "AS" }),
  table({
    hands: {},
    trump: "",
    trumpCard: "",
    cardsInDeck: 52,
    cutCard: "AS",
    secondCutCard: "2S",
    actions: { [VILLE]: ["DEAL"] },
  }),
  table({ hands, cardsInDeck: 37, trump: "", trumpCard: "3S", actions: { [VILLE]: ["SPEAK"] } }),
]

// After five tricks; "Näytä kortit" reveals the concealed won cards.
const revealState = table({
  hands: {},
  played: {
    [VILLE]: ["QH", "QC", "4S", "4D", "QS"],
    [VESA]: ["7H", "KD", "9S", "2C", "TD"],
    [TERHI]: ["AH", "8C", "JS", "3D", "6H"],
  },
  won: { [VILLE]: [3, 4], [TERHI]: [0, 1, 2] },
})

const Sequence = ({ steps, viewers = [VILLE], rapid }) => {
  const [step, setStep] = useState(0)
  const [cardsVisible, setCardsVisible] = useState(false)
  const timer = useRef()
  useEffect(() => () => clearTimeout(timer.current), [])
  const go = next => {
    setCardsVisible(false)
    setStep(Math.max(0, Math.min(steps.length - 1, next)))
  }
  const twice = () => {
    go(step + 1)
    timer.current = setTimeout(() => setStep(s => Math.min(steps.length - 1, s + 1)), 120)
  }
  return (
    <>
      <Stack direction="row" spacing={1} sx={{ mb: 1 }} alignItems="center">
        <Button variant="outlined" onClick={() => go(0)}>
          Alusta
        </Button>
        <Button variant="outlined" onClick={() => go(step - 1)} disabled={step === 0}>
          Edellinen
        </Button>
        <Button
          variant="contained"
          onClick={() => go(step + 1)}
          disabled={step === steps.length - 1}
        >
          Seuraava
        </Button>
        {rapid && (
          <Button variant="outlined" onClick={twice} disabled={step >= steps.length - 2}>
            Kaksi päivitystä nopeasti
          </Button>
        )}
        <span>
          {step + 1}/{steps.length}
        </span>
      </Stack>
      {viewers.map(viewer => (
        <div key={viewer}>
          {viewers.length > 1 && (
            <h3>{seats.find(([email]) => email === viewer)?.[1] ?? "Sivustakatsoja"}</h3>
          )}
          <RenderGame
            {...props}
            game={view(steps[step], viewer)}
            cardsVisible={cardsVisible}
            showAllCards={() => setCardsVisible(true)}
          />
        </div>
      ))}
    </>
  )
}

export default {
  title: "CardMotion",
  group: "Containers",
  variants: {
    plays: () => <Sequence steps={playSequence} />,
    trumpAndExtraCard: () => <Sequence steps={handSequence} />,
    cutAndDeal: () => <Sequence steps={cutSequence} />,
    revealAndConceal: () => <Sequence steps={[revealState, revealState]} />,
    rapidUpdates: () => <Sequence steps={playSequence} rapid />,
    participantsAndSpectator: () => (
      <Sequence steps={playSequence} viewers={[VILLE, TERHI, "spectator@example.com"]} />
    ),
    privateExtraCard: () => <Sequence steps={handSequence} viewers={[VILLE, VESA]} />,
  },
}
