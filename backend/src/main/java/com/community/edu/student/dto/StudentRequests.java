package com.community.edu.student.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public final class StudentRequests {

    private StudentRequests() {
    }

    @Getter
    @Setter
    public static class HomeworkSubmitRequest {
        private String content;
        @Valid
        private List<HomeworkSubmitFile> files = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class HomeworkSubmitFile {
        @NotNull
        private Long fileId;
        @NotBlank
        private String mediaType;
        private Integer sortOrder;
    }

    @Getter
    @Setter
    public static class GroupCreateRequest {
        @NotNull
        private BigDecimal childAge;
        @NotBlank
        private String grade;
        @NotBlank
        private String targetSystem;
        @NotBlank
        private String englishLevel;
        private List<String> preferredTimes = new ArrayList<>();
        private String remark;
        private String contactPhone;
    }

    @Getter
    @Setter
    public static class GroupJoinRequest {
        private String nickname;
        private String phone;
    }
}
