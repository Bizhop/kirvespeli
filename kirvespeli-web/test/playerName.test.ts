import { test } from "node:test"
import assert from "node:assert/strict"
import { playerName } from "../src/components/shared/playerName.ts"

test("nickname is preferred", () => {
  assert.equal(playerName({ email: "a@b.fi", nickname: "Ville" }), "Ville")
})

test("email is used without a nickname", () => {
  assert.equal(playerName({ email: "a@b.fi", nickname: null }), "a@b.fi")
  assert.equal(playerName({ email: "a@b.fi", nickname: "" }), "a@b.fi")
})

test("missing person gives an empty name", () => {
  assert.equal(playerName(undefined), "")
})
