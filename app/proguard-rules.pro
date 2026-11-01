# 앱 전용 R8 keep 규칙.
# Room · Hilt · kotlinx.serialization · Compose 등 라이브러리는 AAR에 포함된 consumer 규칙을 따른다.

# 스택 트레이스의 줄 번호를 유지하고 원본 파일명은 숨긴다. (mapping.txt로 역난독화)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Navigation3 백스택(rememberNavBackStack)은 NavKey를 kotlinx.serialization으로 저장하고,
# 복원할 때 저장해 둔 클래스 이름으로 serializer를 리플렉션으로 찾는다.
# 목적지 클래스 이름·멤버(Companion, INSTANCE, serializer())가 바뀌거나 제거되면
# 프로세스 재생성 후 상태 복원이 실패하므로 그대로 유지한다.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keep class * implements androidx.navigation3.runtime.NavKey { *; }
