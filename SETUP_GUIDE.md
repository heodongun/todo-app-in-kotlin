# Ugoal 앱 설정 가이드

## 빠른 시작

### 1. Android Studio에서 프로젝트 열기
```bash
1. Android Studio 실행
2. File > Open
3. 이 프로젝트 폴더 선택
4. "Trust Project" 클릭
```

### 2. Gradle 동기화
프로젝트가 열리면 자동으로 Gradle 동기화가 시작됩니다.
- 화면 상단에 "Sync Now" 버튼이 나타나면 클릭
- 초기 빌드는 몇 분 소요될 수 있습니다

### 3. 에뮬레이터 또는 실제 기기 준비

#### 에뮬레이터 생성 (권장)
```
1. Tools > Device Manager
2. "Create Device" 클릭
3. Pixel 6 또는 최신 기기 선택
4. System Image: Android 14 (API 34) 다운로드
5. "Finish" 클릭
```

#### 실제 기기 연결
```
1. 기기에서 개발자 옵션 활성화
2. USB 디버깅 활성화
3. USB로 컴퓨터에 연결
4. "이 컴퓨터 신뢰" 허용
```

### 4. 앱 실행
```
1. 상단 툴바에서 실행할 기기 선택
2. 초록색 재생 버튼 클릭 (▶️)
3. 또는 Shift + F10 (Windows/Linux)
4. 또는 Control + R (Mac)
```

## MongoDB 설정

앱은 이미 MongoDB Atlas에 연결되도록 설정되어 있습니다:
- 연결 문자열: `mongodb+srv://heodongun:heodongun0922!!@heodongun.zpzozxd.mongodb.net/`
- 데이터베이스: `ugoal`
- 컬렉션: `users`

### 데이터 초기화 (선택사항)
MongoDB Atlas 콘솔에서:
```
1. https://cloud.mongodb.com 로그인
2. Clusters > Browse Collections
3. ugoal 데이터베이스 선택
4. users 컬렉션에 초기 문서 생성 (자동으로 생성됨)
```

## 주요 파일 위치

### 소스 코드
```
app/src/main/java/com/heodongun/ugoal/
├── MainActivity.kt                    # 앱 진입점
├── UgoalApplication.kt                # Application 클래스
├── data/                              # 데이터 레이어
│   ├── models/                        # 데이터 모델
│   ├── remote/                        # MongoDB 클라이언트
│   └── repository/                    # Repository 패턴
├── ui/                                # UI 레이어
│   ├── screens/                       # 화면들
│   ├── components/                    # 재사용 컴포넌트
│   ├── theme/                         # 디자인 시스템
│   └── navigation/                    # 내비게이션
├── viewmodel/                         # ViewModel 레이어
└── utils/                             # 유틸리티
```

### 리소스
```
app/src/main/res/
├── values/
│   ├── strings.xml                    # 문자열 리소스
│   └── themes.xml                     # 테마 설정
└── mipmap-*/                          # 앱 아이콘
```

## 테스트 실행

### 단위 테스트
```bash
# Android Studio에서
1. app/src/test 우클릭
2. "Run 'Tests in 'test''" 선택

# 터미널에서
./gradlew test
```

### UI 테스트
```bash
# 에뮬레이터 또는 실제 기기 연결 후

# Android Studio에서
1. app/src/androidTest 우클릭
2. "Run 'Tests in 'androidTest''" 선택

# 터미널에서
./gradlew connectedAndroidTest
```

## 문제 해결

### Gradle 동기화 실패
```bash
# 1. Gradle 캐시 정리
./gradlew clean

# 2. Android Studio 재시작

# 3. File > Invalidate Caches / Restart
```

### 빌드 오류
```bash
# 의존성 새로고침
./gradlew build --refresh-dependencies

# Gradle wrapper 재설정
./gradlew wrapper --gradle-version 8.2
```

### MongoDB 연결 실패
```
1. 인터넷 연결 확인
2. AndroidManifest.xml에 INTERNET 권한 있는지 확인
3. MongoDB Atlas 크레덴셜 확인
```

### 에뮬레이터 느림
```
1. Tools > Device Manager
2. 기존 AVD 삭제
3. 새 AVD 생성 시:
   - Graphics: Hardware - GLES 2.0 선택
   - RAM: 2048 MB 이상
   - Internal Storage: 2048 MB 이상
```

## 개발 팁

### Hot Reload 활성화
Compose는 자동으로 코드 변경사항을 반영합니다:
```
- UI 코드 수정 후 저장하면 자동 리로드
- ViewModel이나 데이터 레이어 변경 시 재실행 필요
```

### 디버깅
```
1. 코드에 중단점 설정 (줄 번호 옆 클릭)
2. 🐛 버튼으로 디버그 모드 실행
3. Logcat에서 로그 확인: View > Tool Windows > Logcat
```

### 레이아웃 미리보기
```kotlin
@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    UgoalTheme {
        // Your composable
    }
}
```

## 다음 단계

### 기능 추가
1. 목표 상세 화면 구현
2. 통계 대시보드 추가
3. 알림 기능 추가
4. 위젯 구현

### 성능 최적화
1. 이미지 로딩 최적화
2. 데이터 캐싱 전략
3. 오프라인 모드 지원

### 배포 준비
1. ProGuard 규칙 최적화
2. 서명 키 생성
3. Play Store 등록

## 도움이 필요하신가요?

- [Jetpack Compose 공식 문서](https://developer.android.com/jetpack/compose)
- [Kotlin 공식 문서](https://kotlinlang.org/docs/home.html)
- [MongoDB Atlas 문서](https://www.mongodb.com/docs/atlas/)

---

**즐거운 개발 되세요! 🚀**
