import React from "react"

// Hand-drawn tally mark for the score sheet. The same seed always draws the same strokes.
//   score 0: empty, 1: circle, 2: + vertical stroke, 3: + horizontal stroke
//   loss "stick" (first loss of a match) or "axe" (second loss) is drawn over a faded tally.
// Strokes use currentColor; the loss symbol can be recolored with --score-loss-color.

const hash = text => {
  let h = 2166136261
  for (let i = 0; i < text.length; i++) {
    h ^= text.charCodeAt(i)
    h = Math.imul(h, 16777619)
  }
  return h >>> 0
}

// mulberry32
const random = seed => {
  let a = hash(String(seed))
  return () => {
    a = (a + 0x6d2b79f5) | 0
    let t = Math.imul(a ^ (a >>> 15), 1 | a)
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
}

const f = n => n.toFixed(1)

// rnd in [-amount, amount]
const jitter = (rand, amount) => (rand() * 2 - 1) * amount

const circlePath = rand => {
  const cx = 20 + jitter(rand, 0.6)
  const cy = 20 + jitter(rand, 0.6)
  const start = rand() * Math.PI * 2
  // a pen circle rarely closes exactly: overshoot a little past the start
  const sweep = Math.PI * 2 + 0.25 + rand() * 0.35
  const steps = 14
  const points = []
  for (let i = 0; i <= steps; i++) {
    const angle = start + (sweep * i) / steps
    const r = 10.5 + jitter(rand, 0.7) + (i / steps) * 0.8
    points.push([cx + Math.cos(angle) * r, cy + Math.sin(angle) * r])
  }
  // Catmull-Rom through the points as cubic Béziers
  let d = `M${f(points[0][0])},${f(points[0][1])}`
  for (let i = 0; i < points.length - 1; i++) {
    const p0 = points[Math.max(i - 1, 0)]
    const p1 = points[i]
    const p2 = points[i + 1]
    const p3 = points[Math.min(i + 2, points.length - 1)]
    const c1 = [p1[0] + (p2[0] - p0[0]) / 6, p1[1] + (p2[1] - p0[1]) / 6]
    const c2 = [p2[0] - (p3[0] - p1[0]) / 6, p2[1] - (p3[1] - p1[1]) / 6]
    d += ` C${f(c1[0])},${f(c1[1])} ${f(c2[0])},${f(c2[1])} ${f(p2[0])},${f(p2[1])}`
  }
  return d
}

// slightly bowed line from (x1, y1) to (x2, y2)
const linePath = (rand, x1, y1, x2, y2) => {
  const sx = x1 + jitter(rand, 0.8)
  const sy = y1 + jitter(rand, 0.8)
  const ex = x2 + jitter(rand, 0.8)
  const ey = y2 + jitter(rand, 0.8)
  const bow = jitter(rand, 1.2)
  const mx = (sx + ex) / 2 + ((ey - sy) / 30) * bow
  const my = (sy + ey) / 2 - ((ex - sx) / 30) * bow
  return `M${f(sx)},${f(sy)} Q${f(mx)},${f(my)} ${f(ex)},${f(ey)}`
}

const tallyPaths = (rand, score) => {
  const paths = []
  if (score >= 1) paths.push(circlePath(rand))
  // the circle spans about 9..31: strokes reach a bit past its edge
  if (score >= 2) paths.push(linePath(rand, 20, 5, 20, 35))
  if (score >= 3) paths.push(linePath(rand, 5, 20, 35, 20))
  return paths
}

// drawn upright around (20, 20), then tilted: the handle, and for the axe a filled blade
// with a curved cutting edge
const lossPaths = (rand, loss) => {
  const handle = { d: linePath(rand, 20, 37, 20, 3), fill: false }
  if (loss !== "axe") return [handle]
  const j = () => jitter(rand, 0.4)
  const blade =
    `M${f(19 + j())},${f(7 + j())} L${f(26 + j())},${f(7 + j())}` +
    ` Q${f(30 + j())},${f(5 + j())} ${f(33 + j())},${f(1 + j())}` +
    ` Q${f(38 + j())},${f(11 + j())} ${f(33 + j())},${f(21 + j())}` +
    ` Q${f(30 + j())},${f(16 + j())} ${f(26 + j())},${f(15 + j())}` +
    ` L${f(19 + j())},${f(15 + j())} Z`
  return [handle, { d: blade, fill: true }]
}

const label = (score, loss) =>
  [score > 0 ? `${score} p` : null, loss === "axe" ? "kirves" : loss === "stick" ? "keppi" : null]
    .filter(Boolean)
    .join(", ") || "0 p"

const ScoreMark = ({ score = 0, loss = null, seed = "", size = 32 }) => {
  const rand = random(`${seed}:${score}:${loss}`)
  const tally = tallyPaths(rand, score)
  const lossStrokes = loss ? lossPaths(rand, loss) : []
  const tilt = 28 + jitter(rand, 6)
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 40 40"
      role="img"
      aria-label={label(score, loss)}
      style={{ display: "block", overflow: "visible" }}
    >
      <title>{label(score, loss)}</title>
      <g
        fill="none"
        stroke="currentColor"
        strokeWidth="2.4"
        strokeLinecap="round"
        strokeLinejoin="round"
        opacity={loss ? 0.4 : 1}
      >
        {tally.map((d, i) => (
          <path key={i} d={d} />
        ))}
      </g>
      {loss && (
        <g
          strokeWidth="3"
          strokeLinecap="round"
          strokeLinejoin="round"
          transform={`rotate(${f(tilt)} 20 20)`}
          style={{ color: "var(--score-loss-color, currentColor)" }}
        >
          {lossStrokes.map(({ d, fill }, i) => (
            <path key={i} d={d} stroke="currentColor" fill={fill ? "currentColor" : "none"} />
          ))}
        </g>
      )}
    </svg>
  )
}

export default ScoreMark
