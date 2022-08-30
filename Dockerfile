FROM openjdk:11

COPY "target/kirvespeli-1.0.0.jar" "/app.jar"

EXPOSE 8080
CMD [ "-jar", "/app.jar" ]
ENTRYPOINT [ "java" ]