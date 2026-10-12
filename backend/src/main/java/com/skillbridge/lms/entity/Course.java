package com.skillbridge.lms.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(length = 40)
    private String difficulty = "Intermediate";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String instructor;

    @Column(nullable = false, length = 60)
    private String duration;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String syllabus;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prerequisites;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean published = true;

    public Course() {
    }

    public Course(String title, String category, String description, String instructor,
                  String duration, BigDecimal price, String syllabus, String prerequisites, String imageUrl) {
        this(title, category, "Intermediate", description, instructor, duration, price, syllabus, prerequisites, imageUrl, true);
    }

    public Course(String title, String category, String difficulty, String description, String instructor,
                  String duration, BigDecimal price, String syllabus, String prerequisites, String imageUrl, Boolean published) {
        this.title = title;
        this.category = category;
        this.difficulty = (difficulty != null && !difficulty.isBlank()) ? difficulty : "Intermediate";
        this.description = description;
        this.instructor = instructor;
        this.duration = duration;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.syllabus = syllabus;
        this.prerequisites = prerequisites;
        this.imageUrl = imageUrl;
        this.published = published != null ? published : true;
    }

    @PrePersist
    @PreUpdate
    protected void applyDefaults() {
        if (this.difficulty == null || this.difficulty.isBlank()) {
            this.difficulty = "Intermediate";
        }
        if (this.published == null) {
            this.published = true;
        }
        if (this.price == null) {
            this.price = BigDecimal.ZERO;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDifficulty() {
        return difficulty != null ? difficulty : "Intermediate";
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
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

    public Boolean getPublished() {
        return published == null || published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }
}
