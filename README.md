Academic Course Registration System

The academic course registration system is a set of backend APIs that allow users with
different roles to authenticate themselves and perform different tasks. The current allowed roles
are “ADMIN”, “STUDENT” and “PROFESSOR”. The users are assigned roles when they are
created. The backend APIs are deployed via AWS Lambda and exposed via AWS API Gateway.
The data for the backend is stored in a NoSQL database, AWS DynamoDB.


• Only users with the role ADMIN can access the endpoints under “/admin”.
• Only users with the role STUDENT can access the endpoints under “/student”.
• All users should authenticate themselves to access any of the endpoints.
/admin/createcourse
• A course can be created with only the courseId initially and other details can be added later
using the edit or respective field endpoints.
• A courseId should always start with an alphabet and contain only alphanumeric characters.
• The courseSchedule is an array of items and each item should always have a valid day of the
week, start time(HH:mm) and end time(HH:mm).
• The courseSchedule cannot have conflicts or overlaps within the items in the array.
• The courseSchedule can only be between 09:00 and 18:00 and between Monday and Friday.
• The professorId will be checked for scheduling conflicts with other courses the professor is
assigned to.
• The professorId will be rejected if there is no courseSchedule in the request since schedule
conflicts cannot be identified.
• The professorId should always start with an alphabet and contain only alphanumeric
characters.
• The maxStudents count cannot be less than 1.
• The courseId is unique and two courses with the same courseId cannot be created.
/admin/addSchedule/{courseId}
• Same rules for courseSchedule as mentioned in createCourse apply.
• A path parameter courseId is mandatory to identify which course to apply the schedule to.
• If a schedule already exists and students are already enrolled, the new schedule is rejected.
• If a schedule already exists and there are no students enrolled but professor has a conflict
with the new schedule, the schedule is rejected.
/admin/setmaxstudents/{courseId}/{maxStudents}
• The maxStudents value cannot be less than 1.
• If students are enrolled already, the new maxStudents value cannot be less than the count of
enrolled students.
• If the new value is the same as the existing value, no updates are performed.
/admin/assignProfessor/{courseId}/{professorId}?overrideInd=
• If the professor has a schedule conflict with the existing courses, the request is rejected.
• If a professor is already assigned, the new value is not updated.
• An optional overrideInd is passed as a query string parameter to override and set a new
professor provided there is no schedule conflict for the new professor.
/admin/editcourse
• All details except the courseId can be edited.
• Partial updates are performed based on different validations if multiple updates are passed.
• Same rules apply for courseSchedule, maxStudents, professorId as mentioned above in the
other endpoints corresponding to them.
• If no edits are provided, no updates are performed.
/admin/deletecourse/{courseId}
• The courseId is mandatory to delete the course.
• When a course is deleted, all students enrolled in the course are identified and they are all
withdrawn from that course since it doesn’t exist anymore.
/admin/coursesummary?pageSize=&lastEvaluatedKey=
• The course summary brings back all the courses and their corresponding details from the
table.
• The course summary details are paginated and brings back a default count of max 10
always.
• The optional query string parameter pageSize can be used to bring back more than 10
courses.
• The optional query string parameter lastEvaluatedKey can be used to retrieve the courses
page by page for faster access.
/student/registercourse
• The courseId, studentId and studentName are mandatory for a new student registering for
the first course.
• When a student registers, the enrollment count in the course is checked and incremented
first and then the student enrollment detail is updated and both are triggered as a transaction
to ensure first come first serve when multiple students enroll at the same time.
• The studentId should always start with an alphabet and contain only alphanumeric
characters.
• For an existing student, the courseId and studentId are the only expected parameters and
studentName is ignored, if passed.
• If the max capacity has reached for the course, the request is rejected.
• If the course does not have complete details like courseSchedule, professorId and
maxStudents, the request is rejected since the course is not ready for enrollment.
• If the student is already enrolled in the course, the request is rejected.
• If the courseId is invalid, the request is rejected.
/student/withhdrawcourse/{studentId}/{courseId}
• The studentId and courseId path parameters are mandatory fields.
• If the courseId or the studentId is invalid, the request is rejected.
• Once the student is withdrawn from the course, the enrollment count in the course is
decremented to allow new students to register later.
• If all courses enrolled by the student are withdrawn, an empty course list is present for that
student in the table.
