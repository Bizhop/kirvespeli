// A small Kirves engine for preview fixtures. It mirrors the backend rules (Game.java,
// Player.java) closely enough that fixtures built by playing actions are snapshots the
// backend could send: card counts, deck size, trick winners, turns, actions, scores and
// messages follow from the actions instead of being written by hand.
//
// Illegal actions throw, so a fixture that breaks the rules fails when the preview data loads
// (and in `npm test`).

export const DECK_SIZE = 54
const HAND_SIZE = 5
const SUITS = { C: "CLUBS", S: "SPADES", H: "HEARTS", D: "DIAMONDS" }
const RANKS = { 2: 2, 3: 3, 4: 4, 5: 5, 6: 6, 7: 7, 8: 8, 9: 9, T: 10, Q: 12, K: 13, A: 14 }
const JACKS = { D: 15, H: 16, S: 17, C: 18 }

export const ROSTER = ["Ville", "Vesa", "Terhi", "Kalle", "Sanna", "Matti"].map((nickname, i) => ({
  email: i === 0 ? "ville.piispa@gmail.com" : `${nickname.toLowerCase()}@example.com`,
  nickname,
}))
export const emailOf = nickname => ROSTER.find(p => p.nickname === nickname).email

// every card, in a fixed scrambled order for filling hands that a fixture does not specify
export const ALL_CARDS = (() => {
  const cards = ["RJ", "BJ"]
  for (const suit of "CSHD") for (const rank of "23456789TJQKA") cards.push(rank + suit)
  let seed = 7
  for (let i = cards.length - 1; i > 0; i--) {
    seed = (seed * 1103515245 + 12345) % 2147483648
    const j = seed % (i + 1)
    ;[cards[i], cards[j]] = [cards[j], cards[i]]
  }
  return cards
})()

const isJoker = card => card === "RJ" || card === "BJ"
const isJack = card => card[0] === "J" && !isJoker(card)
const rankOf = card =>
  card === "RJ" ? 20 : card === "BJ" ? 19 : isJack(card) ? JACKS[card[1]] : RANKS[card[0]]
// jacks and jokers belong to the trump suit
export const suitOf = (card, trump) => (isJoker(card) || isJack(card) ? trump : SUITS[card[1]])
const isTrump = (card, trump) => suitOf(card, trump) === trump

// Index of the winning card among cards in play order (backend Game.winningCard).
export const winningIndex = (cards, trump) => {
  let leader = 0
  for (let i = 1; i < cards.length; i++) {
    const leaderSuit = suitOf(cards[leader], trump)
    const suit = suitOf(cards[i], trump)
    if (
      (suit === trump && leaderSuit !== trump) ||
      (suit === leaderSuit && rankOf(cards[i]) > rankOf(cards[leader]))
    )
      leader = i
  }
  return leader
}

const fail = message => {
  throw new Error(`Kirves preview fixture: ${message}`)
}

export class KirvesGame {
  constructor({ count, dealer = "Ville", admin = "Ville", scores, history = [], id = 12 }) {
    this.id = id
    this.admin = admin
    this.players = ROSTER.slice(0, count).map(({ email, nickname }) => ({
      email,
      nickname,
      hand: [],
      hidden: [],
      played: [],
      won: [],
      actions: [],
      speak: null,
      declared: false,
      folded: false,
      inGame: true,
      departurePending: false,
      extra: null,
    }))
    this.scores = this.players.map((p, i) => ({
      email: p.email,
      nickname: p.nickname,
      score: scores?.[i] ?? 0,
    }))
    // players with three points sit out until the game ends
    this.scores.forEach((s, i) => {
      if (s.score === 3) this.players[i].inGame = false
    })
    this.history = []
    for (const [loser, points] of history) this.recordLoss(this.player(loser), points)
    this.messages = []
    this.deck = DECK_SIZE
    this.trump = null
    this.trumpCard = null
    this.cutCard = null
    this.secondCutCard = null
    this.canDeclineCut = false
    this.speaking = false
    this.forced = false
    this.departureSecondsLeft = null
    // action log of the current hand (from DEAL) and the admin's view after each item
    this.log = []
    this.replays = []
    this.setDealer(this.player(dealer))
  }

  // the backend logs DEAL and the hand's actions with the acting user
  record(player, input) {
    this.log.push({
      user: { email: player.email, jwt: null, nickname: player.nickname },
      input: {
        action: input.action,
        index: input.index ?? 0,
        keepExtraCard: input.keepExtraCard ?? false,
        suit: input.suit ?? null,
        declineCut: false,
        speak: input.speak ?? null,
      },
    })
    this.replays.push(this.view(this.admin))
  }

  player(nickname) {
    return this.players.find(p => p.nickname === nickname) ?? fail(`no player ${nickname}`)
  }
  seat(player) {
    return this.players.indexOf(player)
  }
  // next and previous player still in the game, as Player.getNext/getPrevious
  next(player) {
    for (let i = 1; i <= this.players.length; i++) {
      const candidate = this.players[(this.seat(player) + i) % this.players.length]
      if (candidate.inGame) return candidate
    }
    return fail("nobody in game")
  }
  previous(player) {
    const n = this.players.length
    for (let i = 1; i <= n; i++) {
      const candidate = this.players[(this.seat(player) - i + n) % n]
      if (candidate.inGame) return candidate
    }
    return fail("nobody in game")
  }
  // in-game players starting from `player` (Game.getPlayersStartingFrom)
  from(player) {
    const start = player.inGame ? player : this.next(player)
    const list = [start]
    for (let p = this.next(start); p !== start; p = this.next(p)) list.push(p)
    return list
  }
  resetActions() {
    this.players.forEach(p => (p.actions = []))
  }
  require(nickname, action) {
    const player = this.player(nickname)
    if (!player.actions.includes(action)) fail(`${nickname} cannot ${action}`)
    return player
  }

  recordLoss(loser, points) {
    let losses = 0
    for (const entry of [...this.history].reverse()) {
      if (entry.axe) break
      if (entry.loser === loser.email) losses++
    }
    const scores = this.players.map(p => ({
      email: p.email,
      nickname: p.nickname,
      score: p === loser ? points : 3,
    }))
    const axe = losses === 1
    this.history.push({ scores, loser: loser.email, axe })
    return axe
  }

  setDealer(dealer) {
    this.from(dealer).forEach(p => (p.hand = []))
    this.dealer = dealer
    this.trumpCard = null
    this.trump = null
    this.speaking = false
    this.forced = false
    this.canDeclineCut = false
    this.resetActions()
    this.turn = this.previous(dealer)
    this.turn.actions = ["CUT"]
  }

  chooseDealer(nickname, dealerName) {
    const chooser = this.require(nickname, "SET_DEALER")
    const dealer = this.player(dealerName)
    this.setDealer(dealer)
    chooser.actions.push("SET_DEALER")
    this.messages.push(`${chooser.nickname} valitsi jakajaksi pelaajan ${dealer.nickname}`)
    return this
  }

  // cut: the deck is shuffled anew and the cut cards leave it
  cut(card, second) {
    const cutter = this.players.find(p => p.actions.includes("CUT")) ?? fail("nobody can cut")
    this.players.forEach(p => (p.actions = p.actions.filter(a => a !== "SET_DEALER")))
    this.deck = DECK_SIZE
    this.cutCard = card
    this.secondCutCard = null
    this.deck--
    if (isJack(card) || isJoker(card)) {
      second ?? fail("a jack or joker cut needs a second card")
      this.secondCutCard = second
      this.deck--
      if (isJack(second) || isJoker(second)) {
        this.messages.push(`Uusi nosto, ${cutter.nickname} voi kieltäytyä nostamasta`)
        this.canDeclineCut = true
        return this
      }
      cutter.extra = card
      this.forced = true
    }
    this.players.forEach(p => {
      p.declared = false
      p.actions = []
      p.played = []
      p.speak = null
      p.won = []
    })
    this.dealer.actions = ["DEAL"]
    this.turn = this.dealer
    return this
  }

  // hands: nickname -> five cards; dealt from the dealer onwards, then the trump card is turned.
  // Players without given cards get unused cards in a fixed order.
  deal(hands, trumpCard) {
    this.require(this.dealer.nickname, "DEAL")
    const used = new Set(
      [...Object.values(hands).flat(), trumpCard, this.cutCard, this.secondCutCard].filter(Boolean),
    )
    const spare = ALL_CARDS.filter(card => !used.has(card))
    for (const player of this.from(this.dealer)) {
      const cards = hands[player.nickname] ?? spare.splice(0, HAND_SIZE)
      if (cards.length !== HAND_SIZE) fail(`${player.nickname} needs five cards`)
      player.played = []
      player.hand = [...cards]
    }
    this.deck -= HAND_SIZE * this.from(this.dealer).length + 1
    this.trumpCard = trumpCard
    this.trump = isJoker(trumpCard)
      ? trumpCard === "BJ"
        ? "SPADES"
        : "HEARTS"
      : SUITS[trumpCard[1]]
    const dealer = this.dealer
    if (this.players.some(p => p.extra) || isJoker(trumpCard) || isJack(trumpCard)) {
      // yhteinen tai väkyri: the dealer takes the card and must play
      dealer.extra = trumpCard
      dealer.declared = true
      this.trumpCard = null
      this.forced = true
    } else if (trumpCard[0] === "2" || trumpCard[0] === "A") {
      dealer.hidden = dealer.hand.splice(0, trumpCard[0] === "2" ? 2 : 3)
      dealer.extra = trumpCard
      this.trumpCard = null
    }
    this.cutCard = null
    this.secondCutCard = null
    this.speaking = true
    const first = this.next(dealer)
    this.setCardPlayer(first)
    this.firstOfRound = first
    this.log = []
    this.replays = []
    this.record(dealer, { action: "DEAL" })
    return this
  }

  setCardPlayer(start) {
    this.resetActions()
    const discarder = this.from(start).find(p => p.extra)
    if (this.dealer.hidden.length) {
      this.turn = this.dealer
      this.turn.actions = ["ACE_OR_TWO_DECISION"]
    } else if (discarder) {
      this.turn = discarder
      discarder.actions = ["DISCARD"]
    } else {
      const player = start.inGame ? start : this.next(start)
      this.turn = player
      if (this.speaking && !this.forced) player.actions = ["SPEAK"]
      else {
        player.actions = ["PLAY_CARD"]
        const active = this.from(player).length
        for (const p of this.from(player)) if (this.canFold(p, active)) p.actions.push("FOLD")
      }
    }
  }

  canFold(player, active) {
    if (active < 2 || !this.trump) return false
    if (this.firstOfRound !== this.turn) return false
    if (player.hand.length === HAND_SIZE) return player !== this.firstOfRound
    return player.hand.every(card => !isTrump(card, this.trump))
  }

  aceOrTwo(keep) {
    const dealer = this.require(this.dealer.nickname, "ACE_OR_TWO_DECISION")
    if (keep) this.speaking = false
    else {
      this.trumpCard = dealer.extra
      dealer.extra = null
    }
    dealer.hand.push(...dealer.hidden)
    dealer.hidden = []
    this.setCardPlayer(this.next(dealer))
    this.record(dealer, { action: "ACE_OR_TWO_DECISION", keepExtraCard: keep })
    return this
  }

  discard(nickname, card) {
    const player = this.require(nickname, "DISCARD")
    const index = player.hand.indexOf(card)
    if (index < 0) fail(`${nickname} has no ${card}`)
    player.hand = [...player.hand.filter(c => c !== card), player.extra]
    player.extra = null
    player.declared = true
    this.setCardPlayer(this.next(this.dealer))
    this.record(player, { action: "DISCARD", index })
    return this
  }

  speak(nickname, speak) {
    const player = this.require(nickname, "SPEAK")
    player.speak = speak
    if (speak === "KEEP") {
      player.declared = true
      this.speaking = false
      this.setCardPlayer(this.firstOfRound)
      this.record(player, { action: "SPEAK", speak })
      return this
    }
    const next = this.next(player)
    if (next !== this.firstOfRound) this.setCardPlayer(next)
    else {
      const changer = this.from(this.firstOfRound).find(p => p.speak === "CHANGE")
      if (changer) {
        this.resetActions()
        changer.actions = ["SPEAK_SUIT"]
        this.turn = changer
      } else this.continueAfterHand()
    }
    this.record(player, { action: "SPEAK", speak })
    return this
  }

  speakSuit(nickname, suit) {
    const player = this.require(nickname, "SPEAK_SUIT")
    if (suit === this.trump) fail("the new trump must differ")
    player.declared = true
    this.trump = suit
    this.trumpCard = null
    this.speaking = false
    this.setCardPlayer(this.firstOfRound)
    this.record(player, { action: "SPEAK_SUIT", suit })
    return this
  }

  // the player in turn plays `card`
  play(card) {
    const player = this.turn
    if (!player.actions.includes("PLAY_CARD")) fail(`${player.nickname} cannot play`)
    const index = player.hand.indexOf(card)
    if (index < 0) fail(`${player.nickname} has no ${card}`)
    player.hand = player.hand.filter(c => c !== card)
    player.played.push(card)
    this.setCardPlayer(this.next(player))
    this.roundWinner(true)
    this.record(player, { action: "PLAY_CARD", index })
    return this
  }

  // A plain strategy for filler play: lead the strongest card, follow suit with the highest
  // card, otherwise the lowest trump, otherwise the lowest card.
  choose(player) {
    const strength = card => (isTrump(card, this.trump) ? 100 : 0) + rankOf(card)
    const byStrength = [...player.hand].sort((a, b) => strength(a) - strength(b))
    if (this.firstOfRound === player) return byStrength.at(-1)
    const led = suitOf(this.firstOfRound.played.at(-1), this.trump)
    const follow = byStrength.filter(card => suitOf(card, this.trump) === led)
    if (follow.length) return follow.at(-1)
    return byStrength.find(card => isTrump(card, this.trump)) ?? byStrength[0]
  }

  // play `count` cards (default: until the hand ends) with the filler strategy
  playOn(count = Infinity) {
    while (count-- > 0 && this.turn.actions.includes("PLAY_CARD")) this.play(this.choose(this.turn))
    return this
  }

  // several plays in turn order
  plays(...cards) {
    cards.forEach(card => this.play(card))
    return this
  }

  fold(nickname) {
    const player = this.require(nickname, "FOLD")
    const roundInProgress = this.turn !== this.firstOfRound
    const wasTurn = this.turn === player
    player.hand = []
    player.extra = null
    player.hidden = []
    player.played = []
    player.inGame = false
    player.folded = true
    player.actions = []
    if (this.firstOfRound === player) this.firstOfRound = this.next(player)
    if (wasTurn) this.setCardPlayer(this.next(player))
    this.roundWinner(roundInProgress)
    this.messages.push(`${player.nickname} meni pakkaan`)
    this.record(player, { action: "FOLD" })
    return this
  }

  roundWinner(roundInProgress) {
    const players = this.from(this.firstOfRound)
    if (players.length === 1) return this.scoring(new Set(players))
    if (!roundInProgress || this.turn !== this.firstOfRound) return
    const cards = players.map(p => p.played.at(-1))
    const winner = players[winningIndex(cards, this.trump)]
    winner.won.push(winner.played.length - 1)
    if (winner.hand.length) {
      this.setCardPlayer(winner)
      this.firstOfRound = winner
    } else {
      const handWinner = this.handWinner(players)
      const winners = new Set()
      for (const p of players) {
        if (p === handWinner) winners.add(p)
        else if (!p.declared && players.some(o => o !== p && o.declared && o !== handWinner))
          winners.add(p)
      }
      this.scoring(winners)
    }
  }

  handWinner(players) {
    const three = players.find(p => p.won.length >= 3)
    if (three) return three
    const two = players.filter(p => p.won.length === 2)
    if (two.length === 1) return two[0]
    if (two.length === 2) return two[0].won[1] > two[1].won[1] ? two[1] : two[0]
    return players.find(p => p.won[0] === 4) ?? fail("no hand winner")
  }

  scoring(winners) {
    const list = this.players.filter(p => winners.has(p))
    this.messages.push(
      `${list.length === 1 ? "Voittaja" : "Voittajat"}: ${list.map(p => p.nickname).join(",")}`,
    )
    const departing = []
    for (const winner of list) {
      const score = this.scores[this.seat(winner)]
      score.score++
      if (score.score === 3) departing.push(winner)
    }
    if (!departing.length) return this.continueAfterHand()
    this.resetActions()
    for (const p of departing) {
      p.inGame = false
      p.departurePending = true
      p.actions = ["ACKNOWLEDGE_DEPARTURE"]
    }
    this.departureSecondsLeft = 60
    this.updateContinue()
  }

  updateContinue() {
    this.players.forEach(p => (p.actions = p.actions.filter(a => a !== "CONTINUE_GAME")))
    const admin = this.player(this.admin)
    if (!admin.departurePending && this.players.some(p => p.departurePending))
      admin.actions.push("CONTINUE_GAME")
  }

  acknowledge(nickname) {
    const player = this.require(nickname, "ACKNOWLEDGE_DEPARTURE")
    player.departurePending = false
    player.actions = player.actions.filter(a => a !== "ACKNOWLEDGE_DEPARTURE")
    if (this.players.some(p => p.departurePending)) this.updateContinue()
    else this.finishDepartures()
    return this
  }

  continueGame() {
    const admin = this.require(this.admin, "CONTINUE_GAME")
    this.messages.push(`${admin.nickname} jatkoi peliä`)
    this.players.forEach(p => {
      if (p.departurePending) p.departurePending = false
      p.actions = p.actions.filter(a => a !== "ACKNOWLEDGE_DEPARTURE")
    })
    this.finishDepartures()
    return this
  }

  finishDepartures() {
    this.departureSecondsLeft = null
    for (const p of this.players)
      if (!p.inGame && !p.folded) {
        p.hand = []
        p.extra = null
        p.hidden = []
      }
    this.continueAfterHand()
  }

  continueAfterHand() {
    this.players.filter(p => p.folded).forEach(p => ((p.inGame = true), (p.folded = false)))
    this.setDealer(this.next(this.dealer))
    if (this.players.filter(p => p.inGame).length < 2) this.endGame()
  }

  endGame() {
    const loser = this.players.find(p => p.inGame)
    const points = this.scores[this.seat(loser)].score
    const axe = this.recordLoss(loser, points)
    this.history.at(-1).scores = this.scores.map(s => ({ ...s }))
    this.scores.forEach(s => (s.score = 0))
    this.players.forEach(p => ((p.inGame = true), (p.folded = false)))
    this.messages.push(`Hävisi: ${loser.nickname}`)
    if (axe) this.messages.push(`${loser.nickname} sai kirveen`)
    this.setDealer(loser)
    loser.actions.push("SET_DEALER")
  }

  firstCardSuit() {
    if (!this.trump || !this.firstOfRound || this.firstOfRound === this.turn) return ""
    const card = this.firstOfRound.played.at(-1)
    return card ? suitOf(card, this.trump) : ""
  }

  // The snapshot sent to `viewer` (a nickname, or anyone else for a spectator): seats start
  // from the viewer, only the viewer's own cards are included.
  view(viewer) {
    const me = this.players.find(p => p.nickname === viewer)
    const start = me ? this.seat(me) : 0
    const seats = [...this.players.slice(start), ...this.players.slice(0, start)]
    return {
      id: this.id,
      players: seats.map(p => ({
        email: p.email,
        nickname: p.nickname,
        cardsInHand: p.hand.length,
        playedCards: [...p.played],
        roundsWon: [...p.won],
        availableActions: [...p.actions],
        extraCard: p.extra,
        declaredPlayer: p.declared,
        folded: p.folded,
        inGame: p.inGame,
        departurePending: p.departurePending,
        speak: p.speak,
      })),
      messages: [...this.messages],
      cardsInDeck: this.deck,
      turn: this.turn.email,
      dealer: this.dealer.email,
      myCardsInHand: me ? [...me.hand] : [],
      myExtraCard: me?.extra ?? null,
      myAvailableActions: me ? [...me.actions] : [],
      canJoin: false,
      canDeclineCut: this.canDeclineCut,
      trumpCard: this.trumpCard ?? "",
      trump: this.trump ?? "",
      cutCard: this.cutCard ?? "",
      secondCutCard: this.secondCutCard ?? "",
      playersTotal: this.players.length,
      firstCardSuit: this.firstCardSuit(),
      scores: this.scores.map(s => ({ ...s })),
      scoresHistory: this.history.map(h => ({ ...h, scores: h.scores.map(s => ({ ...s })) })),
      departureSecondsLeft: this.departureSecondsLeft,
      numOfPlayedRounds: this.players.reduce((sum, p) => sum + p.won.length, 0),
    }
  }

  // an independent copy, for branching one game into several fixtures
  clone() {
    const copy = Object.create(KirvesGame.prototype)
    Object.assign(copy, structuredClone({ ...this, players: undefined, dealer: undefined }))
    copy.players = this.players.map(p => structuredClone(p))
    const same = p => p && copy.players[this.seat(p)]
    copy.dealer = same(this.dealer)
    copy.turn = same(this.turn)
    copy.firstOfRound = same(this.firstOfRound)
    return copy
  }
}
