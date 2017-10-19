package org.ctlv.proxmox.monitor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONException;

public class Monitor {
	
	private ProxmoxAPI api ; 
	private Organizer organizer ; 
	
	public Monitor(ProxmoxAPI api, Organizer organizer) {
		this.api = api ; 
		this.organizer = organizer ; 
	}
	
	
	public void analyze(Map<String, List<LXC>> myCTsOnSrvs) throws Exception {
		
		try {
			
			long memUsedSrv1 = 0 ; 
			for (LXC ct : myCTsOnSrvs.get(Constants.SERVER1)) {
				memUsedSrv1 += ct.getMem() ; 
			}
			
			long memUsedSrv2 = 0 ; 
			for (LXC ct : myCTsOnSrvs.get(Constants.SERVER2)) {
				memUsedSrv2 += ct.getMem() ; 
			}
			
			// Ratio de RAM utilisée sur chaque serveur
			long memUsedRatioSrv1 = memUsedSrv1 / api.getNode(Constants.SERVER1).getMemory_total() ; 
			long memUsedRatioSrv2 = memUsedSrv2 / api.getNode(Constants.SERVER2).getMemory_total() ; 
			
			if ((memUsedRatioSrv1 < Constants.MIGRATION_THRESHOLD) && (memUsedRatioSrv2 < Constants.MIGRATION_THRESHOLD)) {
				// OK
			} else if (((memUsedRatioSrv1 > Constants.MIGRATION_THRESHOLD) && (memUsedRatioSrv2 < Constants.MIGRATION_THRESHOLD)) 
					|| ((memUsedRatioSrv1 > Constants.DROPPING_THRESHOLD) && (memUsedRatioSrv2 < Constants.DROPPING_THRESHOLD))) {
				
				// Migration de 1 vers 2 (2 cas de figures possibles)				
				this.organizer.migrateFrom(Constants.SERVER1,Constants.SERVER2) ; 
				
			} else if (((memUsedRatioSrv2 > Constants.MIGRATION_THRESHOLD) && (memUsedRatioSrv1 < Constants.MIGRATION_THRESHOLD)) 
					|| ((memUsedRatioSrv2 > Constants.DROPPING_THRESHOLD) && (memUsedRatioSrv1 < Constants.DROPPING_THRESHOLD))) {
				
				// Migration de 2 vers 1 (2 cas de figures possibles)
				this.organizer.migrateFrom(Constants.SERVER2,Constants.SERVER1) ; 
				
			} else if ((memUsedRatioSrv1 > Constants.DROPPING_THRESHOLD) && (memUsedRatioSrv2 > Constants.DROPPING_THRESHOLD)) {

				// Suppression dans les 2
				this.organizer.stopLastContainer(Constants.SERVER1) ; 
				this.organizer.stopLastContainer(Constants.SERVER2) ; 
				
			} else if ((Constants.DROPPING_THRESHOLD > memUsedRatioSrv1) && (memUsedRatioSrv1 > Constants.MIGRATION_THRESHOLD) 
					&& (Constants.DROPPING_THRESHOLD > memUsedRatioSrv2) && (memUsedRatioSrv2 > Constants.MIGRATION_THRESHOLD)) {
				// Ne rien faire 
			} else {
				throw new Exception("Cas non prévu dans l'analyseur : memUsedRationSrv1 = " + memUsedRatioSrv1 + " ; memUsedRatioSrv2 = " + memUsedRatioSrv2) ; 
			}

		} catch (LoginException | JSONException | IOException e1) {
			e1.printStackTrace();
		}

		try {
			Thread.sleep(60 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}		
}
