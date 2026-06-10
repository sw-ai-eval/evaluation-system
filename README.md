<div align="center">

# 인사평가 관리 시스템

### 성과 · 역량 · 다면평가 기반 인사평가 관리 시스템

기업의 인사평가 프로세스를 디지털화하여 평가의 공정성과 효율성을 향상시키기 위한 HR Evaluation Platform

<br>

![Java](https://img.shields.io/badge/Java-1E2535?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-1E2535?style=for-the-badge&logo=springboot)
![Spring Security](https://img.shields.io/badge/Security-2A3447?style=for-the-badge)
![MyBatis](https://img.shields.io/badge/MyBatis-2A3447?style=for-the-badge)
![MSSQL](https://img.shields.io/badge/MSSQL-1F6FEB?style=for-the-badge&logo=microsoftsqlserver)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-2A3447?style=for-the-badge&logo=thymeleaf)


</div>

---

# 📌 Table of Contents

* [Project Overview](#project-overview)
* [Key Features](#key-features)
* [Tech Stack](#tech-stack)
* [System Architecture](#system-architecture)
* [Database Design](#database-design)
* [Core Implementation](#core-implementation)
* [Future Improvements](#future-improvements)

---

# Project Overview

사내 인사평가 프로세스를 디지털화하기 위한 **성과평가, 역량평가, 다면평가 기반 HR 평가 시스템**입니다.

평가 대상자, 평가자, 평가 항목 및 등급 체계를 통합 관리하며 평가 결과를 시각화하여 최종 인사결정에 활용할 수 있도록 설계하였습니다.

### 🎯 목표

* 평가 프로세스 표준화
* 평가 데이터 통합 관리
* 평가 결과 시각화
* 역할 기반 접근 제어
* 평가 결과 자동 산출

---

# Key Features

## 🔐 Authentication & Authorization

* Spring Security 기반 인증
* 사번 / 비밀번호 로그인
* 로그인 실패 5회 계정 잠금
* 관리자 비밀번호 초기화
* 퇴사자 로그인 제한
* 역할 기반 권한 제어

## 👥 Department Management &  Employee Management 

* 부서 CRUD
* 부서 코드 계층적 구조
* 부서장 지정
* 사원 CRUD
* 사번 자동 생성
* 재직 상태 관리

## 📊 Performance Evaluation

### 평가 요소

* 평가 유형 CRUD
* 유형에 따른 문항 CRUD
* 조직별 가중치/등급 설정
* 부서장 등급 설정

### 평가자 매핑

* 사원 정보에 따른 자동 매핑
* 일반 평가(0차: 부서원 본인, 1차: 부서장, 최종 등급 확정자: 임원)
* 다면평가(1차 평가: 부서원이 부서장 평가, 최종 등급 확정자: 임원)


### 성과평가

* 자기평가
* 부서장 평가
* 평가 순서 제어
* 점수 자동 계산

### 역량평가

* 자기평가
* 부서장 평가
* 평가 순서 제어
* 역량 등급 가이드
* 가중치 적용

### 다면평가

* 평가 순서 제어(성과/역량 본인 평가 후 다면평가 진행)
* 평균 점수 계산
* 부서장의 본인 결과 시각화(레이더 차트)
* 기업 전처 부서장 결과 시각화(레이더 차트)

## 📅 면담 관리

* 면담 일정 등록
* 면담 대상자 조회
* 평가 유형의 면담 카테고리 자동 연결

## 📈 대시보드 & 공지사항

* 평가 진행률 확인
* 예정된 면담 일정 확인
* 나의 할 일 확인
* 공지사항 확인
* 평가 진척률 확인(관리자 전용)

---

# Tech Stack

## Backend

| Tech            | Description                    |
| --------------- | ------------------------------ |
| Spring Boot     | Backend Framework              |
| Spring Security | Authentication / Authorization |
| MyBatis         | ORM Framework                  |
| MSSQL           | Database                       |
| Maven           | Build Tool                     |

## Frontend

| Tech       | Description           |
| ---------- | --------------------- |
| Thymeleaf  | Server Side Rendering |
| HTML5      | Markup                |
| CSS3       | Styling               |
| JavaScript | Client Logic          |

## Collaboration

| Tool   |
| ------ |
| Git    |
| GitHub |

---

# System Architecture

<p align="center">
  <img src="https://github.com/user-attachments/assets/6cd9567d-3ded-44f2-b476-7e279fb6cc25" width="500">
</p>

# Database Design

### ERD

https://www.erdcloud.com/d/GXkcZEbxARZXLqWX2

### Main Tables

* Employee
* Department
* Evaluation_Item
* Evaluation_Result
* Eval_Target_Mapping
* Interview

---

# Core Implementation

## 1. 동시성을 고려한 부서코드, 사번 자동 생성

* 중복 사번 방지(DB SEQUENCE, Pessimistic Locking)
* 트랜잭션 처리
* 예외 상황 대응

## 2. 평가 프로세스 상태 제어

```text
SELF_EVALUATION
      ↓
MANAGER_EVALUATION
      ↓
FINAL_CONFIRM
```

* 단계별 접근 제한
* 평가 순서 보장

## 3. 최종 등급 자동 산출

* 성과평가
* 역량평가
* 다면평가(부서장)

가중치를 적용하여 최종 등급 계산

```text
Final Score
= Performance * 50%
+ Competency * 30%
+ Peer Review * 20%
```

## 4. 등급 확정 처리

* 트랜잭션 기반 처리
* 임원의 수동 수정 지원
* 확정 후 수정 불가
* 자신의 피평가자 결과 조회
* 부서장의 소속된 부서원 결과 조회

---

# 🚀 Getting Started

## Clone Repository

```bash
git clone https://github.com/sw-ai-eval/evaluation-system.git
cd evaluation-system
```

## Build 

```bash
mvn clean install
```

## Run

```bash
mvn spring-boot:run
```

---

# Future Improvements

* CI/CD 자동 배포 (GitHub Actions / Jenkins)
* 클라우드 배포
* 이메일 알림 기능
* 엑셀 업로드를 통한 평가자 관리
* 조직도 시각화
* REST API 전환
* React Frontend 적용

---

<div align="center">

### ⭐ 서경대학교 2026년 1학기 기업연계 소프트인재양상교육 프로젝트로 개발한 서비스입니다.

</div>
