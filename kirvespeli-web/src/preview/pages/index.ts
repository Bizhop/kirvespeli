import type { PreviewPage } from "../types.ts"
// Preview pages, in navigation order. Each page: { title, group, variants: { name: () => JSX } }
import ActionButton from "./ActionButton.tsx"
import Card from "./Card.tsx"
import Cards from "./Cards.tsx"
import Hand from "./Hand.tsx"
import KirvesGames from "./KirvesGames.tsx"
import Log from "./Log.tsx"
import Messages from "./Messages.tsx"
import ScoreCard from "./ScoreCard.tsx"
import ScoreSlip from "./ScoreSlip.tsx"
import MessagesButton from "./MessagesButton.tsx"
import ScoreMark from "./ScoreMark.tsx"
import DealerSelector from "./DealerSelector.tsx"
import SuitSelector from "./SuitSelector.tsx"
import UpdateUserForm from "./UpdateUserForm.tsx"
import Dash from "./Dash.tsx"
import Header from "./Header.tsx"
import Kirves from "./Kirves.tsx"
import KirvesGame from "./KirvesGame.tsx"
import RenderGame from "./RenderGame.tsx"

import Table from "./Table.tsx"
import CardMotion from "./CardMotion.tsx"
import Seat from "./Seat.tsx"
import Icon from "./Icon.tsx"

const pages: PreviewPage[] = [
  Table,
  CardMotion,
  Seat,
  Icon,
  ActionButton,
  Card,
  Cards,
  Hand,
  KirvesGames,
  Log,
  Messages,
  MessagesButton,
  ScoreCard,
  ScoreSlip,
  ScoreMark,
  DealerSelector,
  SuitSelector,
  UpdateUserForm,
  Dash,
  Header,
  Kirves,
  KirvesGame,
  RenderGame,
]
export default pages
