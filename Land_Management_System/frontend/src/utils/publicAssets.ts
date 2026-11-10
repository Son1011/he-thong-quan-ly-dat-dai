/**
 * Build a safe URL for assets served from Vite `public/`.
 *
 * Why: filenames with Vietnamese characters / spaces can fail on some servers
 * if the browser sends an un-encoded URL. `encodeURI` ensures the request is
 * percent-encoded (e.g. spaces -> %20).
 */
export function publicAssetUrl(pathFromPublicRoot: string) {
  const p = pathFromPublicRoot.startsWith("/") ? pathFromPublicRoot : `/${pathFromPublicRoot}`;
  return encodeURI(p);
}

export function publicImageUrl(fileName: string) {
  return publicAssetUrl(`/images/${fileName}`);
}


