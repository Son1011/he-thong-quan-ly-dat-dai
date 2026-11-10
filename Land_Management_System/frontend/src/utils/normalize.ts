export function normalizeViText(s: string) {
  return (s ?? "")
    .trim()
    .toLowerCase()
    .normalize("NFKD")
    .replace(/[\u0300-\u036f]/g, "")
    // remove administrative prefixes uniformly
    .replace(/\b(tinh|thanh pho|tp\.?)\b/g, "")
    .replace(/[()]/g, "")
    .replace(/[·•,]/g, " ")
    .replace(/\s+/g, " ")
    .trim();
}

