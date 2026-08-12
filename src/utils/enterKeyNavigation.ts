import React from 'react';

/**
 * Global Enter-Key Navigation Handler for Admission Form Steps.
 * - Navigates strictly in spatial visual order: Left to Right across each row, then Down to the next row.
 * - De-duplicates MUI form controls to prevent jumping or skipping fields.
 * - On the final field of the section, pressing Enter triggers `onFinalSubmit` to advance to the next step.
 */
export const handleFormEnterKeyDown = (
  e: React.KeyboardEvent<HTMLElement>,
  onFinalSubmit?: () => void
) => {
  if (e.key !== 'Enter') return;

  const target = e.target as HTMLElement;

  // Allow normal Enter key behavior for multiline textareas or explicit buttons
  if (
    target.tagName === 'TEXTAREA' ||
    target.tagName === 'BUTTON' ||
    (target.getAttribute('role') === 'button' && !target.classList.contains('MuiSelect-select'))
  ) {
    return;
  }

  // Prevent default form submission on enter key
  e.preventDefault();

  const form = e.currentTarget;

  // Query potential interactive input elements
  const rawElements = Array.from(
    form.querySelectorAll<HTMLElement>(
      'input:not([type=hidden]):not([disabled]):not([readonly]), select:not([disabled]), [role="combobox"]:not([aria-disabled=true]), [tabindex="0"]:not([disabled])'
    )
  );

  // Filter out hidden, non-visible, or dummy autofill elements
  const visibleElements = rawElements.filter((el) => {
    if (el.id?.startsWith('prevent_autofill_') || el.getAttribute('name')?.startsWith('prevent_autofill_')) {
      return false;
    }
    const rect = el.getBoundingClientRect();
    if (rect.width === 0 || rect.height === 0) return false;
    const style = getComputedStyle(el);
    return style.visibility !== 'hidden' && style.display !== 'none';
  });

  // Group elements by parent MuiFormControl container so each field only has ONE focus target
  const uniqueFocusables: HTMLElement[] = [];
  const visitedContainers = new Set<Element>();

  for (const el of visibleElements) {
    const container = el.closest('.MuiFormControl-root') || el.closest('.MuiOutlinedInput-root') || el;
    if (!visitedContainers.has(container)) {
      visitedContainers.add(container);
      uniqueFocusables.push(el);
    }
  }

  // Sort focusables spatially: Row by Row (Top to Bottom), Left to Right within each Row
  uniqueFocusables.sort((a, b) => {
    const rectA = a.getBoundingClientRect();
    const rectB = b.getBoundingClientRect();
    const topDiff = rectA.top - rectB.top;

    // If elements are on the same visual line (within 20px threshold), sort Left-to-Right
    if (Math.abs(topDiff) < 20) {
      return rectA.left - rectB.left;
    }
    // Otherwise sort Top-to-Bottom
    return topDiff;
  });

  // Find index of current target (or its parent container element)
  const currentContainer = target.closest('.MuiFormControl-root') || target.closest('.MuiOutlinedInput-root') || target;
  let currentIndex = uniqueFocusables.findIndex(
    (el) => el === target || el.closest('.MuiFormControl-root') === currentContainer || el.closest('.MuiOutlinedInput-root') === currentContainer
  );

  if (currentIndex < 0) {
    currentIndex = uniqueFocusables.indexOf(target);
  }

  if (currentIndex >= 0 && currentIndex < uniqueFocusables.length - 1) {
    const nextEl = uniqueFocusables[currentIndex + 1];
    nextEl.focus();
    if (nextEl instanceof HTMLInputElement && nextEl.type !== 'date') {
      nextEl.select();
    }
  } else if (currentIndex === uniqueFocusables.length - 1 || currentIndex === -1) {
    if (onFinalSubmit) {
      onFinalSubmit();
    }
  }
};
