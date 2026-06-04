# 小程序样式全面优化计划

## 摘要

对「小区英语」微信小程序进行全面样式优化。核心问题是全局 `App.vue` 中 `button { line-height: 1; padding: 0; }` 导致全项目约 20+ 个按钮文字无法垂直居中。将通过修复全局样式 + 统一设计系统 + 逐页优化来解决，同时修复登录页文件损坏问题。

---

## 当前状态分析

经代码库全面扫描，发现以下问题：

### 严重问题
1. **`src/pages/login/index.vue` 文件损坏** — 有效内容仅前 334 行，之后有大量重复 `<script setup>` 块污染，总文件膨胀至 1592+ 行
2. **全局按钮居中失效** — `src/App.vue` 全局 button 重置（`line-height:1; padding:0`）破坏了小程序原生按钮的文字居中行为

### 按钮居中缺失清单（共 11 个文件，20+ 个按钮）

| 文件 | 按钮选择器 | 数量 |
|------|-----------|------|
| `student/home.vue` | `.mine`, `.ghost`, `.group-button`, `.quick` | 7 |
| `student/course.vue` | `.tab` | 2 |
| `student/learning.vue` | `.tab` | 4 |
| `student/group/share.vue` | `.join` | 1 |
| `student/group/detail.vue` | `.action` | 2 |
| `student/group/create.vue` | `.submit` | 1 |
| `student/homework/detail.vue` | `.submit`, `.voice` | 2 |
| `teacher/home.vue` | `.round-button` | 1 |
| `guardian/home.vue` | `.mine` | 1 |

### 设计系统缺失
- `src/uni.scss` 已定义设计令牌变量（`$page-bg`, `$ink`, `$green` 等），但所有页面均使用硬编码颜色值，未引用变量
- 各页面样式各自为政，缺乏统一的视觉语言
- 缺少统一的间距、圆角、阴影规范

### 参考范本
- `src/pages/mine/index.vue` — 唯一使用 flexbox 居中的页面（`.logout` 写法正确）
- `src/pages/identity/index.vue` — 使用 flex 对齐，写法正确

---

## 设计方向

### 视觉定调
- **风格**：现代温润的「暖白 + 深绿」品牌调性，克制且专业的移动端 UI
- **色板**：主色 #22624c（深绿）、强调色 #f0b84d（暖金）、背景 #f6f1e8（米白）
- **布局哲学**：少即是多 — 使用分区而非卡片堆叠，强依赖排版层次而非装饰元素
- **交互**：按下缩放反馈、页面入场淡入动画

### 参考的优秀移动端设计模式
- **Linear 风格克制** — 深沉背景、极简分组列表、单色强调
- **Notion 按钮** — `display:flex; align-items:center; justify-content:center` 作为按钮的基础模式
- **微信小程序原生美感** — 大圆角、充足留白、WeChat 绿色为行动召唤色

---

## 拟议变更

### 变更 1：修复 `src/pages/login/index.vue` 文件损坏

**文件**：`src/pages/login/index.vue`
**操作**：重写整个文件，清除重复块，恢复到单组 `<script>` + `<template>` + `<style>` 结构
**内容**：保持之前的登录页设计（品牌区 + 角色选择 + 手机号/OpenID输入 + 微信登录按钮 + 协议），确保所有按钮使用 `display:flex; align-items:center; justify-content:center`

### 变更 2：修复 `src/App.vue` 全局按钮样式

**文件**：`src/App.vue`
**操作**：在全局 `button` 样式中添加居中属性
**变更为**：
```css
button {
  padding: 0;
  margin: 0;
  border: 0;
  background: transparent;
  line-height: 1.2;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}
button::after {
  border: 0;
}
```

**影响**：一次性修复全项目 20+ 个按钮的居中问题，无需逐页修改。

### 变更 3：优化 `src/pages/student/home.vue` 学生首页

**文件**：`src/pages/student/home.vue`
**操作**：
- 优化整体布局，增加呼吸感
- 课程卡片改用更清晰的排版层次
- 底部快捷网格按钮统一尺寸和圆角
- 添加页面入场动画

### 变更 4：优化 `src/pages/student/course.vue` 课程页

**文件**：`src/pages/student/course.vue`
**操作**：
- Tab 切换增加选中态下划线指示器
- 优化课程表/课时记录的列表排版

### 变更 5：优化 `src/pages/student/learning.vue` 学习中心

**文件**：`src/pages/student/learning.vue`
**操作**：
- 筛选 Tab 增加视觉层次
- 作业卡片优化信息密度

### 变更 6：优化 `src/pages/teacher/home.vue` 老师工作台

**文件**：`src/pages/teacher/home.vue`
**操作**：
- 按钮居中对齐
- 优化整体布局间距

### 变更 7：优化 `src/pages/guardian/home.vue` 家长中心

**文件**：`src/pages/guardian/home.vue`
**操作**：
- 按钮居中对齐
- 优化整体布局间距

### 变更 8：优化其他子页面样式

**文件**：
- `src/pages/student/group/share.vue`
- `src/pages/student/group/detail.vue`
- `src/pages/student/group/create.vue`
- `src/pages/student/homework/detail.vue`

**操作**：统一按钮样式规范，确保所有按钮 `display: flex; align-items: center; justify-content: center;`

### 变更 9：全局样式补充 — 页面入场动画

**文件**：`src/App.vue`
**操作**：添加全局页面过渡动画 keyframe，使所有页面切换有淡入效果

---

## 假设与决策

1. **不改变业务逻辑** — 仅修改 CSS 样式和模板中按钮的结构属性，不改变任何 JS 逻辑
2. **保持 uni-app + Vue3 技术栈** — 使用 rpx 单位、scoped style、微信小程序原生 `<button>` 组件
3. **颜色使用全局已定义的 SCSS 变量**（通过 CSS 变量桥接实现复用）
4. **不做大改** — 保持现有页面结构和组件层级，仅做样式增强
5. **优先修复全局 button** — 这是最高效的方案，一次修复覆盖全项目

---

## 验证步骤

1. 在微信开发者工具中预览每个页面，确认所有按钮文字居中
2. 检查登录页是否正常显示且文件内容无重复污染
3. 测试角色切换按钮点击响应正常
4. 检查页面入场动画是否流畅，不干扰交互
5. 对比修复前后截图，确认视觉效果提升
