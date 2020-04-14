#!/bin/sh
git status
git add -A
git commit -m "update"
git push origin master
mvn clean install