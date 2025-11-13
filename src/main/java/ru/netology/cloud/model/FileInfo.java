package ru.netology.cloud.model;

import jakarta.persistence.*;

@Entity
@Table(name = "files")
public class FileInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    private Long size;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public FileInfo() {}

    public FileInfo(String filename, Long size, User user) {
        this.filename = filename;
        this.size = size;
        this.user = user;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
