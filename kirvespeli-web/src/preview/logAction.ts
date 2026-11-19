// Callback prop for previews: logs the call and shows it in the preview's action bar
export const logAction =
  (name: string) =>
  async (...args: unknown[]): Promise<void> => {
    console.log(`[preview] ${name}`, ...args)
    window.dispatchEvent(new CustomEvent("preview-action", { detail: { name, args } }))
  }
