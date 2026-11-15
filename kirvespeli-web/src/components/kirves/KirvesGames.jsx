import React, { useState } from "react"
import { NavLink } from "react-router"
import {
  TableContainer,
  Paper,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  IconButton,
  Button,
  Tooltip,
  ToggleButton,
  ToggleButtonGroup,
} from "@mui/material"
import CheckIcon from "@mui/icons-material/Check"
import ListIcon from "@mui/icons-material/List"
import DeleteForeverIcon from "@mui/icons-material/DeleteForever"
import VisibilityIcon from "@mui/icons-material/Visibility"

import { formatString } from "../shared/dateFormat"
import { playerName } from "../shared/playerName"
import { GAME_FILTERS, filterGames } from "./gameFilter"
import GamePeek from "./GamePeek"

const KirvesGames = ({ games, user, getLog, deleteGame, fetchGame }) => {
  const [filter, setFilter] = useState("all")
  const [peeked, setPeeked] = useState(null)
  const visibleGames = filterGames(games, filter)
  return (
    <>
      <ToggleButtonGroup
        exclusive
        size="small"
        value={filter}
        onChange={(_, value) => value && setFilter(value)}
        aria-label="Suodata pelit"
        sx={{ my: 1 }}
      >
        {GAME_FILTERS.map(f => (
          <ToggleButton key={f.value} value={f.value}>
            {f.label}
          </ToggleButton>
        ))}
      </ToggleButtonGroup>
      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Peli</TableCell>
              <TableCell>Pvm</TableCell>
              <TableCell>Aloittaja</TableCell>
              <TableCell>Pelaajia</TableCell>
              <TableCell>Voi liittyä</TableCell>
              <TableCell />
              <TableCell />
              <TableCell />
            </TableRow>
          </TableHead>
          <TableBody>
            {visibleGames.map(game => (
              <TableRow key={`game-${game.id}`}>
                <TableCell>
                  <Button
                    component={NavLink}
                    to={`/kirves/${game.id}`}
                    variant="contained"
                    size="small"
                    aria-label={`Avaa peli ${game.id}`}
                    sx={{ minWidth: 44 }}
                  >
                    {game.id}
                  </Button>
                </TableCell>
                <TableCell>{formatString(game.createdAt)}</TableCell>
                <TableCell>{playerName(game.admin)}</TableCell>
                <TableCell>
                  {game.players}/{game.maxPlayers}
                </TableCell>
                <TableCell>{game.canJoin && <CheckIcon />}</TableCell>
                <TableCell>
                  {fetchGame && (
                    <Tooltip title="Vilkaise peliä">
                      <IconButton
                        onClick={() => setPeeked(game)}
                        aria-label={`Vilkaise peliä ${game.id}`}
                      >
                        <VisibilityIcon />
                      </IconButton>
                    </Tooltip>
                  )}
                </TableCell>
                <TableCell>
                  {user.email === game.admin.email && (
                    <Tooltip title="Avaa loki">
                      <IconButton
                        onClick={() => getLog({ gameId: game.id, handId: game.lastHandId })}
                      >
                        <ListIcon />
                      </IconButton>
                    </Tooltip>
                  )}
                </TableCell>
                <TableCell>
                  {user.email === game.admin.email && (
                    <Tooltip title="Poista peli">
                      <IconButton onClick={() => deleteGame(game.id)}>
                        <DeleteForeverIcon />
                      </IconButton>
                    </Tooltip>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
      {peeked && <GamePeek brief={peeked} fetchGame={fetchGame} onClose={() => setPeeked(null)} />}
    </>
  )
}

export default KirvesGames
