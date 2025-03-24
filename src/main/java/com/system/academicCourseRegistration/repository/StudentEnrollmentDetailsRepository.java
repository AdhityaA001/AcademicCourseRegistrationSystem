package com.system.academicCourseRegistration.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.system.academicCourseRegistration.exception.ApplicationException;
import com.system.academicCourseRegistration.exception.ValidationException;
import com.system.academicCourseRegistration.model.CourseItem;
import com.system.academicCourseRegistration.model.StudentEnrollmentDetailsItem;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactDeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactPutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactUpdateItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Repository
public class StudentEnrollmentDetailsRepository {
	
	private final DynamoDbTable<StudentEnrollmentDetailsItem> studentEnrollmentTable;
	private final DynamoDbTable<CourseItem> courseTable;
	private final DynamoDbEnhancedClient enhancedClient;

    public StudentEnrollmentDetailsRepository(DynamoDbEnhancedClient enhancedClient) {
        this.studentEnrollmentTable = enhancedClient.table("StudentEnrollmentDetails", TableSchema.fromBean(StudentEnrollmentDetailsItem.class));
        this.courseTable = enhancedClient.table("CollegeCourses", TableSchema.fromBean(CourseItem.class));
        this.enhancedClient = enhancedClient;
    }
    
    public Optional<StudentEnrollmentDetailsItem> getStudentById(String studentId) {
    	Key key = Key.builder().partitionValue(studentId).build();
        return Optional.ofNullable(studentEnrollmentTable.getItem(key));
    }
    
    public String registerCourse(TransactUpdateItemEnhancedRequest<CourseItem> incrementEnrolledCountExpression,
    	TransactUpdateItemEnhancedRequest<StudentEnrollmentDetailsItem> updateStudentEnrollment) {
    	TransactWriteItemsEnhancedRequest transactionRequest = TransactWriteItemsEnhancedRequest.builder()
    			.addUpdateItem(courseTable, incrementEnrolledCountExpression)
    			.addUpdateItem(studentEnrollmentTable, updateStudentEnrollment)
    			.build();
    	
    	try {
            enhancedClient.transactWriteItems(transactionRequest);
            return "Student enrolled successfully in course.";
        } catch (Exception e) {
            throw new ApplicationException("Student could not be enrolled in the course.\nException: " + e.getMessage());
        }
		
	}
    
    public String registerCourse(TransactUpdateItemEnhancedRequest<CourseItem> incrementEnrolledCountExpression,
    		TransactPutItemEnhancedRequest<StudentEnrollmentDetailsItem> createStudentEnrollment) {
    	TransactWriteItemsEnhancedRequest transactionRequest = TransactWriteItemsEnhancedRequest.builder()
    			.addUpdateItem(courseTable, incrementEnrolledCountExpression)
    			.addPutItem(studentEnrollmentTable, createStudentEnrollment)
    			.build();
    	
    	try {
            enhancedClient.transactWriteItems(transactionRequest);
            return "Student enrolled successfully in course.";
        } catch (Exception e) {
        	throw new ApplicationException("Student could not be enrolled in the course.\nException: " + e.getMessage());
        }
		
	}
    
    public TransactUpdateItemEnhancedRequest<StudentEnrollmentDetailsItem> createUpdateStudentExpression(StudentEnrollmentDetailsItem studentEnrollment, String courseId) {
    	
    	studentEnrollment.getCourseId().add(courseId);

        // Step 4: Conditional Expression to prevent overwrite issues
        Expression conditionExpression = Expression.builder()
            .expression("attribute_exists(courseId)")  // Ensures the list hasn't been deleted or modified
            .build();

        // Step 5: Build the request
        return TransactUpdateItemEnhancedRequest.builder(StudentEnrollmentDetailsItem.class)
                .item(studentEnrollment)
                .conditionExpression(conditionExpression)
                .build();
        		
    }
    public TransactPutItemEnhancedRequest<StudentEnrollmentDetailsItem> createNewStudentExpression(String studentId, String studentName, String courseId) {
    	return TransactPutItemEnhancedRequest.builder(StudentEnrollmentDetailsItem.class)
                .item(StudentEnrollmentDetailsItem.builder()
                        .studentId(studentId)
                        .studentName(studentName)
                        .courseId(List.of(courseId)) // First course entry
                        .build())
                .build();
        		
    }
    
    public boolean withdrawCourse(String studentId, String courseId) {
    	Optional<StudentEnrollmentDetailsItem> student = getStudentById(studentId);
    	if(student.isEmpty()) {
    		throw new ValidationException("Could not find student for provided studentId.");
    	}
    	StudentEnrollmentDetailsItem studentItem = student.get();
    	List<String> updatedCourseList = studentItem.getCourseId().stream()
    	        .filter(id -> !id.equals(courseId))  // Filter out the course to delete
    	        .collect(Collectors.toList());
    	if(studentItem.getCourseId().size()==updatedCourseList.size()) {
    		throw new ValidationException("Student was not enrolled in the courseId provided.");
    	}
    	studentItem.setCourseId(updatedCourseList);
    	studentEnrollmentTable.updateItem(studentItem);
    	return true;
    }
    
    public String removeCourseFromAllStudents(String courseId, TransactDeleteItemEnhancedRequest transactionRequest) {
        // Step 1: Identify Students Who Have the Course ID
        List<StudentEnrollmentDetailsItem> studentsWithCourse = new ArrayList<>();

        Expression filterExpression = Expression.builder()
        	    .expression("contains(courseId, :courseId)")  // Filter condition
        	    .expressionValues(Map.of(":courseId", AttributeValue.fromS(courseId))) // Placeholder values
        	    .build();

        	// Step 2: Build ScanEnhancedRequest with Correct Filter Expression
        	ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder()
        	    .filterExpression(filterExpression)  // Correct usage here
        	    .build();

        	studentEnrollmentTable.scan(scanRequest).items().forEach(studentsWithCourse::add);

        if (studentsWithCourse.isEmpty()) {
            return "No students found with course ID: " + courseId;
        }

        // Step 2: Build Transaction Requests to Remove Course ID
        TransactWriteItemsEnhancedRequest.Builder transactionRequestBuilder = TransactWriteItemsEnhancedRequest.builder();

        for (StudentEnrollmentDetailsItem student : studentsWithCourse) {
        	List<String> updatedCourseList = student.getCourseId().stream()
        	        .filter(id -> !id.equals(courseId))  // Filter out the course to delete
        	        .collect(Collectors.toList());

        	    // Step 2: Build the Updated Item with Modified `courseIds`
        	    student.setCourseId(updatedCourseList);

        	    // Step 3: Build the Update Request
        	    TransactUpdateItemEnhancedRequest<StudentEnrollmentDetailsItem> transactUpdateRequest = TransactUpdateItemEnhancedRequest.builder(StudentEnrollmentDetailsItem.class)
                .item(student)
                .build();

            transactionRequestBuilder.addUpdateItem(studentEnrollmentTable, transactUpdateRequest);
        }
        
        transactionRequestBuilder.addDeleteItem(courseTable, transactionRequest);

        // Step 3: Execute Transaction (Batched)
        try {
            enhancedClient.transactWriteItems(transactionRequestBuilder.build());
            return "Successfully delete course with ID: " + courseId + " and withdrew all students from the deleted course.";
        } catch (Exception e) {
            throw new ApplicationException("Transaction failed: " + e.getMessage());
        }
    }


}
