# Ugoal - 오늘을 설계하는 목표 관리 앱

Toss 감성의 미니멀하고 감각적인 Android 목표 관리 앱입니다.

## 주요 기능

### 1️⃣ 큰 목표 (Big Goals)
- 장기적인 목표 등록 및 관리
- 커스터마이징 가능한 색상과 아이콘
- 진행률 표시 (자연스러운 애니메이션)
- 목표별 할 일 추적

### 2️⃣ 오늘의 목표 (Daily Focus)
- 매일 집중할 일 설정
- 완료 체크 시 진동 피드백
- 날짜별 MongoDB 저장

### 3️⃣ 할 일 (Todos)
- CRUD (생성/수정/삭제/완료)
- 큰 목표와 연동
- 스와이프 삭제 애니메이션
- MongoDB Atlas 실시간 동기화

## 기술 스택

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM
- **Database**: MongoDB Atlas
- **Networking**: Ktor Client
- **Async**: Coroutines + Flow
- **Design**: Material 3 + Custom Toss Theme
- **Navigation**: Compose Navigation
- **Testing**: JUnit + Compose UI Test

## 프로젝트 구조

```
com.heodongun.ugoal/
├── data/
│   ├── models/          # Data models (BigGoal, DailyGoal, Todo)
│   ├── remote/          # MongoDB client
│   └── repository/      # Repository layer
├── domain/
│   └── usecase/         # Business logic (future)
├── ui/
│   ├── screens/         # Compose screens
│   ├── components/      # Reusable UI components
│   ├── theme/           # Toss-inspired theme
│   └── navigation/      # Navigation setup
├── viewmodel/           # ViewModels
└── utils/               # Utilities (HapticFeedback, DateFormatter)
```

## 디자인 특징

### Toss 감성 디자인
- **색상**: 흰색 베이스 + 포인트 블루(#3182F6)
- **타이포그래피**: 
  - 메인 제목: 26sp Bold
  - 날짜/시간: 14sp Light
  - To-do 항목: 16sp Medium
- **애니메이션**: 
  - FadeIn / SlideIn
  - Spring animation
  - Smooth transitions
- **레이아웃**: 
  - 충분한 여백
  - 깔끔한 카드 디자인
  - Bottom Navigation

## MongoDB Atlas 설정

### 연결 정보
```
mongodb+srv://heodongun:heodongun0922!!@heodongun.zpzozxd.mongodb.net/
```

### 데이터 구조
```json
{
  "userId": "default_user",
  "bigGoals": [
    {
      "id": "uuid",
      "title": "앱 출시",
      "color": "#3182F6",
      "icon": "🎯",
      "createdAt": 1234567890,
      "todos": []
    }
  ],
  "dailyGoals": [
    {
      "id": "uuid",
      "date": "2025-10-28",
      "title": "코딩하기",
      "isCompleted": false,
      "createdAt": 1234567890
    }
  ],
  "todos": [
    {
      "id": "uuid",
      "title": "할 일",
      "isCompleted": false,
      "goalId": "goal-uuid",
      "date": "2025-10-28",
      "createdAt": 1234567890
    }
  ]
}
```

## 빌드 방법

### 1. 프로젝트 열기
```bash
# Android Studio에서 프로젝트 열기
File > Open > 프로젝트 폴더 선택
```

### 2. Gradle 동기화
```bash
# 자동으로 동기화되지 않는 경우
File > Sync Project with Gradle Files
```

### 3. 빌드 및 실행
```bash
# Android Studio에서
Run > Run 'app'

# 또는 터미널에서
./gradlew assembleDebug
```

### 4. 테스트 실행
```bash
# 단위 테스트
./gradlew test

# UI 테스트 (에뮬레이터 또는 실제 기기 필요)
./gradlew connectedAndroidTest
```

## 요구사항

- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34
- **JDK**: 17

## 주요 의존성

```kotlin
// Compose
implementation("androidx.compose.material3:material3:1.1.2")
implementation("androidx.compose.material:material-icons-extended")

// Navigation
implementation("androidx.navigation:navigation-compose:2.7.5")

// ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

// Ktor Client
implementation("io.ktor:ktor-client-android:2.3.6")
implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.6")

// MongoDB
implementation("io.realm.kotlin:library-base:1.13.0")

// Testing
testImplementation("app.cash.turbine:turbine:1.0.0")
```

## 개발 가이드

### 새로운 화면 추가
1. `ui/screens/`에 Composable 함수 작성
2. `ui/navigation/NavGraph.kt`에 라우트 추가
3. 필요시 ViewModel 생성

### 새로운 컴포넌트 추가
1. `ui/components/`에 재사용 가능한 Composable 작성
2. Toss 디자인 시스템 따르기 (Color, Typography)

### MongoDB 데이터 추가
1. `data/models/`에 데이터 모델 정의
2. `data/repository/UgoalRepository.kt`에 CRUD 메서드 추가
3. ViewModel에서 Repository 메서드 호출

## 트러블슈팅

### Gradle 동기화 실패
- JDK 버전 확인 (17 필요)
- `gradle/wrapper/gradle-wrapper.properties` 확인

### MongoDB 연결 실패
- 인터넷 연결 확인
- MongoDB Atlas URL 확인
- AndroidManifest.xml에 INTERNET 권한 확인됨

### 빌드 오류
```bash
# 캐시 정리
./gradlew clean

# 재빌드
./gradlew build --refresh-dependencies
```

## 라이센스

This project is for educational purposes.

## 문의

프로젝트 관련 문의사항이 있으시면 이슈를 등록해주세요.

---

**Made with ❤️ using Kotlin & Jetpack Compose**
