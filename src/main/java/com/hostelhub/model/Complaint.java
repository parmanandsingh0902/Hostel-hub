package com.hostelhub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "complaints")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ComplaintCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ComplaintPriority priority = ComplaintPriority.MEDIUM;

    @Column(name = "room_number", nullable = false)
    private String roomNumber;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_user_id")
    private User studentUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hostel_group_id")
    private HostelGroup hostelGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_warden_id")
    private User assignedWarden;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ComplaintStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public Complaint() {
    }

    public Complaint(Long id, String title, String description, ComplaintCategory category,
                     ComplaintPriority priority, String roomNumber, String studentName,
                     User studentUser, HostelGroup hostelGroup, User assignedWarden,
                     ComplaintStatus status, LocalDateTime createdAt, LocalDateTime updatedAt,
                     LocalDateTime resolvedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority != null ? priority : ComplaintPriority.MEDIUM;
        this.roomNumber = roomNumber;
        this.studentName = studentName;
        this.studentUser = studentUser;
        this.hostelGroup = hostelGroup;
        this.assignedWarden = assignedWarden;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.resolvedAt = resolvedAt;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.status == null) {
            this.status = ComplaintStatus.PENDING;
        }
        if (this.priority == null) {
            this.priority = ComplaintPriority.MEDIUM;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public User getStudentUser() {
        return studentUser;
    }

    public void setStudentUser(User studentUser) {
        this.studentUser = studentUser;
    }

    public HostelGroup getHostelGroup() {
        return hostelGroup;
    }

    public void setHostelGroup(HostelGroup hostelGroup) {
        this.hostelGroup = hostelGroup;
    }

    public User getAssignedWarden() {
        return assignedWarden;
    }

    public void setAssignedWarden(User assignedWarden) {
        this.assignedWarden = assignedWarden;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Complaint complaint = (Complaint) o;
        return Objects.equals(id, complaint.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Complaint{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category=" + category +
                ", priority=" + priority +
                ", roomNumber='" + roomNumber + '\'' +
                ", studentName='" + studentName + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", resolvedAt=" + resolvedAt +
                '}';
    }
}
