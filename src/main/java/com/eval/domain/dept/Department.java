package com.eval.domain.dept;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "department_52")
public class Department {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "leader_emp_no")
    private String leaderEmpNo;

    @Column(name = "name")
    private String name;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "use_yn")
    private boolean useYn;
    
    @Column(name = "level")
    private int level;
    
    @Column(name="delete_yn")
    private Boolean deleteYn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Department parent;

    @Transient
    private List<Department> children;
    
    @Column(name = "parent_id", insertable = false, updatable = false) 
    private String parentId;
}