package com.system.academicCourseRegistration.model;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Getter
@Setter
@DynamoDbBean
public class UserItem {
	
	private String userName;
    private String encryptedPwd;
    private String role; // ADMIN, PROFESSOR, STUDENT

    @DynamoDbPartitionKey
	public String getUserName() {
		return userName;
	}  

}
