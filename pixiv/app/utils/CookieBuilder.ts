import { Cookie } from 'playwright';

export function objectToCookie(o: Cookie): string {
  if (!o) return '';
  return[
    o.name ? `${o.name}=${o.value};` : null,
    o.expires ? `Expires=${o.expires};` : null,
    o.sameSite ? `SameSite=${o.sameSite};` : null,
    o.httpOnly ? 'HttpOnly;' : null,
    o.secure ? 'Secure;' : null,
  ]
    .filter((s) => s)
    .join(' ');
}
