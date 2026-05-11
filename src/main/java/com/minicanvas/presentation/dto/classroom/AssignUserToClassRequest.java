package com.minicanvas.presentation.dto.classroom;

public class AssignUserToClassRequest {
    public Long userId;
    public Long classId;
    public String classRole; // STUDENT or TEACHER
}