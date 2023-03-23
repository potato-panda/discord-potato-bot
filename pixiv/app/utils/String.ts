// rome-ignore lint/suspicious/noExplicitAny: <explanation>
export function safelyStringify<T = any>(o: T): string {
  try {
    return JSON.stringify(o);
  } catch (err) {
    return '';
  }
}