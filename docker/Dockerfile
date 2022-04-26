FROM openjdk:11-jdk

# install general tools
RUN apt-get update && apt-get install --yes --no-install-recommends && apt-get install -y wget && apt-get install -y vim && \
   apt-get install unzip && apt-get install sudo && apt-get -y install curl  && sudo apt-get install -y alien && apt-get -y install fontconfig

# install gradle
ENV GRADLE_HOME /opt/gradle
ENV GRADLE_VERSION 7.4
ARG GRADLE_DOWNLOAD_SHA256=8cc27038d5dbd815759851ba53e70cf62e481b87494cc97cfd97982ada5ba634
RUN set -o errexit -o nounset && \
   echo "Downloading Gradle" && \
   wget --no-verbose --output-document=gradle.zip "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" && \
   echo "${GRADLE_DOWNLOAD_SHA256} *gradle.zip" | sha256sum --check - && \
   echo "Installing Gradle" && \
   unzip gradle.zip && \
   rm gradle.zip && \
   mv "gradle-${GRADLE_VERSION}" "${GRADLE_HOME}/" && \
   ln --symbolic "${GRADLE_HOME}/bin/gradle" /usr/bin/gradle && \
   gradle --version

WORKDIR /home/DockDockbuild

CMD ["./gradlew", "buildPlugin"]
