package com.hostelhub.dto;

import com.hostelhub.model.ComplaintCategory;
import com.hostelhub.model.ComplaintPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateComplaintRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Category is required")
    private ComplaintCategory category;

    private ComplaintPriority priority;

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    private String studentName;

    public CreateComplaintRequest() {
    }

    public CreateComplaintRequest(String title, String description, ComplaintCategory category, String roomNumber, String studentName) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.roomNumber = roomNumber;
        this.studentName = studentName;
    }

    public CreateComplaintRequest(String title, String description, ComplaintCategory category, ComplaintPriority priority, String roomNumber, String studentName) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.roomNumber = roomNumber;
        this.studentName = studentName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ComplaintCategory getCategory() {
        return category;
    }

    public void setCategory(ComplaintCategory category) {
        this.category = category;
    }

    public ComplaintPriority getPriority() {
        return priority;
    }

    public void setPriority(ComplaintPriority priority) {
        this.priority = priority;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
}
