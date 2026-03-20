package ru.ilya.filedrive.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "roles")
@Data
public class Role {

    @Column(length = 16)
    @Id
    private String name;
}
