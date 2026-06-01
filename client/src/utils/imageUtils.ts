const IMAGE_URL_PATTERN = /^(?:[a-z][a-z0-9+.-]*:|\/\/)/i;

export const EVENT_IMAGE_FALLBACKS = {
  card: 'https://placehold.co/400x200?text=Event',
  detail: 'https://placehold.co/600x400?text=Event',
  summary: 'https://placehold.co/200x200?text=Event',
  thumbnail: 'https://placehold.co/96x96?text=Event',
} as const;

export const getEventImageSrc = (
  image?: string | null,
  fallback: string = EVENT_IMAGE_FALLBACKS.card
): string => {
  const source = image?.trim();

  if (!source) {
    return fallback;
  }

  if (source.startsWith('/') || IMAGE_URL_PATTERN.test(source)) {
    return source;
  }

  return `/${source.replace(/^\.?\//, '')}`;
};
