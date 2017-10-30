package org.ctlv.proxmox.monitor;

import java.io.IOException;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.ctlv.proxmox.generator.Generator;
import org.json.JSONException;

public class Organizer {

	private ProxmoxAPI api ; 

	public Organizer(ProxmoxAPI api) {
		this.api = api ; 
	}

	/* Migre un container au hasard de srvFrom vers srvTo. 
	 * Pour cela : arrête le container, attend qu'il s'arrête complètement, le migre, 
	 * attend qu'il finisse de migrer, le relance et attend qu'il soit complètement relancé. 
	 */
	public void migrateFrom(String srvFrom, String srvTo) throws LoginException, JSONException, IOException, InterruptedException {
		List<LXC> cts = api.getCTs(srvFrom) ; 
		if (cts.get(0) != null) {
			LXC ct = cts.get(0) ; 
			String ctID = ct.getVmid() ;  
			while (!ct.getStatus().equals("stopped")) {	
				try {
					api.stopCT(srvFrom, cts.get(0).getVmid());
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
				ct = api.getCT(srvFrom, ctID) ; 
			}
			System.out.println(ct.getName() + " is stopped");
			api.migrateCT(srvFrom, ct.getVmid(), srvTo);
			System.out.println(ct.getName() + " migrated from " + srvFrom + " to " + srvTo + "...");
			ct = api.getCT(srvTo, ctID) ; 
			while (ct == null || !ct.getStatus().equals("running")) {
				if (ct != null) {
					api.startCT(srvTo, ct.getVmid());
				}
				ct = api.getCT(srvTo, ctID) ; 
				Thread.sleep(Generator.getNextEventPeriodicSec(3));
			}
			ct = api.getCT(srvTo, ctID) ; 
			System.out.println(ct.getName() + " is started");
		}
	}

	/*
	 * Arrête le dernier container créé sur le serveur srv.
	 */
	public void stopLastContainer(String srv) throws LoginException, JSONException, IOException {
		String olderCT = findMyOlderCT(srv) ; 
		String ctID = api.getCT(srv, olderCT).getVmid(); 
		api.stopCT(srv, ctID);
		System.out.println("Container " + olderCT + " stopped from " + srv);
	}

	/*
	 * Renvoie l'ID du container le plus ancien.
	 */
	public String findMyOlderCT(String srv) throws LoginException, JSONException, IOException {
		List<LXC> cts = api.getCTs(srv) ; 
		String olderCTID = "" ; 
		long maxUptime = 0 ; 
		for (LXC ct : cts) {
			if (ct.getName().startsWith(Constants.CT_BASE_NAME)) {
				if (ct.getUptime() > maxUptime) {
					maxUptime = ct.getUptime() ; 
					olderCTID = ct.getVmid() ; 
				}
			}
		}
		return olderCTID ; 
	}
}
