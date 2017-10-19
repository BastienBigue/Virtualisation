package org.ctlv.proxmox.monitor;

import java.io.IOException;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONException;

public class Organizer {

	private ProxmoxAPI api ; 

	public Organizer(ProxmoxAPI api) {
		this.api = api ; 
	}


	public void migrateFrom(String srvFrom, String srvTo) throws LoginException, JSONException, IOException {
		List <String> cts = api.getCTList(srvFrom) ; 
		if (cts.get(0) != null) {
			api.migrateCT(srvFrom, cts.get(0), srvTo);
			System.out.println("Container" + cts.get(0) + " migrated from " + srvFrom + " to " + srvTo);
		}
	}

	public void stopLastContainer(String srv) throws LoginException, JSONException, IOException {
		String olderCT = findMyOlderCT(srv) ; 
		api.stopCT(srv, olderCT);
		System.out.println("Container" + olderCT + " stopped from " + srv);
	}

	public String findMyOlderCT(String srv) throws LoginException, JSONException, IOException {

		List<LXC> cts = api.getCTs(srv) ;
		String olderCT = "" ; 
		long maxUptime = 0 ; 
		for (LXC ct : cts) {
			if (ct.getName().startsWith(Constants.CT_BASE_NAME)) {
				if (ct.getUptime() > maxUptime) {
					maxUptime = ct.getUptime() ; 
					olderCT = ct.getName() ; 
				}
			}
		}
		return olderCT ; 
	}

}
