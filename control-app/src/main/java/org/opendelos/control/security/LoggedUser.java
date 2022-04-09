/* 
     Author: Michael Gatzonis - 15/3/2022 
     obrella
*/
package org.opendelos.control.security;

import java.util.List;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import lombok.Getter;
import lombok.Setter;
import org.opendelos.model.users.ActiveUserStore;

import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class LoggedUser implements HttpSessionBindingListener {

	private String username;
	private ActiveUserStore activeUserStore;

	public LoggedUser(String username, ActiveUserStore activeUserStore) {
		this.username = username;
		this.activeUserStore = activeUserStore;
	}

	public LoggedUser() {}

	@Override
	public void valueBound(HttpSessionBindingEvent event) {
		List<String> users = activeUserStore.getUsers();
		LoggedUser user = (LoggedUser) event.getValue();
		if (!users.contains(user.getUsername())) {
			users.add(user.getUsername());
		}
	}

	@Override
	public void valueUnbound(HttpSessionBindingEvent event) {
		List<String> users = activeUserStore.getUsers();
		LoggedUser user = (LoggedUser) event.getValue();
		if (users.contains(user.getUsername())) {
			users.remove(user.getUsername());
		}
	}

}
