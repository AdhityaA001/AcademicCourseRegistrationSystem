package com.system.academicCourseRegistration.model;

import java.util.List;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaginatedResult<T> {
    private List<T> items;
    private String lastEvaluatedKey;
}