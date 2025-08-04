# Virtual Thread를 활용한 스프링 부트 서비스 구현

## 구성

- Spring Boot 3.4.5
- Spring Config Server
- Spring Web
- Virtual Thread
- Spring Actuator -> 모니터링 까지

- mysql
- kafka
- redis

## 개발환경

- JDK24
- Cursor AI
- Spring Boot Extension

## 프로젝트 구성 및 기능 구현

### 회원 서버

1. DDD기반 + CQRS
1. MDC 컨텍스트
1. 트랜잭션 아웃박스 패턴 구현
1. 1,2차 캐싱 처리
1. actuator -> micrometer -> 프로메테우스 -> grafana
1. git commit id
1. 컨테이너 라이징
1. spring jdbc
1. spring jpa
1. 서킷브레이커 -> 일정 수치 -> 429
1. mysql connector 9.0
1. distributed tracing(ots), metrics, logs (tempo, loki?)

### config server

### 게이트 웨이 & 서비스 디스커버러링

1. cloud gateway + routing
1. k3s 서비스 레지스트리?

1. 로그인 구현 : 넷플릭스 passport라는 id 토큰 (사용자 기기정보 + 유저정보)

### 기능

- 회원 등록, 조회, 수정, 삭제
- 회원 그룹, 등급 조회, 수정, 삭제
- 회원 목록 조회 : 회원 전체 항목 + 그룹 수, 등급 수
  - 검색 조건 : 그룹이 A 조회, 등급이 VIP 조회, 그룹수가 3 이상 조회
- 회원 히스토리
- oauth server + google 로그인?
- 일별, 월별 통계 조회 쿼리
- 등록, 수정시 이벤트 발행 -> IO 발생
- rest api와 grpc 동일한 조회성 api를 만들자. 직렬화, 통신 과정에서 네트워크, cpu, 메모리 지표 확인
- 단건 조회시 1차 (caffeine) 2차 레디스, 캐시 전략 설정할것
- api콜에 따른 프로메테우스로 api호출수 전송 ()
- TestCase 작성
- 톰캣 쓰레드 10개정도로 고정, oha 로 부하테스트

## k3s 실습
