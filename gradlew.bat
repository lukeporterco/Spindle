@ECHO OFF
SETLOCAL

SET APP_HOME=%~dp0
SET CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar

IF DEFINED JAVA_HOME (
    SET JAVA_EXEC=%JAVA_HOME%\bin\java.exe
) ELSE (
    SET JAVA_EXEC=java.exe
)

"%JAVA_EXEC%" -Dorg.gradle.appname=gradlew -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
