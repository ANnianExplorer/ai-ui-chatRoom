<div align="center">

# FenDou Chat

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-blue.svg" alt="Java 17" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.5.3-brightgreen.svg" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/Netty-4.1-green.svg" alt="Netty" />
  <img src="https://img.shields.io/badge/Vue-3.5-4FC08D.svg" alt="Vue 3" />
  <img src="https://img.shields.io/badge/Vite-5.4-646CFF.svg" alt="Vite" />
  <img src="https://img.shields.io/badge/Spring_AI-1.1.0-blueviolet.svg" alt="Spring AI" />
  <img src="https://img.shields.io/badge/MySQL-8.0-orange.svg" alt="MySQL" />
  <img src="https://img.shields.io/badge/Redis-6.0-red.svg" alt="Redis" />
</p>

[English](#) | [简体中文](README.md)

**面向现代团队与企业的高性能、AI 增强型实时通讯引擎**

<br />

</div>

## 核心亮点

| 模块 | 现有能力 |
| --- | --- |
| **实时通讯** | 基于 Netty 构建的高性能 WebSocket 服务，支持单聊、群聊，消息毫秒级送达、离线消息同步、已读/未读状态管理 |
| **AI 智能助手** | 原生集成 Spring AI，无缝对接 OpenAI、智谱清言、Ollama，支持多轮对话上下文管理与内置角色提示词切换 |
| **社交与群组** | 完善的好友申请、审批、备注、分组；群组创建、邀请加群、群主权限管理与成员管理机制 |
| **文件与多媒体** | 支持图片与文件上传、本地存储策略 (Local Storage)，大文件切片与秒传逻辑扩展能力 |
| **互动与体验** | 聊天内单选/多选互动投票、消息一键收藏、动态表情包面板 (vue3-emoji-picker) |
| **数据与后台** | 基于 ECharts 的数据大盘展示，涵盖用户管理、群组管理、消息审计、违规词过滤及系统全方位设置 |

## 快速体验

| 平台 | 演示地址 | 账号密码 |
| --- | --- | --- |
| **用户端** | `http://localhost:5173` | test / 123456 (示例) |
| **管理后台** | `http://localhost:5173/admin` | admin / admin123 (示例) |

## 项目源码

| 项目模块 | 本地路径 | 说明 |
| --- | --- | --- |
| **后端服务** | `fendou-chat` | 核心业务逻辑与 WebSocket 通信 |
| **前端应用** | `fendou-chat-web` | 包含用户端与管理后台界面 |

## 技术架构

**后端架构**
- **核心框架**: Spring Boot 3.5.3 + Java 17
- **通信框架**: Netty (提供高性能的 WebSocket 握手与长连接维护)
- **AI 架构**: Spring AI (统一的 LLM 抽象层，包含对话工具支持)
- **数据存储**: MySQL 8.0 + MyBatis Plus
- **缓存体系**: Redis (Token 管理、验证码、在线状态缓存)
- **安全与工具**: JWT 鉴权体系、Hutool 工具集、FastJSON

**前端架构**
- **核心框架**: Vue 3 (Composition API) + Vite 5
- **状态管理**: Pinia (响应式全局状态，包含用户、主题与 WebSocket)
- **UI 组件库**: Element Plus
- **路由控制**: Vue Router 4
- **数据可视化**: ECharts 5 (后台数据统计大屏)
- **工程化**: Sass (模块化与主题变量隔离)、明暗主题 (Dark Mode) 无缝切换

## 更新日志 (Changelog)

### 1.0.0 Released (2026-04-29)

本次发布专注于**基础通信稳定性、AI 深度整合以及前后端架构升级**。主要更新包括：

**架构全面升级**
- 后端已全面升级至 `Spring Boot 3.5.3` 和 `Java 17`。
- 显著降低了系统的内存占用并提升了并发处理能力，更适配企业级生产环境部署。
- 前端升级至 `Vite 5`，支持秒级冷启动。

**AI 大模型整合**
- 核心 AI 模块重构，采用 `Spring AI` 标准化接口。
- 新增对 `Ollama` 本地部署模型的支持。
- 新增对话工具 (MessageAgentTool)，提升复杂问题解析的准确度。

**全平台交互体验优化**
- 聊天界面增加动态表情包支持。
- 全面适配并优化深色模式 (Dark Mode)。
- 遵循专业 UI/UX 规范，去除过度装饰，提升界面的专业度和可读性。

---
<div align="center">
  <p>采用 MIT 开源协议 | 欢迎提交 Pull Request 共同完善</p>
</div>
