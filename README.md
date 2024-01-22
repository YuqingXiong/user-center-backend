# 用户中心管理

## 后端

技术选择：
- Java
- Spring, SpringMVC, SpringBoot
- MySQL, MyBatis, MyBatisPlus


## 前端

This project is initialized with [Ant Design Pro](https://pro.ant.design).

## Environment Prepare

下载依赖：

Install `node_modules`:

```bash
npm install
```

or

```bash
yarn
```

**初始化项目的错误记录：[ErrorRecord.md]()**

还使用了 umi 来快速开发前端页面：

```bash
yarn add @umijs/preset-ui -D
```

项目瘦身：

删除国际化，oneapi(config.ts里的OpenAPI配置也要删除)，测试工具的删除...