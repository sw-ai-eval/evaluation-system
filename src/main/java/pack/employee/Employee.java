package pack.employee;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "employee_52")
public class Employee {

    @Id
    @Column(name = "emp_no")
    private String empNo;

    @Column(name = "name")
    private String name;

    @Column(name = "dept_id")
    private String deptId;

    @Column(name = "password")
    private String password;


}