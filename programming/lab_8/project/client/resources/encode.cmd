@echo off
for %%f in (черновики\*.utf8) do (native2ascii -encoding utf-8 %%f %%~nf.properties)