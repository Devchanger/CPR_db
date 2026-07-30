package com.cpr_db.cpr_db.controller;

import com.cpr_db.cpr_db.common.ApiResponse;
import com.cpr_db.cpr_db.entity.Student;
import com.cpr_db.cpr_db.service.StudentService;
import org.springframework.http.ResponseEntity;
<<<<<<< HEAD
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
=======
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
>>>>>>> 193e2be (feat: complete all missing backend API endpoints)

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
<<<<<<< HEAD
    public ResponseEntity<ApiResponse<List<Student>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(studentService.getAll()));
=======
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStudentList(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(ApiResponse.success(studentService.getStudentList(keyword, status, page, pageSize)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Student>> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(studentService.getStudentById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Student>> createStudent(@RequestBody Student body) {
        return ResponseEntity.ok(ApiResponse.success(studentService.createStudent(body), "created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Student>> updateStudent(@PathVariable Long id, @RequestBody Student body) {
        return ResponseEntity.ok(ApiResponse.success(studentService.updateStudent(id, body), "updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.success(null, "deleted"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Student>> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String status = body.get("status") == null ? null : body.get("status").toString();
        return ResponseEntity.ok(ApiResponse.success(studentService.updateStudentStatus(id, status), "updated"));
>>>>>>> 193e2be (feat: complete all missing backend API endpoints)
    }
}
