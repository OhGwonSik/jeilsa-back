# 제일택배사 시스템 재구축 - 백엔드

소규모 택배사 운영 시스템 백엔드. ERD 설계부터 개발, 배포, 운영까지 전 과정 단독 담당.

---

## 기술 스택

- **Backend**: Java 17, Spring Boot 3.4.7, MyBatis, Spring Security (JWT)
- **DB**: PostgreSQL
- **Build**: Gradle
- **Infra**: Docker, Jenkins
- **기타**: Jasper Reports, JXLS, Thymeleaf, Logback

---

## 프로젝트 개요

| 항목 | 내용 |
|------|------|
| 기간 | 2025.07 ~ 2026.02 (약 7개월) |
| 팀 구성 | 백엔드 본인 단독 + 프론트 1인 |
| 도메인 | 운송정보 입력 / 정산 / 청구서 관리 |

**비즈니스 프로세스**
운송 요청 접수 → 운송정보 등록 → 배송 진행 → 월말 정산 → 업체별 청구서 발행 및 메일 발송

---

## 요구사항

- Java 17
- Gradle (또는 내장된 gradlew 사용)
- PostgreSQL

---

## 실행 방법

```bash
git clone https://github.com/OhGwonSik/jeilsa-back.git
cd jeilsa-back

# application.properties.example을 참고하여 application.properties 생성 후 값 입력

./gradlew bootRun
```

---

## 주요 기능

### 인증/인가
- JWT AccessToken / RefreshToken 이중 구조 직접 설계 및 구현
- Spring Security 기반 권한 체크 (`@PreAuthorize + permissionHelper`)
- TransactionAOP - 읽기/쓰기 트랜잭션 분리 (readOnly, REQUIRES_NEW)

### 운송 기능
- 운송정보 입력 / 배송코스 관리 / 운송장 선등록 / 화주별 정산
- 배송체크 엑셀 다운로드

### 정산/청구 기능
- 청구서 등록 / 재계산 / PDF 출력 (Jasper Reports)
- 세금계산서 엑셀 다운로드 (JXLS)
- 업체별 메일 발송 (Thymeleaf 템플릿, 0원 업체 SKIP, 발송 이력 기록)

### 운영 서버
- Docker + Jenkins CI/CD 파이프라인 구성
- Logback rolling policy (로그/컨테이너 용량 관리)
- 운영/개발/로컬 환경 프로파일 분리

---

## 트러블슈팅

### 정산 쿼리 성능 저하 (5~10분 → 1~3분)
- **문제**: 개발 환경과 실 데이터 양 차이로 오픈 후 성능 저하 발생
- **원인 파악**: EXPLAIN 실행계획 분석으로 병목 구간 파악
- **해결**:
  - 1단계: 인덱스 추가 (운송/정산/회사 테이블)
  - 2단계: CTE MATERIALIZED 적용 (반복 계산 제거), DISTINCT ON으로 중간 결과물 축소, filtered_transport CTE로 연산 범위 제한
- **결과**: 5~10분 → 1~3분으로 단축

### 운영 서버 다운
- **문제**: 운영 서버 갑자기 다운, 인프라 지식 부족한 상태
- **원인 파악**: 도커 컨테이너 및 로그 용량 초과 확인
- **해결**: Logback rolling policy 설정으로 로그 용량 관리
- **결과**: 이후 동일 장애 재발 없음

### 청구서 메일 자동 발송 구조 개선
- **문제**: 청구서 등록 시 자동 발송으로 설계했으나 재정산 케이스 발생
- **해결**: 재정산 완료 후 수동 버튼 발송 방식으로 변경 제안 및 적용
- **결과**: 재정산 후 잘못된 청구서 발송 문제 방지

---

## 환경 설정

민감 정보(DB 접속 정보, JWT 시크릿키 등)는 `application.properties`에 관리되며 별도 제공되지 않습니다.

`application.properties.example` 파일을 참고하여 환경에 맞게 설정하세요.

```properties
# DB
spring.datasource.hikari.jdbc-url=jdbc:postgresql://localhost:5432/jeilsa
spring.datasource.hikari.username=
spring.datasource.hikari.password=

# JWT
jwt.accessToken=
jwt.refreshToken=

# Mail
spring.mail.host=
spring.mail.username=
spring.mail.password=
```
