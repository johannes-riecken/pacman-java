FROM gcr.io/oss-fuzz-base/base-builder-jvm:v1
RUN apt-get update && apt-get install -y make autoconf automake libtool maven
COPY . $SRC/pacman
WORKDIR pacman
COPY .clusterfuzzlite/build.sh $SRC/
