package com.cybertek.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role extends BaseEntity{

    private String description;

    @OneToMany(mappedBy = "role",fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();

}
