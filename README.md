# 说明文档

## 前端开发

在web_vue 目录下

### 环境

上传的没有node_moudules目录，需要打开命令行在该目录下进行npm install 来安装一些依赖的库
然后用vue ui 直接图形化导入运行即可

## 后端开发

backend 目录

### 环境
- jdk: 1.8  
- java：8
- 包管理：maven
- Springboot：2.6.13  
  - 	阿里云镜像：https://start.aliyun.com
- mysql：8.0.39
- 数据库操作：MyBatis-Plus

## 数据库 

| user | id  | username | password | urank | phone | email |
| ---- | --- | -------- | -------- | ----- | ----- | ----- |

## 模块

### 登陆、注册 模块

springsecurity + jwt 
用户状态用jwt_token进行验证
