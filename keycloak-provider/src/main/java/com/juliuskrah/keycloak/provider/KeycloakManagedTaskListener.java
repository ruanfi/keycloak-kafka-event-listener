package com.juliuskrah.keycloak.provider;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.concurrent.ManagedTaskListener;

import org.apache.kafka.clients.producer.RecordMetadata;

import lombok.extern.slf4j.Slf4j;

/**
 * A listener class for lifecycle events of an Executor
 * 
 * @author Julius Krah
 *
 */
@Slf4j
public class KeycloakManagedTaskListener implements ManagedTaskListener {

	@Override
	public void taskSubmitted(Future<?> future, ManagedExecutorService executor, Object task) {
		log.trace("Task is submitted");
	}

	@Override
	public void taskAborted(Future<?> future, ManagedExecutorService executor, Object task, Throwable exception) {
		log.trace("Task is aborted");
	}

	@Override
	public void taskDone(Future<?> future, ManagedExecutorService executor, Object task, Throwable exception) {
		log.trace("Task is done");
		try {
			log.trace("Future status is: done?: {}, canceled?: {}", future.isDone(), future.isCancelled());
			Optional<RecordMetadata> metadataOpt = (Optional<RecordMetadata>) future.get();
			metadataOpt.ifPresent(recordMetadata -> log.info("Topic '{}' updated", recordMetadata.topic()));

		} catch (InterruptedException | ExecutionException e) {
			log.error("Error while listening for done event", e);
		}
	}

	@Override
	public void taskStarting(Future<?> future, ManagedExecutorService executor, Object task) {
		log.trace("Task is starting");
	}
}
