package com.hostelhub.dto;

import com.hostelhub.model.ComplaintPriority;
import jakarta.validation.constraints.NotNull;

public class UpdatePriorityRequest {

    @NotNull(message = "Priority is required")
    private ComplaintPriority priority;

    public UpdatePriorityRequest() {
    }

    public UpdatePriorityRequest(ComplaintPriority priority) {
        this.priority = priority;
    }

    public ComplaintPriority getPriority() {
        return priority;
    }

    public void setPriority(ComplaintPriority priority) {
        this.priority = priority;
    }
}
