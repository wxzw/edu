package com.community.edu.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;

/**
 * MyBatis 元对象处理器。自动填充创建人、更新人等字段。
 */
import com.community.edu.common.context.CurrentUser;
import com.community.edu.common.context.CurrentUserHolder;
import java.time.OffsetDateTime;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

@Component
public class MybatisMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        OffsetDateTime now = OffsetDateTime.now();
        Long userId = currentUserId();
        strictInsertFill(metaObject, "createdAt", OffsetDateTime.class, now);
        strictInsertFill(metaObject, "updatedAt", OffsetDateTime.class, now);
        strictInsertFill(metaObject, "createdBy", Long.class, userId);
        strictInsertFill(metaObject, "updatedBy", Long.class, userId);
        setDefault(metaObject, "deleted", 0);
        setDefault(metaObject, "version", 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        OffsetDateTime now = OffsetDateTime.now();
        Long userId = currentUserId();
        strictUpdateFill(metaObject, "updatedAt", OffsetDateTime.class, now);
        strictUpdateFill(metaObject, "updatedBy", Long.class, userId);
    }

    private Long currentUserId() {
        CurrentUser currentUser = CurrentUserHolder.getOrNull();
        return currentUser == null ? null : currentUser.getUserId();
    }

    private void setDefault(MetaObject metaObject, String fieldName, Object value) {
        if (metaObject.hasGetter(fieldName) && getFieldValByName(fieldName, metaObject) == null) {
            setFieldValByName(fieldName, value, metaObject);
        }
    }
}
