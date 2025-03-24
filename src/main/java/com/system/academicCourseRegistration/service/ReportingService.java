package com.system.academicCourseRegistration.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.system.academicCourseRegistration.model.CourseItem;
import com.system.academicCourseRegistration.model.CourseSummaryDetails;
import com.system.academicCourseRegistration.model.PaginatedResult;
import com.system.academicCourseRegistration.repository.CollegeCoursesRepository;
import com.system.academicCourseRegistration.repository.StudentEnrollmentDetailsRepository;

@Service
public class ReportingService {
	
	private final CollegeCoursesRepository collegeCoursesRepository;

	public ReportingService(CollegeCoursesRepository collegeCoursesRepository) {
		this.collegeCoursesRepository = collegeCoursesRepository;
	}
	
	public PaginatedResult<CourseSummaryDetails> completeCourseSummary(int pageSize, String lastEvaluatedKey) {
		
		PaginatedResult<CourseItem> paginatedCourses = collegeCoursesRepository.getCourseSummary(pageSize, lastEvaluatedKey);

        List<CourseSummaryDetails> courseSummaries = paginatedCourses.getItems().stream()
            .map(course -> CourseSummaryDetails.builder()
                .courseId(course.getCourseId())
                .courseName(course.getCourseName())
                .courseSchedule(course.getCourseSchedule())
                .professor(course.getProfessorId() != null ? course.getProfessorId() : "TBD")
                .enrolledStudentsCount(course.getEnrolledStudents()  != null ? course.getEnrolledStudents() : 0)
                .maxStudentsCount(course.getMaxStudents() != null ? course.getMaxStudents() : 0)
                .build())
            .collect(Collectors.toList());

        return new PaginatedResult<>(courseSummaries, paginatedCourses.getLastEvaluatedKey());
	}

}
