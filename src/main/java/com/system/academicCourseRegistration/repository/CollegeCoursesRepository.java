package com.system.academicCourseRegistration.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.system.academicCourseRegistration.exception.ApplicationException;
import com.system.academicCourseRegistration.model.CourseItem;
import com.system.academicCourseRegistration.model.PaginatedResult;

import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactDeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactUpdateItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Repository
public class CollegeCoursesRepository {
	
	private final DynamoDbTable<CourseItem> courseTable;
	private final DynamoDbIndex<CourseItem> professorIndex;
	private final StudentEnrollmentDetailsRepository studentEnrollmentDetailsRepository;

    public CollegeCoursesRepository(DynamoDbEnhancedClient enhancedClient, StudentEnrollmentDetailsRepository studentEnrollmentDetailsRepository) {
        this.courseTable = enhancedClient.table("CollegeCourses", TableSchema.fromBean(CourseItem.class));
        this.professorIndex = courseTable.index("CollegeCoursesByProfessorId");
        this.studentEnrollmentDetailsRepository = studentEnrollmentDetailsRepository;
    }

    public String saveCourse(CourseItem courseItem) {
        try {
        	courseTable.putItem(courseItem);
        	return "Successfully created new course with coursId " + courseItem.getCourseId();
        } catch (Exception e) {
        	throw new ApplicationException("Could not create new course and save in table.\nException: "+e.getMessage());
        }    
    }
    
    public PaginatedResult<CourseItem> getCourseSummary(int pageSize, String lastEvaluatedKey) {
        Map<String, AttributeValue> startKey = null;

        if (lastEvaluatedKey != null) {
            startKey = Map.of("courseId", AttributeValue.fromS(lastEvaluatedKey));
        }

        ScanEnhancedRequest request = ScanEnhancedRequest.builder()
            .limit(pageSize)
            .exclusiveStartKey(startKey)
            .build();

        Page<CourseItem> firstPage = courseTable.scan(request).stream().findFirst().orElse(null);

        if (firstPage == null || firstPage.items().isEmpty()) {
            return new PaginatedResult<>(Collections.emptyList(), null);
        }

        return new PaginatedResult<>(firstPage.items(),
                firstPage.lastEvaluatedKey() != null ? firstPage.lastEvaluatedKey().get("courseId").s() : null);
    }
    
    public Optional<CourseItem> getCourseById(String courseId) {
    	Key key = Key.builder().partitionValue(courseId).build();
        return Optional.ofNullable(courseTable.getItem(key));
    }
    
    public List<CourseItem> getCoursesByProfessor(String professorId) {
        SdkIterable<Page<CourseItem>> courseItems = professorIndex.query(r -> r.queryConditional(
                QueryConditional.keyEqualTo(k -> k.partitionValue(professorId))));
        
        var courseItemsList = new ArrayList<CourseItem>();
        
        courseItems.forEach(courseItem -> courseItemsList.addAll(courseItem.items()));
        
        return courseItemsList;
    }
    
    public boolean updateEnrolledCountAfterWithdrawal(String courseId) {
    	Optional<CourseItem> course = getCourseById(courseId);
    	try {
    		if(course.isPresent()) {
        		CourseItem courseItem = course.get();
        		courseItem.setEnrolledStudents(courseItem.getEnrolledStudents()-1);
        		courseTable.updateItem(courseItem);
        		return true;
        	}
    	} catch (Exception e) {
    		return false;
    	}
    	return false;
    }
    
    public TransactUpdateItemEnhancedRequest<CourseItem> createIncrementEnrolledStudentCountExpression(CourseItem courseItem) {
    	Expression conditionExpression = Expression.builder()
    	        .expression("#enrolled <= #max")
    	        .expressionNames(Map.of(
    	            "#enrolled", "enrolledStudents",
    	            "#max", "maxStudents"
    	        ))
    	        .build();
    	
    		courseItem.setEnrolledStudents(courseItem.getEnrolledStudents()+1);

    	    // Build the request
    	    return TransactUpdateItemEnhancedRequest.builder(CourseItem.class)
    	        .item(courseItem)                 // Include the updated object
    	        .conditionExpression(conditionExpression) // Add condition
    	        .build();
        		
    }
    
    public String updateTable(CourseItem courseItem) {
    	try {
    		courseTable.updateItem(courseItem);
        	return "Updated details for course.";
    	} catch(Exception e) {
    		throw new ApplicationException("Could not update the table with the updated course details.\n Exception: "+e.getMessage());
    	}
    	
    	
    }
    
    public String deleteCourse(String courseId) {

        // Step 1: Delete Course from Course Table
    	TransactDeleteItemEnhancedRequest courseDeleteRequest =
                TransactDeleteItemEnhancedRequest.builder()
                    .key(k -> k.partitionValue(courseId))
                    .build();

        // Step 3: Build Transaction Request
//        TransactWriteItemsEnhancedRequest transactionRequest =
//            TransactWriteItemsEnhancedRequest.builder()
//                .addDeleteItem(courseTable, courseDeleteRequest)     // Delete course   // Remove course from students
//                .build();
        
    	var response = studentEnrollmentDetailsRepository.removeCourseFromAllStudents(courseId, courseDeleteRequest);
        return response;
    }

}
