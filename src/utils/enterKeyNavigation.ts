import React from 'react';

/**
 * Global Enter-Key Navigation Handler for Admission Form Steps.
 * - Pressing Enter moves focus to the next input/select element in natural order.
 * - On the final field of the section, pressing Enter triggers `onFinalSubmit` to advance to the next page.
 */
export const handleFormEnterKeyDown = (
  e: React.KeyboardEvent<HTMLElement>,
  onFinalSubmit?: () => void
) => {
  if (e.key !== 'Enter') return;

  const target = e.target as HTMLElement;
  // Allow normal Enter key behavior for multiline textareas or explicit buttons
  if (target.tagName === 'TEXTAREA' || target.tagName === 'BUTTON' || target.getAttribute('role') === 'button') {
    return;
  }

  e.preventDefault();

  const form = e.currentTarget;
  const focusables = Array.from(
    form.querySelectorAll<HTMLElement>(
      'input:not([type=hidden]):not([disabled]):not([readonly]), select:not([disabled]), [tabindex="0"]:not([disabled])'
    )
  ).filter((el) => {
    // Exclude hidden or non-visible elements
    return el.offsetWidth > 0 && el.offsetHeight > 0 && getComputedStyle(el).visibility !== 'hidden';
  });

  const currentIndex = focusables.indexOf(target);

  if (currentIndex >= 0 && currentIndex < focusables.length - 1) {
    focusables[currentIndex + 1].focus();
  } else if (currentIndex === focusables.length - 1 || currentIndex === -1) {
    if (onFinalSubmit) {
      onFinalSubmit();
    }
  }
};
