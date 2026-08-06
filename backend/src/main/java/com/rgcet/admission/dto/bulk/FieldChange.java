package com.rgcet.admission.dto.bulk;

import java.util.List;

/**
 * The validated diff of a single field, shown to the user in the preview step.
 */
public record FieldChange(
        String sheet,
        String field,
        String oldValue,
        String newValue
) {
}
