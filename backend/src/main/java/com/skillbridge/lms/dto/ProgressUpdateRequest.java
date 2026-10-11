package com.skillbridge.lms.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ProgressUpdateRequest {

    @NotNull(message = "Completion percentage is required")
    @Min(value = 0, message = "Completion percentage must be between 0 and 100")
    @Max(value = 100, message = "Completion percentage must be between 0 and 100")
    private Integer completionPercentage;

    public ProgressUpdateRequest() {
    }

    public ProgressUpdateRequest(Integer completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public Integer getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(Integer completionPercentage) {
        this.completionPercentage = completionPercentage;
    }
}
