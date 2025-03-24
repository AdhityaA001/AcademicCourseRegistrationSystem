package com.system.academicCourseRegistration.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.system.academicCourseRegistration.service.StudentEnrollmentService;
import com.system.academicCourseRegistration.util.AppUtils;

@RestController
@RequestMapping("/student")
public class StudentController {
	
	private final StudentEnrollmentService studentEnrollmentService;
	
	public StudentController(StudentEnrollmentService studentEnrollmentService) {
        this.studentEnrollmentService = studentEnrollmentService;
    }
	
	@PostMapping("/registercourse")
	@ResponseStatus(HttpStatus.OK)
	public Map<String,Object> registerCourse(@RequestBody String request) {
		var requestMap = AppUtils.jsonToMap(request);
		var response = studentEnrollmentService.registerCourse(requestMap.get("courseId"), requestMap.get("studentId"), requestMap.get("studentName"));
		return buildResponse(response);	
	}
	
	@DeleteMapping("/withdrawcourse/{studentId}/{courseId}")
	@ResponseStatus(HttpStatus.OK)
	public Map<String,Object> withdrawCourse(@PathVariable String studentId, @PathVariable String courseId) {
		var response = studentEnrollmentService.withdrawCourse(studentId, courseId);
		return buildResponse(response);
	}
	
	public Map<String, Object> buildResponse(String message) {
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("Status", "Success");
		response.put("Details", message);
		
		return response;
	}
	
}
