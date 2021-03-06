package com.juliuskrah.keycloak.provider;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.enterprise.concurrent.ManagedTask;
import javax.enterprise.concurrent.ManagedTaskListener;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.keycloak.events.Event;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransactionManager;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class KeycloakEventListener implements Callable<RecordMetadata>, ManagedTask {
	private final Event event;
	private final KeycloakSession session;

	@Override
	public ManagedTaskListener getManagedTaskListener() {
		return new KeycloakManagedTaskListener();
	}

	@Override
	public Map<String, String> getExecutionProperties() {
		// Do not add any execution properties
		return null;
	}

	@Override
	public RecordMetadata call() throws Exception {
		log.debug("Running task asynchronously");
		log.debug("Session is: {}", session);
		log.debug("Transaction Manager is: {}", session.getTransactionManager());
		KeycloakSession keycloakSession = session.getKeycloakSessionFactory().create();
		KeycloakTransactionManager transactionManager = keycloakSession.getTransactionManager();
		transactionManager.begin();
		RealmProvider realmProvider = session.realms();
		RealmModel realm = realmProvider.getRealm(event.getRealmId());
		UserProvider userProvider = session.users();
		UserModel user = userProvider.getUserById(event.getUserId(), realm);
		log.debug("TX manager is active: {}", keycloakSession);
		try {
			transactionManager.commit();
		} catch (Exception ex) {
			log.debug("It keep failing all the time! Bro!", ex);
		}
		RecordMetadata metadata = KeycloakEventProducer.get() //
				.send(new ProducerRecord<String, Object>("keycloak.users", //
						"user", User.toUser(user)))
				.get();
		return metadata;

	}

}
