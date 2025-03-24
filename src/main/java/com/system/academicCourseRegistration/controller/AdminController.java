package com.system.academicCourseRegistration.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.system.academicCourseRegistration.model.CourseItem;
import com.system.academicCourseRegistration.model.CourseSchedule;
import com.system.academicCourseRegistration.model.CourseSummaryDetails;
import com.system.academicCourseRegistration.model.PaginatedResult;
import com.system.academicCourseRegistration.service.CollegeCoursesService;
import com.system.academicCourseRegistration.service.ReportingService;

@RestController
@RequestMapping("/admin")
public class AdminController {
	
	private final CollegeCoursesService collegeCoursesService;
	private final ReportingService reportingService;

    public AdminController(CollegeCoursesService collegeCoursesService, ReportingService reportingService) {
        this.collegeCoursesService = collegeCoursesService;
        this.reportingService = reportingService;
    }
	
	@PostMapping("/createcourse")
	@ResponseStatus(HttpStatus.OK)
	public Map<String,Object> createCourse(@Validated @RequestBody CourseItem courseItem) {		
		var response = collegeCoursesService.createCourse(courseItem);
        return buildResponse(response);	
	}
	
	@PostMapping("/editcourse")
	@ResponseStatus(HttpStatus.OK)
	public Map<String,Object> editCourse(@Validated @RequestBody CourseItem courseItem) {
		var response = collegeCoursesService.editCourse(courseItem);
		return buildResponse(response);
	}
	
	@PostMapping("/addschedule/{courseId}")
	@ResponseStatus(HttpStatus.OK)
	public Map<String,Object> addSchedule(@RequestBody List<CourseSchedule> request, @PathVariable String courseId) {
		var response = collegeCoursesService.setWeeklySchedule(courseId, request);
		return buildResponse(response);
	}
	
	@DeleteMapping("/deletecourse/{courseId}")
	@ResponseStatus(HttpStatus.OK)
	public Map<String,Object> deleteCourse(@PathVariable String courseId) {
		var response = collegeCoursesService.deleteCourse(courseId);
		return buildResponse(response);
	}
	
	@PostMapping("/assignprofessor/{courseId}/{professorId}")
	@ResponseStatus(HttpStatus.OK)
	public Map<String,Object> assignProfessor(@PathVariable String courseId, @PathVariable String professorId, @RequestParam(defaultValue = "false") boolean overrideInd) {
		var response = collegeCoursesService.setProfessor(professorId, courseId, overrideInd);
		return buildResponse(response);
	}
	
	@PostMapping("setmaxstudents/{courseId}/{maxStudents}")
	@ResponseStatus(HttpStatus.OK)
	public Map<String,Object> setMaxStudents(@PathVariable String courseId, @PathVariable int maxStudents) {
		var response = collegeCoursesService.setMaxStudents(courseId, maxStudents);
		return buildResponse(response);
	}
	
	@GetMapping("/coursesummary")
	public ResponseEntity<PaginatedResult<CourseSummaryDetails>> getCourseSummary(@RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String lastEvaluatedKey) {
		PaginatedResult<CourseSummaryDetails> paginatedCourses =
	            reportingService.completeCourseSummary(pageSize, lastEvaluatedKey);

        if (paginatedCourses.getItems().isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content if no results
        }

        return ResponseEntity.ok(paginatedCourses);
	}
	
	public Map<String, Object> buildResponse(Object message) {
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("Status", "Success");
		response.put("Details", message);
		
		return response;
	}

}
