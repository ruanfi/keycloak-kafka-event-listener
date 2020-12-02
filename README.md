# Keycloak Event Listener for Kafka

This demo project will create all infrastructure to show SPI event listener for `Keycloak` and `Apache Kafka`

## Prerequisite

- Docker (latest)
- Compose (latest)
- [Kafka Tool](https://www.kafkatool.com/download.html)

## Building the Containers

To build the containers:

```bash
> cd keycloak-provider
> ./gradlew clean assemble
> cd ..
> docker-compose build
```

## Start example

You can run the project with the following command

```bash
> docker-compose up
```

Now open the keycloak UI at [http://localhost:8080](http://localhost:8080/auth)

login with:

username: admin  
password: admin

Go to the `Events` left menu item, and navigate to `Config`. Under `Event Listeners`, add `keycloak-demo` and save.

Start `Kafka Tool`, create new connection:
- Zookeeper host: localhost
- Zookeeper port: 2181
- Advanced/Bootstrap server: localhost:9092

Topic to watch is: keycloak.userssout

Logout and log back in. Watch the console for changes and `Kafka Tool` to see events.

