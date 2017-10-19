package org.ctlv.proxmox.monitor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONException;

public class Controler implements Runnable {
	
	private ProxmoxAPI api ; 
	
	private Organizer org ; 
	
	public Controler(ProxmoxAPI api, Organizer org) {
		this.api = api ; 
		this.org = org ; 
	}
	
	
	@Override
	public void run() {

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
		
		List <LXC> ctSrv1 = api.getCTs(Constants.SERVER1) ;
		List <LXC> ctSrv2 = api.getCTs(Constants.SERVER2) ; 
		
		Map <String, List<LXC>> myCTs = new HashMap<String, List<LXC>>() ; 
		
		Iterator <LXC> it1 = ctSrv1.iterator() ; 
		LXC curr1 ; 
		while (it1.hasNext()) {
			curr1 = it1.next() ;
			if (!curr1.getName().startsWith(Constants.CT_BASE_NAME)) {
				ctSrv1.remove(curr1) ; 
			}
		}
		
		myCTs.put(Constants.SERVER1, ctSrv1) ; 
		
		Iterator <LXC> it2 = ctSrv1.iterator() ; 
		LXC curr2 ; 
		while (it2.hasNext()) {
			curr2 = it2.next() ;
			if (!curr2.getName().startsWith(Constants.CT_BASE_NAME)) {
				ctSrv1.remove(curr2) ; 
			}
		}
		
		myCTs.put(Constants.SERVER2, ctSrv2) ;
		
		return myCTs ; 
		
	}
}
