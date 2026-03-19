package com.taskflow.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for PUT /projects/{id}
 *
 * Both fields are optional — only provided fields are updated.
 * At least one field should be present (validated in service).
 */
@Data
public class ProjectUpdateRequest {

    @Size(min = 2, max = 150, message = "Project name must be between 2 and 150 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}