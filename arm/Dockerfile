FROM zsoltm/postgresql-armhf
MAINTAINER Kimbro Staken version: 0.1

RUN apt-get update
RUN apt-get install -y git 
RUN echo deb http://http.debian.net/debian jessie-backports main >> /etc/apt/sources.list
RUN apt-get update
RUN apt install -y -t jessie-backports  openjdk-8-jre-headless ca-certificates-java
RUN apt-get install -y openjdk-8-jre openjdk-8-jdk-headless
RUN  apt-get install -y openjdk-8-jdk
RUN apt-get install -y maven

RUN apt-get clean
#RUN rm -rf /var/lib/apt/lists/*
RUN chown postgres:postgres /usr/lib/postgresql/9.4/bin/postgres
RUN chown postgres:postgres /var/lib/postgresql/data
# RUN /usr/lib/postgresql/9.4/bin/initdb
VOLUME /var/lib/postgresql/data
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-armhf
#RUN git clone https://github.com/pfremaux/3pPusher.git
#RUN cd 3pPusher
# RUN mvn clean install -f /3pPusher/pom.xml
EXPOSE 8080

# CMD ["java", "-jar", "/usr/share/jenkins/jenkins.war"]
