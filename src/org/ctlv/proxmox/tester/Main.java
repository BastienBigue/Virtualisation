package org.ctlv.proxmox.tester;



import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.monitor.Controler;
import org.ctlv.proxmox.monitor.Organizer;

public class Main {	
	
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


	/*public static void main(String[] args) throws LoginException, JSONException, IOException {

		ProxmoxAPI api = new ProxmoxAPI();		
		
		List<LXC> cts = api.getCTs("srv-px5");
		long cpu = 0 ; 
		long disk = 0 ;
		long memory = 0 ; 
		for (LXC lxc : cts) {
			cpu += lxc.getCpu() ; 
			disk += lxc.getDisk() ; 
			memory += lxc.getMem() ; 
			System.out.println(lxc.getName());
		}
		
		
		System.out.println("srv-px5 : Disk : " + disk + "- CPU :" + cpu + "- Memory : " + memory);
		Node srv = api.getNode("srv-px5") ; 
		System.out.println("srv-px5 - CPU :" + srv.getCpu()  + " - Used Memory : " + 100 * (float) srv.getMemory_used() / (float) srv.getMemory_total()) ; 
		
		System.out.println("srv-px5 - % Used CPU :" + 100 * (float) cpu/ (float) srv.getCpu()  + " - % Used Memory : " + 100 * (float) memory / (float)srv.getMemory_total()) ; 
		
		api.createCT("srv-px5", "21101", "ct-tpgei-ctlv-B11-ct3", 512);
		
		LXC ct1 = api.getCT("srv-px5", "21101") ; 
		//api.startCT("srv-px5", "21101");
		
		for (LXC lxc : cts) {
			cpu += lxc.getCpu() ; 
			disk += lxc.getDisk() ; 
			memory += lxc.getMem() ; 
			System.out.println(lxc.getName() + lxc.getStatus());
		}
		
	}*/