import React, { useEffect, useRef, useState } from "react"
import { Button, Stack } from "@mui/material"
import RenderGame from "../../components/kirves/RenderGame"
import { logAction } from "../logAction"
import { KirvesGame, ROSTER } from "../data/kirves"
import { threePlayerHand, tricks } from "../data/games"

// Short snapshot sequences for checking card motion. Every step is a complete confirmed
// snapshot from the preview engine, as the backend would send it; motion comes only from
// comparing snapshots.

const props = {
  action: logAction("action"),
  join: logAction("join"),
  showAllCards: logAction("showAllCards"),
}

// the game before each step and after every step
const sequence = (game, steps) => {
  const out = [game.clone()]
  for (const step of steps) {
    step(game)
    out.push(game.clone())
  }
  return out
}

// The three-player hand card by card, then Terhi cuts and Ville deals the next hand.
const playSequence = sequence(threePlayerHand(), [
  ...tricks.map(card => g => g.play(card)),
  g => g.cut("KC"),
  g => g.deal({}, "5S"),
])

// Ville deals and turns the ace of spades, keeps it and discards the seven of clubs.
const extraSequence = sequence(new KirvesGame({ count: 3, dealer: "Ville" }).cut("9C"), [
  g => g.deal({ Ville: ["QD", "7C", "KH", "4H", "JC"] }, "AS"),
  g => g.aceOrTwo(true),
  g => g.discard("Ville", "7C"),
])

// Everyone wants to change hearts; Ville picks spades and his hand reorders.
const trumpSequence = sequence(new KirvesGame({ count: 3, dealer: "Terhi" }).cut("TC"), [
  g => g.deal({ Ville: ["2D", "AH", "JC", "9H", "AS"] }, "5H"),
  g => g.speak("Ville", "CHANGE"),
  g => g.speak("Vesa", "PASS"),
  g => g.speak("Terhi", "PASS"),
  g => g.speakSuit("Ville", "SPADES"),
])

// Ville cuts for Vesa, who deals.
const cutSequence = sequence(new KirvesGame({ count: 3, dealer: "Vesa" }), [
  g => g.cut("KD"),
  g => g.deal({}, "8S"),
])

// After five tricks; "Näytä kortit" reveals the concealed won cards.
const revealState = playSequence[tricks.length]

const Sequence = ({ steps, viewers = ["Ville"], rapid }) => {
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
            <h3>{ROSTER.some(p => p.nickname === viewer) ? viewer : "Sivustakatsoja"}</h3>
          )}
          <RenderGame
            {...props}
            game={steps[step].view(viewer)}
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
    trumpChange: () => <Sequence steps={trumpSequence} />,
    extraCard: () => <Sequence steps={extraSequence} />,
    cutAndDeal: () => <Sequence steps={cutSequence} />,
    revealAndConceal: () => <Sequence steps={[revealState, revealState]} />,
    rapidUpdates: () => <Sequence steps={playSequence} rapid />,
    participantsAndSpectator: () => (
      <Sequence steps={playSequence} viewers={["Ville", "Terhi", "Katsoja"]} />
    ),
    privateExtraCard: () => <Sequence steps={extraSequence} viewers={["Ville", "Vesa"]} />,
  },
}
