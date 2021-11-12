#!/bin/sh
set -e
mvn clean package -DskipTests --batch-mode
