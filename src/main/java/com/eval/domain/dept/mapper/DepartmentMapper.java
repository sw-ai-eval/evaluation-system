package com.eval.domain.dept.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import com.eval.domain.dept.dto.DepartmentDto;

@Mapper
public interface DepartmentMapper {

    List<DepartmentDto> selectDepartmentList();
    
    List<DepartmentDto> search(@Param("name") String name,
            @Param("useYn") Boolean useYn);
}