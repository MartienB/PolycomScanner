import java.util.ArrayList;
import java.util.Iterator;
import java.net.InetAddress;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PolycomScanner {

    public static void main (String[] args) {
        if (args.length == 2) {
			String fromIp = args[0];
			String toIp = args[1];
			scanIps(ipRange(fromIp, toIp));
        }else {
        	System.out.println("Correct usage: PolycomScanner [fromIp] [toIp]");
        }
    }
    
    public static void scanIps (ArrayList<Integer> ips) {
    	System.out.printf("Found Polycom (with default credentials) at:\n");
		for (int i=0; i < ips.size(); i++) {
			try {
				String address = intToIp(ips.get(i));
				InetAddress ip = InetAddress.getByName(address);

				if (ip.isReachable(100)) { // Try for one tenth of a second
					String url = ("http:/"+ip.toString()+"/reg_1.htm");
					String encoding = "UG9seWNvbTo0NTY="; // Default credentials of Polycom phones
					URL obj = new URL(url);
					HttpURLConnection con = (HttpURLConnection) obj.openConnection();

					// optional default is GET
					con.setRequestMethod("GET");

					//add request header
					con.setRequestProperty  ("Authorization", "Basic " + encoding);
					int responseCode = con.getResponseCode();

					if (responseCode == 200){ // Login succeeded (or none required)
					
						// Parse response
						BufferedReader in = new BufferedReader(
								new InputStreamReader(con.getInputStream()));
						String inputLine;
						StringBuffer response = new StringBuffer();

						while ((inputLine = in.readLine()) != null) {
							response.append(inputLine);
						}
						in.close();
						
						String responseString = response.toString();
						// Check if response contains the word "Polycom"
						//System.out.printf("%s\n", responseString);
						if (responseString.toLowerCase().contains("Polycom".toLowerCase())){

							String name = responseString.substring(responseString.indexOf("reg.1.displayName") + 26, responseString.indexOf("reg.1.displayName") + 80);
							String []splittedName = name.split("\"");
							String []splittedIP = ip.toString().split("/");
							System.out.printf("%s: ", splittedIP[1]); // Print IP of found device.
							System.out.printf("%s\n", splittedName[0]);
						}
					}
				}
			}catch (Exception e){
				//Do nothing
			}
		}
		System.out.printf("Done scanning!\n\n");
	}
    
    public static ArrayList<Integer> ipRange(String fromIp, String toIp) {
    	int fromIpInt = ipToInt(fromIp);
    	int toIpInt = ipToInt(toIp);
    	
    	ArrayList<Integer> result = new ArrayList<Integer>();
    	for (int i = fromIpInt; i <= toIpInt; i++) {
    		result.add(i);
    	}
    	
        return result;
    }
    
    public static int ipToInt(String ipAddress) {
        try {
			byte[] bytes = InetAddress.getByName(ipAddress).getAddress();
			int octet1 = (bytes[0] & 0xFF) << 24;
			int octet2 = (bytes[1] & 0xFF) << 16;
			int octet3 = (bytes[2] & 0xFF) << 8;
			int octet4 = bytes[3] & 0xFF;
			int address = octet1 | octet2 | octet3 | octet4;

			return address;
		} catch (Exception e) {
			e.printStackTrace();

			return 0;
		}
    }
    
    public static String intToIp(int ipAddress) {
            int octet1 = (ipAddress & 0xFF000000) >>> 24;
            int octet2 = (ipAddress & 0xFF0000) >>> 16;
            int octet3 = (ipAddress & 0xFF00) >>> 8;
            int octet4 = ipAddress & 0xFF;

            return new StringBuffer().append(octet1).append('.').append(octet2)
                                     .append('.').append(octet3).append('.')
                                     .append(octet4).toString();
    }
}