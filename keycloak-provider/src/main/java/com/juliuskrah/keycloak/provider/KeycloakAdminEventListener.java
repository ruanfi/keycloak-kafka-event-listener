package com.juliuskrah.keycloak.provider;

import static org.keycloak.events.admin.OperationType.ACTION;
import static org.keycloak.events.admin.OperationType.DELETE;
import static org.keycloak.events.admin.ResourceType.USER;
import static org.keycloak.events.admin.ResourceType.USER_SESSION;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import javax.enterprise.concurrent.ManagedTask;
import javax.enterprise.concurrent.ManagedTaskListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransactionManager;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;

@Slf4j
@RequiredArgsConstructor
public class KeycloakAdminEventListener implements Callable<Optional<RecordMetadata>>, ManagedTask {
	private final AdminEvent event;
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
	public Optional<RecordMetadata> call() throws Exception {
		Optional<RecordMetadata> metadata = Optional.empty();
		if(USER.equals(event.getResourceType()) && ACTION.equals(event.getOperationType())) {
			log.debug("Resource path: {}, Representation: {}, Error: {}",  event.getResourcePath(), event.getRepresentation(), event.getError());
		}

		if(USER_SESSION.equals(event.getResourceType()) && DELETE.equals(event.getOperationType())) {
			log.trace("Running task asynchronously");
			KeycloakTransactionManager transactionManager = session.getKeycloakSessionFactory().create().getTransactionManager();
			transactionManager.begin();

			RealmProvider realmProvider = session.realms();
			RealmModel realm = realmProvider.getRealm(event.getRealmId());
			UserProvider userProvider = session.users();
			UserModel user = userProvider.getUserById(event.getAuthDetails().getUserId(), realm);
			transactionManager.commit();

			UserSSOut userSSOut = new UserSSOut();
			userSSOut.setAuthDetails(event.getAuthDetails());
			userSSOut.setDateTime(event.getTime());
			userSSOut.setUser(User.toUser(user));

			metadata = Optional.ofNullable(KeycloakEventProducer.get() //
					.send(new ProducerRecord<>("keycloak.userssout", "user", userSSOut))
					.get());
		}

		return metadata;
	}
}
