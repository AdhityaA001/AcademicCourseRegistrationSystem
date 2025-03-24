package com.system.academicCourseRegistration.model;

import java.util.List;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseSummaryDetails {
    private String courseId;
    private String courseName;
    private String professor;
    private int enrolledStudentsCount;
    private int maxStudentsCount;
    private List<CourseSchedule> courseSchedule;
}
