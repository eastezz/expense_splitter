import './LoadingCard.css'

type Props = {
  /** What is loading, for example "Loading groups…". Read by screen readers, not shown on screen. */
  label: string
  /** How many grey placeholder rows to show. */
  rows?: number
}

/**
 * A grey placeholder card with softly shimmering rows, shown while data is loading.
 * It has the same shape as the real lists, so the page does not jump when the data arrives.
 */
function LoadingCard({ label, rows = 3 }: Props) {
  return (
    // role="status" tells screen readers that this part of the page shows progress.
    <div className="loading-card" role="status">
      <span className="visually-hidden">{label}</span>
      {Array.from({ length: rows }, (_, index) => (
        <div key={index} className="loading-row" />
      ))}
    </div>
  )
}

export default LoadingCard