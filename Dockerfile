FROM maven:3 as mvn
WORKDIR /app
COPY . .
RUN mvn package

FROM openjdk:8-jre-alpine
WORKDIR /app
ARG project=NeoGit
ENV artifact=${project}-jar-with-dependencies.jar
ENV MASTERIP=127.0.0.1
ENV ID=0
ENV WD=/home
COPY --from=mvn /app/target/${artifact} .

CMD /usr/bin/java -jar ${artifact} -m $MASTERIP -id $ID -wd $WD