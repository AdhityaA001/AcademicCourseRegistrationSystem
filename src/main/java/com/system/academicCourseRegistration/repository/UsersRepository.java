package com.system.academicCourseRegistration.repository;

import org.springframework.stereotype.Repository;

import com.system.academicCourseRegistration.model.UserItem;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class UsersRepository {
	
	private DynamoDbTable<UserItem> userTable;
	
    public UsersRepository(DynamoDbEnhancedClient enhancedClient) {
		this.userTable = enhancedClient.table("Users", TableSchema.fromBean(UserItem.class));
	}
	
	public UserItem findByUsername(String userName) {
        return userTable.getItem(r -> r.key(k -> k.partitionValue(userName)));
    }

}
