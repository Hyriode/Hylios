#!/bin/bash

: "${MIN_MEMORY:=256M}}"
: "${MAX_MEMORY:=4G}}"

echo "[init] Copying Hylios jar"
cp /usr/app/Hylios.jar /hylios

echo "[init] Starting process..."
exec java -Xms${MIN_MEMORY} -Xmx${MAX_MEMORY} -jar Hylios.jar