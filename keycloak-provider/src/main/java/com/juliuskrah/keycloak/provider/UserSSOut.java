package com.juliuskrah.keycloak.provider;

import lombok.Data;
import org.keycloak.events.admin.AuthDetails;

@Data
public class UserSSOut {
	private AuthDetails authDetails;
	private long dateTime;
	private User user;
}
