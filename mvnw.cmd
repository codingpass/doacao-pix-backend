@ECHO OFF
REM Maven Wrapper - baixe o Maven automaticamente na primeira execucao
SET MAVEN_PROJECTBASEDIR=%~dp0
SET JAVA_EXECUTABLE=java
IF NOT "%JAVA_HOME%"=="" SET JAVA_EXECUTABLE=%JAVA_HOME%\bin\java

%JAVA_EXECUTABLE% -classpath "%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar" ^
  -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" ^
  org.apache.maven.wrapper.MavenWrapperMain %*
