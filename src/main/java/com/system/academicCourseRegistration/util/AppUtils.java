package com.system.academicCourseRegistration.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.academicCourseRegistration.model.CourseSchedule;

public class AppUtils {
	
	private AppUtils() {
		
	}
	
	private static ObjectMapper objectMapper = new ObjectMapper();
	
	public static boolean hasScheduleConflict(List<CourseSchedule> newSchedule, List<CourseSchedule> existingSchedule) {
	    return newSchedule.stream().anyMatch(newEntry ->
	        existingSchedule.stream()
	            .filter(existingEntry -> newEntry.getDayOfWeek().equals(existingEntry.getDayOfWeek()))
	            .anyMatch(existingEntry ->
	                convertToMinutes(newEntry.getStartTime()) < convertToMinutes(existingEntry.getEndTime()) &&
	                convertToMinutes(newEntry.getEndTime()) > convertToMinutes(existingEntry.getStartTime())
	            )
	    );
	}
	
	public static Map<String, String> jsonToMap(String json) {
		var typeRef = new TypeReference<HashMap<String, String>>() {
		};
		return jsonToObject(json, typeRef);
	}
	
	public static <T> T jsonToObject(String json, TypeReference<T> typeRef) {
		if(Objects.isNull(json)) {
			return null;
		}
		try {
			return objectMapper.readValue(json, typeRef);
		} catch (IOException ie) {
			System.out.println("Could not convert json to object"+ie.getMessage());
		} catch (Exception e) {
			System.out.println("Could not convert json to object"+e.getMessage());
		}
		return null;
	}
	
	public static String objectToJson(Object data) {
		if(Objects.isNull(data)) {
			return null;
		}
		
		if(data instanceof String) {
			return data.toString();
		}
		
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		try {
			return objectMapper.writeValueAsString(data);
		} catch (JsonProcessingException e) {
			System.out.println("Could not convert object to json"+e.getMessage());
		}
		
		return null;
	}
	
	private static int convertToMinutes(String time) {
	    String[] parts = time.split(":");
	    return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
	}

}
