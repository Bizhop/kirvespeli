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

import { formatString } from "../shared/dateFormat"
import { GAME_FILTERS, filterGames } from "./gameFilter"

const KirvesGames = ({ games, user, getLog, deleteGame }) => {
  const [filter, setFilter] = useState("all")
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
              <TableCell>Pelaajia</TableCell>
              <TableCell>Voi liittyä</TableCell>
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
                <TableCell>
                  {game.players}/{game.maxPlayers}
                </TableCell>
                <TableCell>{game.canJoin && <CheckIcon />}</TableCell>
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
    </>
  )
}

export default KirvesGames
