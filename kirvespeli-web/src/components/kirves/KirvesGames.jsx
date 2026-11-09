import React from "react"
import { NavLink } from "react-router-dom"
import {
  TableContainer,
  Paper,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  IconButton,
  Tooltip,
} from "@mui/material"
import CheckIcon from "@mui/icons-material/Check"
import ListIcon from "@mui/icons-material/List"
import DeleteForeverIcon from "@mui/icons-material/DeleteForever"

import { formatString } from "../shared/dateFormat"

const KirvesGames = ({ games, user, getLog, deleteGame }) => (
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
        {games.map(game => (
          <TableRow key={`game-${game.id}`}>
            <TableCell>
              <NavLink to={`/kirves/${game.id}`}>{game.id}</NavLink>
            </TableCell>
            <TableCell>{formatString(game.createdAt)}</TableCell>
            <TableCell>{game.players}</TableCell>
            <TableCell>{game.canJoin && <CheckIcon />}</TableCell>
            <TableCell>
              {user.email === game.admin.email && (
                <Tooltip title="Avaa loki">
                  <IconButton onClick={() => getLog({ gameId: game.id, handId: game.lastHandId })}>
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
)

export default KirvesGames
