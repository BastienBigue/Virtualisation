package org.ctlv.proxmox.monitor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.ctlv.proxmox.generator.Generator;
import org.json.JSONException;

public class Controler {

	private ProxmoxAPI api ; 

	private Organizer org ; 

	public Controler(ProxmoxAPI api, Organizer org) {
		this.api = api ; 
		this.org = org ; 
	}

	public void control() {

		Monitor monitor = new Monitor(api, org); 

		Map<String, List<LXC>> myCTs = null;

		while (true) {	
			try {
				myCTs = this.findMyCTs() ;
				monitor.analyze(myCTs);
			} catch (LoginException | JSONException | IOException e1) {
				e1.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				Thread.sleep(Constants.MONITOR_PERIOD * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}


	Map <String, List<LXC>> findMyCTs() throws LoginException, JSONException, IOException {

		Map <String, List<LXC>> myCTs = new HashMap<String, List<LXC>>() ; 

		//Find CT in srv 1.
		List <LXC> ctSrv1 = api.getCTs(Constants.SERVER1) ;

		Iterator <LXC> it1 = ctSrv1.iterator() ; 
		LXC curr1 ; 
		while (it1.hasNext()) {
			curr1 = it1.next() ;
			if (!curr1.getName().startsWith(Constants.CT_BASE_NAME)) {
				it1.remove() ; 
			}
		}

		myCTs.put(Constants.SERVER1, ctSrv1) ;

		//Find CT in srv 2.
		List <LXC> ctSrv2 = api.getCTs(Constants.SERVER2) ; 

		Iterator <LXC> it2 = ctSrv2.iterator() ; 
		LXC curr2 = null; 
		while (it2.hasNext()) {
			curr2 = it2.next() ;
			if (!curr2.getName().startsWith(Constants.CT_BASE_NAME)) {
				it2.remove() ; 
			}
		}

		myCTs.put(Constants.SERVER2, ctSrv2) ;

		return myCTs ; 

	}

	public void stopAllMyCT() {
		Map <String, List<LXC>> myCTs = null ; 
		try {
			myCTs = this.findMyCTs() ; 
			Iterator<Entry<String, List<LXC>>> it = myCTs.entrySet().iterator() ; 
			while (it.hasNext()) {
				Entry<String, List<LXC>> currSrv = it.next() ; 
				List<LXC> CTsOnSrv = currSrv.getValue() ; 
				String srv = currSrv.getKey() ;
				Iterator<LXC> it2 = CTsOnSrv.iterator() ; 
				while (it2.hasNext()) {
					LXC currCT = it2.next() ;
					if (currCT.getStatus().equals("running")) { 
						api.stopCT(srv, currCT.getVmid());
						System.out.println("CT" + currCT.getVmid() + " has been stopped");
					}
				}
			}
		} catch (LoginException | JSONException | IOException e) {
			e.printStackTrace();
		}
	}

	public void displayMyCTs() {
		Map <String, List<LXC>> myCTs = null ; 
		try {
			myCTs = this.findMyCTs() ; 
			Iterator<Entry<String, List<LXC>>> it = myCTs.entrySet().iterator() ; 
			while (it.hasNext()) {
				Entry<String, List<LXC>> currSrv = it.next() ; 
				List<LXC> CTsOnSrv = currSrv.getValue() ; 
				String srv = currSrv.getKey() ;
				System.out.println("Server : " + srv);
				Iterator<LXC> it2 = CTsOnSrv.iterator() ; 
				while (it2.hasNext()) {
					LXC currCT = it2.next() ;
					System.out.println("CT : " + currCT.getName());
				}
			}
		} catch (LoginException | JSONException | IOException e) {
			e.printStackTrace();
		}
	}

	public void startAllMyCTs() {
		Map <String, List<LXC>> myCTs = null ; 
		try {
			myCTs = this.findMyCTs() ; 
			Iterator<Entry<String, List<LXC>>> it = myCTs.entrySet().iterator() ; 
			while (it.hasNext()) {
				Entry<String, List<LXC>> currSrv = it.next() ; 
				List<LXC> CTsOnSrv = currSrv.getValue() ; 
				String srv = currSrv.getKey() ;
				Iterator<LXC> it2 = CTsOnSrv.iterator() ; 
				while (it2.hasNext()) {
					LXC currCT = it2.next() ;
					System.out.println("Wait until CT " + currCT.getName() + " on server " + srv+  "  is running...");
					while (!currCT.getStatus().equals("running")) {
						api.startCT(srv, currCT.getVmid());
						currCT = api.getCT(srv, currCT.getVmid()) ; 
						Thread.sleep(Generator.getNextEventPeriodicSec(3));
					}
					System.out.println("CT" + currCT.getName() + " is now running");
				}
			}
		} catch (LoginException | JSONException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}


	/**
	 * To execute the project : launch the following main and the main situated in Generator file.
	 * Do this with 2 threads was not working. We had to make two processus.
	 */
	public static void main(String[] args) {

		ProxmoxAPI api = new ProxmoxAPI() ; 

		Organizer organizer = new Organizer(api) ; 

		Controler controler = new Controler(api, organizer) ;

		//controler.startAllMyCTs();

		//controler.stopAllMyCT(); 

		controler.control();



	}

}
