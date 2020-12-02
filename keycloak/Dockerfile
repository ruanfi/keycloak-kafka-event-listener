FROM jboss/keycloak:6.0.1
COPY keycloak/standalone.xml /opt/jboss/keycloak/standalone/configuration/standalone.xml
COPY keycloak/standalone-ha.xml /opt/jboss/keycloak/standalone/configuration/standalone-ha.xml
COPY keycloak-provider/build/libs/kafka-event-listener-provider-0.1.jar /opt/jboss/keycloak/standalone/deployments/