export const toUpper = (value?: string | number | null): string => {
  if (value === undefined || value === null) return '';
  return String(value).toUpperCase();
};
