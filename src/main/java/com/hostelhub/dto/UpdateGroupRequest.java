package com.hostelhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateGroupRequest {

    @NotBlank(message = "Group name is required")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotBlank(message = "Section code is required")
    @Size(max = 50, message = "Section code cannot exceed 50 characters")
    private String sectionCode;

    public UpdateGroupRequest() {
    }

    public UpdateGroupRequest(String name, String description, String sectionCode) {
        this.name = name;
        this.description = description;
        this.sectionCode = sectionCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSectionCode() {
        return sectionCode;
    }

    public void setSectionCode(String sectionCode) {
        this.sectionCode = sectionCode;
    }
}
