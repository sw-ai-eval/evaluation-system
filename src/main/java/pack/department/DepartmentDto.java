package pack.department;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentDto {

    private String id;
    private String name;
    private String leaderEmpNo;
    private String parentId;
    private Boolean useYn;
    private int level;
}