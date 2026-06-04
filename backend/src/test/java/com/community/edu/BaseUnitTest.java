package com.community.edu;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;

/**
 * 所有单元测试的基类。
 * 统一配置 DisplayName 生成策略为标准方法名。
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public abstract class BaseUnitTest {
}
