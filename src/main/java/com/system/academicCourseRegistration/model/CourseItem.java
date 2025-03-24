package com.system.academicCourseRegistration.model;

import java.util.List;

import lombok.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class CourseItem {
	@NotBlank(message="courseId cannot be empty")
	private String courseId;
	private String courseName;
	@Valid
	private List<CourseSchedule> courseSchedule;
	private Integer maxStudents;
	private Integer enrolledStudents;
	private String professorId;
	
	@DynamoDbPartitionKey
    public String getCourseId() {
        return courseId;
    }
	
	@DynamoDbSecondaryPartitionKey(indexNames="CollegeCoursesByProfessorId")
	public String getProfessorId() {
		return professorId;
	}

}
