package com.system.academicCourseRegistration.model;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class StudentEnrollmentDetailsItem {
	
	@NotBlank(message="studentId cannot be empty")
	private String studentId;
	@NotBlank(message="studentName cannot be empty")
	private String studentName;
	private List<String> courseId;
	
	@DynamoDbPartitionKey
    public String getStudentId() {
        return studentId;
    }
	

}
