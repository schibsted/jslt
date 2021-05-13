FROM openjdk:slim

RUN apt update && apt install -y git

WORKDIR /usr/src/jslt

COPY . .

RUN ./gradlew :playground:shadowJar

EXPOSE 9999

CMD ["java", "-cp", "playground/build/libs/playground-0.0.1-all.jar", "no.priv.garshol.jslt.playground.PlaygroundServer", "9999"]
