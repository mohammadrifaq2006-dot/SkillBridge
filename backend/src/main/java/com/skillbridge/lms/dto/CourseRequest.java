package com.skillbridge.lms.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CourseRequest {

    @NotBlank(message = "Course title is required")
    @Size(min = 3, max = 150, message = "Course title must be between 3 and 150 characters")
    private String title;

    @NotBlank(message = "Category is required")
    @Size(min = 2, max = 80, message = "Category must be between 2 and 80 characters")
    private String category;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 3000, message = "Description must be at least 10 characters")
    private String description;

    @NotBlank(message = "Instructor name is required")
    @Size(min = 2, max = 100, message = "Instructor name must be between 2 and 100 characters")
    private String instructor;

    @NotBlank(message = "Duration is required")
    @Size(min = 2, max = 60, message = "Duration is required (e.g., 6 Weeks, 20 Hours)")
    private String duration;

    @NotNull(message = "Price is required (enter 0 for free courses)")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    private BigDecimal price = BigDecimal.ZERO;

    @NotBlank(message = "Syllabus is required")
    @Size(min = 10, max = 5000, message = "Syllabus must be at least 10 characters")
    private String syllabus;

    @NotBlank(message = "Prerequisites are required")
    @Size(min = 2, max = 2000, message = "Prerequisites are required (enter 'None' if none)")
    private String prerequisites;

    private String imageUrl;

    public CourseRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getSyllabus() {
        return syllabus;
    }

    public void setSyllabus(String syllabus) {
        this.syllabus = syllabus;
    }

    public String getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
