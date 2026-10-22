export function isPhone(value: string) {
  return value.trim().length >= 7;
}

export function isPassword(value: string) {
  return value.length >= 8;
}
