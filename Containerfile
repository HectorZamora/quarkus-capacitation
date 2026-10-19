####
# Containerfile para el runtime JVM de Quarkus (Podman nativo).
#
# Antes de construir la imagen ejecuta:
#
#   ./mvnw package
#
# La imagen se construye con:
#
#   podman build -t localhost/minibanco:latest -f Containerfile .
#
# Este archivo es equivalente a src/main/docker/Dockerfile.jvm generado por
# Quarkus; se mantiene en la raíz con el nombre "Containerfile" para que
# podman-compose lo detecte de forma nativa.
###
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:1.24

ENV LANGUAGE='en_US:en'

# Se usan capas separadas para reaprovechar la caché de librerías entre builds
COPY --chown=185 target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 target/quarkus-app/*.jar /deployments/
COPY --chown=185 target/quarkus-app/app/ /deployments/app/
COPY --chown=185 target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
