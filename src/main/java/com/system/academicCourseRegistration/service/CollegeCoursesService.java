package com.system.academicCourseRegistration.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.system.academicCourseRegistration.exception.ValidationException;
import com.system.academicCourseRegistration.model.CourseItem;
import com.system.academicCourseRegistration.model.CourseSchedule;
import com.system.academicCourseRegistration.repository.CollegeCoursesRepository;
import com.system.academicCourseRegistration.util.AppUtils;

import software.amazon.awssdk.enhanced.dynamodb.model.TransactUpdateItemEnhancedRequest;

@Service
public class CollegeCoursesService {
	
	private final CollegeCoursesRepository collegeCoursesRepository;
	
	public CollegeCoursesService(CollegeCoursesRepository collegeCoursesRepository) {
		this.collegeCoursesRepository = collegeCoursesRepository;
	}
	
	public String createCourse(CourseItem course) {
        
        if (getCourseById(course.getCourseId()).isPresent()) {
            throw new ValidationException("Course with ID " + course.getCourseId() + " already exists. Please consider editing the course.");
        }
        
        if(Objects.nonNull(course.getProfessorId()))
        {
        	if(Objects.nonNull(course.getCourseSchedule()) && checkProfessorCourseConflict(course.getProfessorId(), course)) {
        		throw new ValidationException("Professor has a conflict and cannot be assigned to course "+course.getCourseId());
        	}
        	else if(Objects.isNull(course.getCourseSchedule())) {
        		throw new ValidationException("Professor cannot be assigned since course does not have a schedule and cannot check for conflict");
        	}
        }
        course.setEnrolledStudents(0);

        var response = collegeCoursesRepository.saveCourse(course);
        return response;
    }
	
	public Map<String, String> editCourse(CourseItem courseItem) {
		
		CourseItem existingCourseItem = getCourseDetails(courseItem.getCourseId());
		Map<String, String> output = new HashMap<String, String>();
		
		if(Objects.nonNull(courseItem.getCourseName())) {
			existingCourseItem.setCourseName(courseItem.getCourseName());
			output.put("courseName", "Updated");
		}
		if(Objects.nonNull(courseItem.getMaxStudents())) {
			if(Objects.nonNull(existingCourseItem.getEnrolledStudents())
				&& courseItem.getMaxStudents() > existingCourseItem.getEnrolledStudents()) {
				existingCourseItem.setMaxStudents(courseItem.getMaxStudents());
				output.put("maxStudents", "Updated");
			}
			else {
				output.put("maxStudents", "Max students cannot be lower than already enrolled student count");
			}
			
		}
		if(Objects.nonNull(courseItem.getProfessorId())) {
			if(Objects.isNull(existingCourseItem.getProfessorId()) || 
					!checkProfessorCourseConflict(courseItem.getProfessorId(), existingCourseItem)) {
				existingCourseItem.setProfessorId(courseItem.getProfessorId());
				output.put("professorId", "Updated");
			}
			else {
				output.put("professorId", "Professor has schedule conflict with existing courses");
			}
			
		}
		if(Objects.nonNull(courseItem.getCourseSchedule())) {
			if ((Objects.isNull(existingCourseItem.getCourseSchedule())) || 
					(existingCourseItem.getEnrolledStudents()==0 && 
					(Objects.isNull(existingCourseItem.getProfessorId()) || 
						!checkProfessorCourseConflict(existingCourseItem.getProfessorId(), courseItem)))) {
				existingCourseItem.setCourseSchedule(courseItem.getCourseSchedule());
				output.put("courseSchedule", "Updated");
			}
			else {
				output.put("courseSchedule", "Schedule already exists or students and professor already assigned to course and may have schedule conflict");
			}
			
		}
		
		if(output.isEmpty()) {
			output.put("Fields updated", "No edits were provided for the course. Please provide valid edits.");
			return output;
		}
		
		collegeCoursesRepository.updateTable(existingCourseItem);
		
		return output;
	}
	
	public String setProfessor(String professorId, String courseId, boolean overrideInd) {
		
		if(Objects.isNull(courseId) || Objects.isNull(professorId)) {
			throw new IllegalArgumentException("CourseId and ProfessorId are mandatory to assign professor to the course");
		}
		
		CourseItem courseItem = getCourseDetails(courseId);
		
		if(Objects.nonNull(courseItem.getProfessorId()) && professorId.equalsIgnoreCase(courseItem.getProfessorId())) {
			throw new ValidationException("Existing professorId is the same as the new value.");
		}
		
		if(Objects.nonNull(courseItem.getProfessorId()) && !overrideInd) {
			throw new ValidationException("Course already has a professor assigned. Use overrideInd to change professor");
		}
		
		if(checkProfessorCourseConflict(professorId, courseItem)) {
			throw new ValidationException("Professor has a conflict and cannot be assigned to course "+courseId);
		}
		
		courseItem.setProfessorId(professorId);
		var response = collegeCoursesRepository.updateTable(courseItem).replace("details", "professorId");
		return response;
		
	}
	
	public String deleteCourse(String courseId) {
		
		if(Objects.isNull(courseId)) {
			throw new IllegalArgumentException("CourseId is mandatory.");
		}
		
		if(getCourseById(courseId).isEmpty()) {
			throw new ValidationException("Course with courseId "+courseId+" does not exist. No changes performed.");
		}
		var response = collegeCoursesRepository.deleteCourse(courseId);
		return response;
	}
	
	public String setMaxStudents(String courseId, int maxStudents) {
		
		if(Objects.isNull(courseId) || Objects.isNull(maxStudents)) {
			throw new IllegalArgumentException("CourseId and maxStudents are mandatory.");
		}
		
		CourseItem courseItem = getCourseDetails(courseId);
		
		if(Objects.nonNull(courseItem.getEnrolledStudents()) && maxStudents < courseItem.getEnrolledStudents()) {
			throw new IllegalArgumentException("Course already has more students enrolled than the new max limit");
		}
		
		if(courseItem.getMaxStudents()==maxStudents) {
			throw new ValidationException("Existing maxStudents is the same as the new value.");
		}
		
		courseItem.setMaxStudents(maxStudents);
		var response = collegeCoursesRepository.updateTable(courseItem).replace("details", "maxStudents");
		return response;
		
	}
	
	public String setWeeklySchedule(String courseId, List<CourseSchedule> courseSchedule) {	
		
		if(Objects.isNull(courseId) || Objects.isNull(courseSchedule)) {
			throw new IllegalArgumentException("CourseId and courseSchedule are mandatory.");
		}
		
		CourseItem courseItem = getCourseDetails(courseId);
		
		if(Objects.nonNull(courseItem.getCourseSchedule()) &&
				(Objects.nonNull(courseItem.getProfessorId()) &&
						checkProfessorCourseConflict(courseItem.getProfessorId(), courseItem)) && 
								courseItem.getEnrolledStudents()!=0) {
			throw new IllegalArgumentException("Schedule already exists or students and professor already assigned to course and may have schedule conflict");
		}
		
		courseItem.setCourseSchedule(courseSchedule);
		var response = collegeCoursesRepository.updateTable(courseItem).replace("details", "weeklySchedule");
		return response;
		
	}
	
	public boolean updateEnrolledCountAfterWithdrawal(String courseId) {
		return collegeCoursesRepository.updateEnrolledCountAfterWithdrawal(courseId);
	}
	
	public TransactUpdateItemEnhancedRequest<CourseItem> createCourseIncrementExpression(CourseItem courseItem) {
		
		return collegeCoursesRepository.createIncrementEnrolledStudentCountExpression(courseItem);
	}
	
	public boolean checkProfessorCourseConflict(String professorId, CourseItem course) {
    	List<CourseItem> professorCourses = collegeCoursesRepository.getCoursesByProfessor(professorId);

        // Check for conflicts
        for (CourseItem existingCourse : professorCourses) {
            if (AppUtils.hasScheduleConflict(course.getCourseSchedule(), existingCourse.getCourseSchedule())) {
                return true;
            }
        }
        
        return false;
	}
	
	public Optional<CourseItem> getCourseById(String courseId) {
		
		return collegeCoursesRepository.getCourseById(courseId);
	}
	
	public CourseItem getCourseDetails(String courseId) {
		
		Optional<CourseItem> course = getCourseById(courseId);
		
		if(course.isEmpty()) {
			throw new IllegalArgumentException("Course with ID " + courseId + " does not exist.");
		}
		
		return course.get();
		
	}

}
