package com.system.academicCourseRegistration.model;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Getter
@Setter
@DynamoDbBean
public class CourseSchedule {
	
	@NotBlank(message="dayOfWeek cannot be blank")
	private String dayOfWeek;
	
	@NotBlank(message="startTime cannot be blank")
	private String startTime;
	
	@NotBlank(message="endTime cannot be blank")
	private String endTime;

}
