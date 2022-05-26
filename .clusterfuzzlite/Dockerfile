FROM gcr.io/oss-fuzz-base/base-builder-jvm:v1
RUN apt-get update && apt-get install -y make autoconf automake libtool maven nux-tools
RUN curl -O https://download.oracle.com/java/18/latest/jdk-18_linux-x64_bin.deb
RUN apt install -y ./jdk-18_linux-x64_bin.deb
COPY . $SRC/pacman
WORKDIR pacman
COPY .clusterfuzzlite/build.sh $SRC/
