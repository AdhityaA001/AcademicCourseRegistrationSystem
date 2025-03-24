package com.system.academicCourseRegistration.service;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.system.academicCourseRegistration.exception.ValidationException;
import com.system.academicCourseRegistration.model.CourseItem;
import com.system.academicCourseRegistration.model.StudentEnrollmentDetailsItem;
import com.system.academicCourseRegistration.repository.StudentEnrollmentDetailsRepository;
import com.system.academicCourseRegistration.util.AppUtils;

@Service
public class StudentEnrollmentService {
	
	private final StudentEnrollmentDetailsRepository studentEnrollmentDetailsRepository;
	private final CollegeCoursesService collegeCoursesService;

	public StudentEnrollmentService(StudentEnrollmentDetailsRepository studentEnrollmentDetailsRepository, CollegeCoursesService collegeCoursesService) {
		this.studentEnrollmentDetailsRepository = studentEnrollmentDetailsRepository;
		this.collegeCoursesService = collegeCoursesService;
	}
	
	public String registerCourse(String courseId, String studentId, String studentName) {
		
		if(Objects.isNull(courseId) || Objects.isNull(studentId)) {
			throw new IllegalArgumentException("CourseId and studentId are mandatory to register student to the course");
		}
        
		CourseItem courseItem = collegeCoursesService.getCourseDetails(courseId);
		
		if(Objects.isNull(courseItem.getMaxStudents()) || Objects.isNull(courseItem.getCourseSchedule()) || Objects.isNull(courseItem.getProfessorId())) {
			throw new ValidationException("Course with ID " + courseId + " is not ready for enrollment yet.");
		}
		
		if (courseItem.getEnrolledStudents() >= courseItem.getMaxStudents()) {
			throw new ValidationException("Course with ID " + courseId + " is already at full capacity.");
	    }
	    
		Optional<StudentEnrollmentDetailsItem> studentEnrollment = studentEnrollmentDetailsRepository.getStudentById(studentId);

	    // Check for schedule conflicts
	    if (studentEnrollment.isPresent()) {
	        for (String enrolledCourseId : studentEnrollment.get().getCourseId()) {
	        	if (courseId.equalsIgnoreCase(enrolledCourseId)) {
	        		throw new ValidationException("Already enrolled in course " + courseId);
	        	}
	            Optional<CourseItem> enrolledCourse = collegeCoursesService.getCourseById(enrolledCourseId);
	            if (enrolledCourse.isPresent() && AppUtils.hasScheduleConflict(courseItem.getCourseSchedule(), enrolledCourse.get().getCourseSchedule())) {
	            	throw new ValidationException("Course with ID " + courseId + " has a schedule conflict with an already enrolled course " + enrolledCourse.get().getCourseId() + ".");
	            }
	        }
	    }
	    else if(Objects.isNull(studentName)) {
        	throw new ValidationException("StudentName is mandatory for new student");
        }

	    // Transaction Request Builder
	    var incrementEnrolledCountExpression = collegeCoursesService.createCourseIncrementExpression(courseItem);
	    var response = "";
        if(studentEnrollment.isPresent()) {
        	var updateStudentEnrollment = studentEnrollmentDetailsRepository.createUpdateStudentExpression(studentEnrollment.get(), courseId);
        	response = studentEnrollmentDetailsRepository.registerCourse(incrementEnrolledCountExpression, updateStudentEnrollment);
        }
        else {
        	var createStudentEnrollment = studentEnrollmentDetailsRepository.createNewStudentExpression(studentId, studentName, courseId);
        	response = studentEnrollmentDetailsRepository.registerCourse(incrementEnrolledCountExpression, createStudentEnrollment);
        }
        
		return response;
    }
	
	public String withdrawCourse(String studentId, String courseId) {
		
		if(Objects.isNull(courseId) || Objects.isNull(studentId)) {
			throw new IllegalArgumentException("CourseId and studentId are mandatory to withdraw student from the course");
		}
		
		if(studentEnrollmentDetailsRepository.withdrawCourse(studentId, courseId)) {
			if(collegeCoursesService.updateEnrolledCountAfterWithdrawal(courseId)) {
				return "Student withdrawn from the course and course enrollment count reduced";
			}
			else {
				return "Student withdrawn from the course but course enrollment count could not be reduced";
			}
		}
		return "Student could not be withdrawn from the course";
	}

}