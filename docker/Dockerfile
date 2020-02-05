FROM openjdk:8-jdk

# install general tools
RUN apt-get update && apt-get install --yes --no-install-recommends && apt-get install -y wget && apt-get install -y vim && \
   apt-get install unzip && apt-get install sudo && apt-get -y install curl  && sudo apt-get install -y alien && apt-get -y install fontconfig

# install gradle
ENV GRADLE_HOME /opt/gradle
ENV GRADLE_VERSION 5.4.1
ARG GRADLE_DOWNLOAD_SHA256=7bdbad1e4f54f13c8a78abc00c26d44dd8709d4aedb704d913fb1bb78ac025dc
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
