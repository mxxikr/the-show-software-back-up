@echo off
echo Starting Spring Boot with dev profile...
java -jar -Dspring.profiles.active=dev internal_test_page.jar
pause