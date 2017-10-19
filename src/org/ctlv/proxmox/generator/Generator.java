package org.ctlv.proxmox.generator;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONException;


public class Generator implements Runnable{
	
	public Generator() {}
	
	static Random rndTime = new Random(new Date().getTime());
	public static int getNextEventPeriodic(int period) {
		return period * 1000 * 60;
	}
	public static int getNextEventUniform(int max) {
		return rndTime.nextInt(max);
	}
	public static int getNextEventExponential(int inv_lambda) {
		float next = (float) (- Math.log(rndTime.nextFloat()) * inv_lambda);
		return (int)next;
	}
	
	public void run() {		
	
		long baseID = Constants.CT_BASE_ID;
		long index = 0 ; 

		ProxmoxAPI api = new ProxmoxAPI();
		Random rndServer = new Random(new Date().getTime());
		
		try {
			long memAllowedOnServer1 = (long) (api.getNode(Constants.SERVER1).getMemory_total() * Constants.MAX_THRESHOLD);
			long memAllowedOnServer2 = (long) (api.getNode(Constants.SERVER2).getMemory_total() * Constants.MAX_THRESHOLD);
			
			while (true) {
				
				// 1. Calculer la quantite de RAM utilisee par mes CTs sur chaque serveur
				long memOnServer1 = 0;
				List<LXC> cts1 = api.getCTs(Constants.SERVER1);
				for (LXC lxc : cts1) {
					memOnServer1 += lxc.getMem() ; 
				}
				
				long memOnServer2 = 0;
				List<LXC> cts2 = api.getCTs(Constants.SERVER2);
				for (LXC lxc : cts2) {
					memOnServer2 += lxc.getMem() ; 
				}
				

				if (memOnServer1 < memAllowedOnServer1 && memOnServer2 < memAllowedOnServer2) {
					
					// choisir un serveur aleatoirement avec les ratios specifies 66% vs 33%
					String serverName;
					if (rndServer.nextFloat() < Constants.CT_CREATION_RATIO_ON_SERVER1)
						serverName = Constants.SERVER1;
					else
						serverName = Constants.SERVER2;
					
					api.createCT(serverName, Long.toString(baseID+index), Constants.CT_BASE_NAME + index, 512);// cr�er un contenaire sur ce serveur
					
					index ++ ; 				
					// attendre jusqu'au prochain evenement
					Thread.sleep(getNextEventPeriodic(1));
				}
				else {
					System.out.println("Servers are loaded, waiting 5 more seconds ...");
					Thread.sleep(Constants.GENERATION_WAIT_TIME* 1000);
				}
			}

		} catch (LoginException | JSONException | IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		
	}

}
