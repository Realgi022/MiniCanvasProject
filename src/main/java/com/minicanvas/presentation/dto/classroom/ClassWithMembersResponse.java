package com.minicanvas.presentation.dto.classroom;

import java.util.List;

public class ClassWithMembersResponse {
    public Long classId;
    public String className;
    public List<ClassMemberResponse> members;

    public ClassWithMembersResponse(
            Long classId,
            String className,
            List<ClassMemberResponse> members
    ) {
        this.classId = classId;
        this.className = className;
        this.members = members;
    }
}