@echo off
for %%f in (�୮����\*.utf8) do (native2ascii -encoding utf-8 %%f %%~nf.properties)