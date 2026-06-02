package com.community.edu.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.community.edu.common.context.CampusContextHolder;
import java.util.Set;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    private static final Set<String> TENANT_TABLES = Set.of(
        "edu_teacher",
        "edu_course",
        "edu_class",
        "edu_class_teacher",
        "edu_guardian",
        "edu_student",
        "edu_student_guardian",
        "edu_class_student",
        "edu_class_schedule",
        "res_file",
        "res_category",
        "res_material",
        "res_material_class",
        "ops_activity",
        "ops_activity_registration",
        "fin_order",
        "fin_payment_record",
        "edu_lesson_hour_account",
        "edu_lesson_hour_record",
        "edu_attendance",
        "edu_homework",
        "edu_homework_target",
        "edu_homework_attachment",
        "edu_homework_submission",
        "edu_homework_submission_file",
        "edu_homework_comment",
        "edu_homework_checkin",
        "grp_rule",
        "grp_request",
        "grp_member",
        "grp_trial",
        "grp_trial_feedback"
    );

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                Long campusId = CampusContextHolder.getCampusId();
                return new LongValue(campusId == null ? -1L : campusId);
            }

            @Override
            public String getTenantIdColumn() {
                return "campus_id";
            }

            @Override
            public boolean ignoreTable(String tableName) {
                return !TENANT_TABLES.contains(tableName.toLowerCase());
            }
        }));
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}
