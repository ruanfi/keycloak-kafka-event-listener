package com.juliuskrah.keycloak.provider;

import static java.util.Objects.nonNull;

import java.util.List;

import javax.enterprise.concurrent.ManagedExecutorService;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class KeycloakEventListenerProvider implements EventListenerProvider {
	private final ManagedExecutorService mes;
	private final List<EventType> excludedEvents;
	private final KeycloakSession session;

	@Override
	public void close() {
		log.trace("Cleaning up");
	}

	@Override
	public void onEvent(Event event) {
		log.debug("Received event {}, with details: {}", event.getType(), event.getDetails());
	}

	@Override
	public void onEvent(AdminEvent event, boolean includeRepresentation) {
		log.debug("Received Admin event {}, with operation: {}, with resource path: {}, with representation: {}, with error: {}",
				event.getResourceType(),
				event.getOperationType(),
				event.getResourcePath(),
				event.getRepresentation(),
				event.getError());

		mes.submit(new KeycloakAdminEventListener(event, session));
	}
}
