@echo off
rem Requiere JDK 17 instalado. Genera el .jar en build\libs\
call gradlew.bat build
echo.
echo Listo: busca cocteleria-1.0.0.jar en la carpeta build\libs
pause
