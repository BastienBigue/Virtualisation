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


public class Generator {
	
	
	public Generator() { }
	
	static Random rndTime = new Random(new Date().getTime());
	
	public static int getNextEventPeriodicSec(int period) {
		return period * 1000 ; 
	}
	
	public static int getNextEventUniform(int max) {
		return rndTime.nextInt(max);
	}
	
	public static int getNextEventExponential(int inv_lambda) {
		float next = (float) (- Math.log(rndTime.nextFloat()) * inv_lambda);
		return (int)next;
	}
	
	public static void main(String[] args) {	
		
		ProxmoxAPI api = new ProxmoxAPI() ; 
	
		long baseID = Constants.CT_BASE_ID;
		long index = 0 ; 

		Random rndServer = new Random(new Date().getTime());
		
		try {
			long memAllowedOnServer1 = (long) (api.getNode(Constants.SERVER1).getMemory_total() * Constants.MAX_THRESHOLD);
			long memAllowedOnServer2 = (long) (api.getNode(Constants.SERVER2).getMemory_total() * Constants.MAX_THRESHOLD);
			
			while (true) {
				
				//Calcule la quantite de RAM utilisee par mes CTs sur chaque serveur
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
				
				//Si on ne dépasse pas le seuil de 16% de la mémoire totale des deux serveurs: 
				if (memOnServer1 < memAllowedOnServer1 && memOnServer2 < memAllowedOnServer2) {
										
					//Choix aléatoire entre les deux serveurs (66%/33%)
					String serverName;
					if (rndServer.nextFloat() < Constants.CT_CREATION_RATIO_ON_SERVER1)
						serverName = Constants.SERVER1;
					else
						serverName = Constants.SERVER2;
					
					//Création et lancement du container.
					//Si le nom du container qui tente d'etre créé est déjà pris, un autre nom est trouvé.
					//On attend que le container c soit lancé avant de commencer a créer le container c+1.
					boolean started = false ; 
					while (!started) {
						try {
							String ctID = Long.toString(baseID+index) ; 
							api.createCT(serverName, ctID, Constants.CT_BASE_NAME + index, 512);
							System.out.println("Creation of CT" + ctID + "...");
							LXC ct = api.getCT(serverName, ctID) ; 
							System.out.println("Wait until CT is created...");	
							while (!ct.getStatus().equals("running")) {
								try {
									api.startCT(serverName, ctID);
								} catch (Exception e) {
									System.out.println("Start did not work because CT is not created yet. Will try again.") ; 
								}
								Thread.sleep(getNextEventPeriodicSec(2));
								ct = api.getCT(serverName, ctID) ; 
							}
							System.out.println(ct.getName() + " is now running");
							started = true ;
							index++ ; 
						} catch (IOException e) {
							//Le nom du container est déjà pris : on modifie l'index pour trouver un nouveau nom.
							index++ ; 
						}
					}

					//Attendre un peu avant de créer le prochain container.
					Thread.sleep(getNextEventPeriodicSec(10));
				}
				else {
					//Les serveurs sont pleins, attendre un moment avant de retenter de créer un container.
					System.out.println("Servers are full, waiting 5 more seconds ...");
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
