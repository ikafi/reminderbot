FROM java:8

RUN echo "deb http://ppa.launchpad.net/natecarlson/maven3/ubuntu precise main" | tee -a /etc/apt/sources.list
RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 3DD9F856

RUN apt-get update
RUN apt-get install -y maven3
RUN ln -s /usr/share/maven3/bin/mvn /usr/bin/mvn

ENV TZ=Europe/Helsinki
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /code
ADD pom.xml /code/pom.xml

RUN mvn dependency:resolve
ADD src /code/src
RUN mvn verify
RUN mvn clean compile

CMD mvn exec:java
