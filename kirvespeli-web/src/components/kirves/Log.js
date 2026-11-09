import React from "react"
import {
  Box,
  TableContainer,
  Table,
  Paper,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  IconButton,
  Icon,
} from "@mui/material"
import CheckIcon from "@mui/icons-material/Check"
import PreviewIcon from "@mui/icons-material/Preview"

import translate from "../shared/translate"

const Log = ({ logId, logItems, selectedLogIndex, getReplay, restoreGame }) => (
  <Box>
    <h2>
      Loki (peli: {logId.gameId} käsi: {logId.handId})
    </h2>
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Pelaaja</TableCell>
            <TableCell>Toiminto</TableCell>
            <TableCell>Lisätieto</TableCell>
            <TableCell>Näytä tilanne</TableCell>
            <TableCell>Palauta tilanne</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {logItems.map((item, index) => (
            <TableRow key={`log-item-${index}`}>
              <TableCell>{item.user.nickname}</TableCell>
              <TableCell>{translate(item.input.action)}</TableCell>
              <TableCell>
                {(() => {
                  switch (item.input.action) {
                    case "PLAY_CARD":
                    case "DISCARD":
                      return `index=${item.input.index}`
                    case "ACE_OR_TWO_DECISION":
                      return item.input.keepExtraCard ? "Pidetty" : "Hylätty"
                    case "SPEAK":
                      return translate(item.input.speak)
                    case "SPEAK_SUIT":
                      return translate(item.input.suit)
                    default:
                      return ""
                  }
                })()}
              </TableCell>
              <TableCell>
                <IconButton
                  onClick={() =>
                    getReplay({
                      gameId: logId.gameId,
                      handId: logId.handId,
                      index,
                    })
                  }
                >
                  <PreviewIcon />
                </IconButton>
              </TableCell>
              <TableCell>
                {selectedLogIndex == index && (
                  <IconButton
                    onClick={() =>
                      restoreGame({
                        gameId: logId.gameId,
                        handId: logId.handId,
                        index,
                      })
                    }
                  >
                    <CheckIcon />
                  </IconButton>
                )}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  </Box>
)

export default Log
