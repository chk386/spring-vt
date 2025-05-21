

👜Pre-condition Check:
함수나 메서드가 올바르게 작동하기 위해 필요한 변수의 상태나 겂의 범위를 가지고 있는지 검사

🪢Runtime Error Check:
Runtime Error 가능성이 있는 코드를 검사하며, 기타 잠재적 위험을 확인

🔍Post-condition Check:
함수나 메서드가 올바르게 작동하는 것을 확인하고, 결과를 검사

🔍Security Issue:
코드가 심각한 보안 결함을 가진 모듈을 사용하거나 보안 취약점을 포함하고 있는지 검사

🔍개인정보 & 암호키 & 토큰 Check:
이름, 전화번호, 주민등록번호, 주소, 이메일등 개인정보가 존재하거나 암호화키, 토큰키등이 평문으로 저장하고 있는지 검사

🔍Optimization Check:
코드가 최적화되어 있는지 검사

아래는 코드 리뷰 예제입니다. 아래와 같은 형태로 답변해주세요.

```java
// 원본 코드
public List<Customer> getCustomersByStatus(String status) {
    List<Customer> result = new ArrayList<>();
    
    try {
        connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        String query = "SELECT * FROM customers WHERE status = '" + status + "'";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        
        while (rs.next()) {
            Customer customer = new Customer();
            customer.setId(rs.getInt("id"));
            customer.setName(rs.getString("name"));
            customer.setEmail(rs.getString("email"));
            customer.setStatus(rs.getString("status"));
            result.add(customer);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    
    return result;
}
```

**파일명 1: 메서드 선언**
```java
/**
 * 특정 상태의 고객 목록을 조회합니다.
 * @param status 조회할 고객 상태
 * @return 조회된 고객 목록, 오류 발생 시 빈 리스트 반환
 * @throws IllegalArgumentException status가 null이거나 빈 문자열인 경우
 */
public List<Customer> getCustomersByStatus(String status) {
```
👜Pre-condition Check:
- 메서드에 JavaDoc 문서화를 추가하여 매개변수, 반환값, 예외에 대한 설명을 추가했습니다.

**라인 2-3: 변수 초기화 및 사전 조건 검사**
```java
    List<Customer> result = new ArrayList<>();
    
    if (status == null || status.trim().isEmpty()) {
        throw new IllegalArgumentException("Status cannot be null or empty");
    }
```
👜Pre-condition Check:
- status 파라미터의 유효성을 검사하는 코드를 추가했습니다.
- null이나 빈 문자열인 경우 명시적으로 예외를 발생시킵니다.

**라인 4-7: DB 연결 및 쿼리 준비**
```java
    try (Connection connection = dataSource.getConnection();
         PreparedStatement stmt = connection.prepareStatement(
             "SELECT id, name, email, status FROM customers WHERE status = ?")) {
        
        stmt.setString(1, status);
```
🔍Security Issue:
- 하드코딩된 DB 연결 정보 대신 dataSource를 사용하여 연결을 관리합니다.
- try-with-resources 문법을 사용하여 자원 누수를 방지합니다.
- SQL Injection 취약점을 방지하기 위해 PreparedStatement를 사용합니다.
- SELECT *를 피하고 필요한 컬럼만 명시적으로 선택합니다.

**라인 8-19: 결과 처리**
```java
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Customer customer = new Customer();
                customer.setId(rs.getInt("id"));
                customer.setName(rs.getString("name"));
                
                String email = rs.getString("email");
                if (isValidEmail(email)) {
                    customer.setEmail(email);
                } else {
                    logger.warn("Invalid email format for customer ID: " + rs.getInt("id"));
                }
                
                customer.setStatus(rs.getString("status"));
                result.add(customer);
            }
        }
```
🔍Post-condition Check:
- ResultSet도 try-with-resources로 관리합니다.
- 이메일 값의 유효성을 검사하는 로직을 추가했습니다.
- 유효하지 않은 이메일이 발견되면 경고 로그를 남깁니다.

**라인 20-24: 예외 처리**
```java
    } catch (SQLException e) {
        logger.error("Database error while fetching customers by status", e);
    } catch (Exception e) {
        logger.error("Unexpected error while fetching customers by status", e);
    }
```

🪢Runtime Error Check:
- 예외를 단순히 출력만 하지 않고 로깅 시스템을 통해 기록합니다.
- SQL 예외와 일반 예외를 구분하여 처리합니다.
- 각 예외 유형에 맞는 메시지를 포함하여 디버깅을 용이하게 합니다.

**라인 26-31: 유틸리티 메서드**
```java
    return result;
}

private boolean isValidEmail(String email) {
    // 이메일 유효성 검사 로직
    return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
}
```
🔍Post-condition Check:
- 이메일 유효성 검사를 위한 별도의 도우미 메서드를 추가했습니다.
- 이메일 형식의 기본적인 검증을 수행합니다.

### 전체적인 개선 요약:
1. 🔍Security Issue: SQL Injection 취약점 제거 (PreparedStatement 사용)
2. 🪢Runtime Error Check: 자원 누수 방지 (try-with-resources 사용)
3. 👜Pre-condition Check: 입력 매개변수 유효성 검사 추가
4. 🔍Post-condition Check: 이메일 데이터 유효성 검사 추가
5. 🔍개인정보 & 암호키 & 토큰 Check: 하드코딩된 DB 연결 정보 제거
6. 🔍Optimization Check: 필요한 컬럼만 조회하도록 쿼리 최적화
7. 🪢Runtime Error Check: 예외 처리 개선 (로깅 사용 및 예외 구분)
8. 📝 문서화: 메서드에 JavaDoc 추가





아래는 github diff야. 위와 같은 형태로 코드리뷰해줘

diff --git "a/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/BtrackApplication.java" "b/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/BtrackApplication.java"
new file mode 100644
index 0000000..54d7b22
--- /dev/null
+++ "b/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/BtrackApplication.java"
@@ -0,0 +1,15 @@
+package cau.ict.btrack;
+
+import org.springframework.boot.SpringApplication;
+import org.springframework.boot.autoconfigure.SpringBootApplication;
+import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
+
+@SpringBootApplication
+@EnableJpaAuditing
+public class BtrackApplication {
+
+    public static void main(String[] args) {
+        SpringApplication.run(BtrackApplication.class, args);
+    }
+
+}
diff --git "a/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/Book.java" "b/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/Book.java"
new file mode 100644
index 0000000..3b7f727
--- /dev/null
+++ "b/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/Book.java"
@@ -0,0 +1,41 @@
+package cau.ict.btrack.domain;
+
+import cau.ict.btrack.domain.common.BaseEntity;
+import cau.ict.btrack.domain.mapping.Likes;
+import cau.ict.btrack.domain.mapping.Rental;
+import cau.ict.btrack.domain.mapping.BookHasTag;
+import jakarta.persistence.*;
+import lombok.*;
+import java.util.List;
+import java.util.ArrayList;
+
+@Entity
+@Getter
+@Builder
+@NoArgsConstructor(access = AccessLevel.PROTECTED)
+@AllArgsConstructor
+
+public class Book extends BaseEntity{
+    @Id
+    @GeneratedValue(strategy = GenerationType.IDENTITY)
+    private Long id;
+
+    @Column(length=30,nullable=false)
+    private String title;
+
+    @Column(length=30,nullable=false)
+    private String description;
+
+    @ManyToOne(fetch = FetchType.LAZY)
+    @JoinColumn(name = "category_id")
+    private BookCategory category;
+
+    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
+    private List<BookHasTag> bookHasTags = new ArrayList<>();
+
+    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
+    private List<Likes> likes = new ArrayList<>();
+
+    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
+    private List<Rental> rental = new ArrayList<>();
+}
diff --git "a/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/BookCategory.java" "b/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/BookCategory.java"
new file mode 100644
index 0000000..f24a1c9
--- /dev/null
+++ "b/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/BookCategory.java"
@@ -0,0 +1,20 @@
+package cau.ict.btrack.domain;
+
+import jakarta.persistence.*;
+import lombok.*;
+import cau.ict.btrack.domain.common.BaseEntity;
+
+@Entity
+@Getter
+@Builder
+@NoArgsConstructor(access = AccessLevel.PROTECTED)
+@AllArgsConstructor
+
+public class BookCategory extends BaseEntity {
+    @Id
+    @GeneratedValue(strategy = GenerationType.IDENTITY)
+    private Long id;
+
+    @Column(length=20,nullable = false)
+    private String name;
+}
diff --git "a/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/HasTag.java" "b/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/HasTag.java"
new file mode 100644
index 0000000..fc32062
--- /dev/null
+++ "b/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/HasTag.java"
@@ -0,0 +1,27 @@
+package cau.ict.btrack.domain;
+
+import cau.ict.btrack.domain.common.BaseEntity;
+import cau.ict.btrack.domain.mapping.BookHasTag;
+import jakarta.persistence.*;
+import lombok.*;
+
+import java.util.ArrayList;
+import java.util.List;
+
+@Entity
+@Getter
+@Builder
+@NoArgsConstructor(access = AccessLevel.PROTECTED)
+@AllArgsConstructor
+
+public class HasTag extends BaseEntity {
+    @Id
+    @GeneratedValue(strategy = GenerationType.IDENTITY)
+    private Long id;
+
+    @Column(length=20,nullable = false)
+    private String name;
+
+    @OneToMany(mappedBy = "hastag", cascade = CascadeType.ALL, orphanRemoval = true)
+    private List<BookHasTag> bookHasTags = new ArrayList<>();
+}
diff --git "a/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/MarketingAlarm.java" "b/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/MarketingAlarm.java"
new file mode 100644
index 0000000..31ad74a
--- /dev/null
+++ "b/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/MarketingAlarm.java"
@@ -0,0 +1,26 @@
+package cau.ict.btrack.domain;
+
+import jakarta.persistence.*;
+import lombok.*;
+import cau.ict.btrack.domain.common.BaseEntity;
+
+@Entity
+@Getter
+@Builder
+@NoArgsConstructor(access = AccessLevel.PROTECTED)
+@AllArgsConstructor
+public class MarketingAlarm extends BaseEntity {
+    @Id
+    @GeneratedValue(strategy = GenerationType.IDENTITY)
+    private Long id;
+
+    @Column(length=30,nullable = false)
+    private String title;
+
+    @Column(length=100,nullable = false)
+    private String message;
+
+    @ManyToOne(fetch = FetchType.LAZY)
+    @JoinColumn(name = "user_id")
+    private User user;
+}
diff --git "a/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/NoticeAlarm.java" "b/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/NoticeAlarm.java"
new file mode 100644
index 0000000..e58493d
--- /dev/null
+++ "b/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/NoticeAlarm.java"
@@ -0,0 +1,28 @@
+package cau.ict.btrack.domain;
+
+import cau.ict.btrack.domain.common.BaseEntity;
+import jakarta.persistence.*;
+import lombok.*;
+
+@Entity
+@Getter
+@Builder
+@NoArgsConstructor(access = AccessLevel.PROTECTED)
+@AllArgsConstructor
+
+
+public class NoticeAlarm extends BaseEntity {
+    @Id
+    @GeneratedValue(strategy = GenerationType.IDENTITY)
+    private Long id;
+
+    @Column(length=30,nullable = false)
+    private String title;
+
+    @Column(length=100,nullable = false)
+    private String message;
+
+    @ManyToOne(fetch = FetchType.LAZY)
+    @JoinColumn(name = "user_id")
+    private User user;
+}
diff --git "a/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/User.java" "b/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/User.java"
new file mode 100644
index 0000000..2de27ad
--- /dev/null
+++ "b/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/User.java"
@@ -0,0 +1,49 @@
+package cau.ict.btrack.domain;
+
+import cau.ict.btrack.domain.common.BaseEntity;
+import cau.ict.btrack.domain.enums.Gender;
+import cau.ict.btrack.domain.mapping.Rental;
+import cau.ict.btrack.domain.mapping.Likes;
+import jakarta.persistence.*;
+import lombok.*;
+import java.util.ArrayList;
+import java.util.List;
+
+@Entity
+@Getter
+@Builder
+@NoArgsConstructor(access = AccessLevel.PROTECTED)
+@AllArgsConstructor
+
+public class User extends BaseEntity {
+    @Id
+    @GeneratedValue(strategy = GenerationType.IDENTITY)
+    private Long id;
+
+    @Column(length=10,nullable=false)
+    private String name;
+
+    @Column(length=20,nullable=false)
+    private String nickname;
+
+    @Column(length=15)
+    private String phoneNum;
+
+    @Enumerated(EnumType.STRING)
+    private Gender gender;
+
+    @Column(length = 20, nullable = false, unique = true)
+    private String loginId;
+
+    @Column(length = 60, nullable = false)
+    private String password;
+
+    @Column
+    private Boolean isDeleted;
+
+    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
+    private List<Likes> likes = new ArrayList<>();
+
+    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
+    private List<Rental> rental = new ArrayList<>();
+}
diff --git "a/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/common/BaseEntity.java" "b/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/common/BaseEntity.java"
new file mode 100644
index 0000000..6606945
--- /dev/null
+++ "b/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/common/BaseEntity.java"
@@ -0,0 +1,21 @@
+package cau.ict.btrack.domain.common;
+
+import jakarta.persistence.EntityListeners;
+import jakarta.persistence.MappedSuperclass;
+import lombok.Getter;
+import org.springframework.data.annotation.CreatedDate;
+import org.springframework.data.annotation.LastModifiedDate;
+import org.springframework.data.jpa.domain.support.AuditingEntityListener;
+import java.time.LocalDateTime;
+
+@MappedSuperclass
+@EntityListeners(AuditingEntityListener.class)
+@Getter
+public abstract class BaseEntity {
+
+    @CreatedDate
+    private LocalDateTime createdAt;
+
+    @LastModifiedDate
+    private LocalDateTime updatedAt;
+}
\ No newline at end of file
diff --git "a/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/enums/Gender.java" "b/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/enums/Gender.java"
new file mode 100644
index 0000000..d53791d
--- /dev/null
+++ "b/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/enums/Gender.java"
@@ -0,0 +1,5 @@
+package cau.ict.btrack.domain.enums;
+
+public enum Gender {
+    MALE, FEMALE, UNKNOWN;
+}
diff --git "a/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/mapping/BookHasTag.java" "b/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/mapping/BookHasTag.java"
new file mode 100644
index 0000000..2d2faf4
--- /dev/null
+++ "b/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/mapping/BookHasTag.java"
@@ -0,0 +1,28 @@
+package cau.ict.btrack.domain.mapping;
+
+import cau.ict.btrack.domain.Book;
+import cau.ict.btrack.domain.HasTag;
+import cau.ict.btrack.domain.common.BaseEntity;
+import jakarta.persistence.*;
+import lombok.*;
+
+@Entity
+@Getter
+@Builder
+@NoArgsConstructor(access = AccessLevel.PROTECTED)
+@AllArgsConstructor
+
+public class BookHasTag extends BaseEntity {
+    @Id
+    @GeneratedValue(strategy = GenerationType.IDENTITY)
+    private Long id;
+
+    @ManyToOne(fetch = FetchType.LAZY)
+    @JoinColumn(name = "book_id")
+    private Book book;
+
+    @ManyToOne(fetch = FetchType.LAZY)
+    @JoinColumn(name = "HasTag_id")
+    private HasTag hasTag;
+
+}
diff --git "a/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/mapping/Likes.java" "b/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/mapping/Likes.java"
new file mode 100644
index 0000000..e312ee3
--- /dev/null
+++ "b/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/mapping/Likes.java"
@@ -0,0 +1,28 @@
+package cau.ict.btrack.domain.mapping;
+
+import cau.ict.btrack.domain.User;
+import cau.ict.btrack.domain.Book;
+import cau.ict.btrack.domain.common.BaseEntity;
+import jakarta.persistence.*;
+import lombok.*;
+
+@Entity
+@Getter
+@Builder
+@NoArgsConstructor(access = AccessLevel.PROTECTED)
+@AllArgsConstructor
+
+public class Likes extends BaseEntity {
+    @Id
+    @GeneratedValue(strategy = GenerationType.IDENTITY)
+    private Long id;
+
+    @ManyToOne(fetch = FetchType.LAZY)
+    @JoinColumn(name = "user_id")
+    private User user;
+
+    @ManyToOne(fetch = FetchType.LAZY)
+    @JoinColumn(name = "book_id")
+    private Book book;
+
+}
diff --git "a/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/mapping/Rental.java" "b/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/mapping/Rental.java"
new file mode 100644
index 0000000..503c4f7
--- /dev/null
+++ "b/\354\265\234\354\204\234\354\230\201_book/main/java/cau/ict/btrack/domain/mapping/Rental.java"
@@ -0,0 +1,38 @@
+package cau.ict.btrack.domain.mapping;
+
+import cau.ict.btrack.domain.Book;
+import cau.ict.btrack.domain.User;
+import cau.ict.btrack.domain.common.BaseEntity;
+import jakarta.persistence.*;
+import lombok.*;
+import java.time.LocalDateTime;
+
+@Entity
+@Getter
+@Builder
+@NoArgsConstructor(access = AccessLevel.PROTECTED)
+@AllArgsConstructor
+
+public class Rental extends BaseEntity {
+
+    @Id
+    @GeneratedValue(strategy = GenerationType.IDENTITY)
+    private Long id;
+
+    @Column(nullable = false)
+    private LocalDateTime rentedAt;
+
+    @Column(nullable = false)
+    private LocalDateTime returnedAt;
+
+    @Column(nullable = false)
+    private LocalDateTime dueDate;
+
+    @ManyToOne(fetch = FetchType.LAZY)
+    @JoinColumn(name = "user_id")
+    private User user;
+
+    @ManyToOne(fetch = FetchType.LAZY)
+    @JoinColumn(name = "book_id")
+    private Book book;
+}