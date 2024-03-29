export function safeStringify<T = any>(o: T): string {
  try {
    return JSON.stringify(o);
  } catch (err) {
    return '';
  }
}
