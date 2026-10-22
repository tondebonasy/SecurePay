export function log(...args: any[]) {
  if (__DEV__) {
    // eslint-disable-next-line no-console
    console.log(...args);
  }
}
