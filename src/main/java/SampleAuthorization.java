import io.blocko.coinstack.ECKey;

public class SampleAuthorization {
	
	public static void main(String[] args) throws Exception {
		sampleAuthorization(SampleMain.CLIENT_REGISTED_PKEY);
		sampleAuthorization(SampleMain.CLIENT_REVOKED_PKEY);
	}
	
	public static String sampleAuthorization(String clientPrivateKeyWIF) throws Exception {
		System.out.println("## sampleAuthorization");
		
		OpenkeychainServer server = SampleMain.newServer();
		OpenkeychainClient client = SampleMain.newClient(clientPrivateKeyWIF);
		
		// request challenge
		String challengeSerialized = server.requestChallenge();
		System.out.println("- [client] challenge="+challengeSerialized);
		
		// check challenge and create response
		String responseSerialized = client.challengeResponse(challengeSerialized);
		if (responseSerialized == null) {
			System.out.println("- [client] challenge response failed");
			return null;
		}
		//System.out.println("- [client] response="+responseSerialized);
		
		// request authorization
		String result = server.requestAuthrization(responseSerialized);
		if (result == null) {
			System.out.println("- [client] authorization failed");
			return null;
		}
		System.out.printf("- [client] authorization succeed: addr=%s, pkey=%s\n",
				ECKey.deriveAddress(clientPrivateKeyWIF), clientPrivateKeyWIF);
		return clientPrivateKeyWIF;
	}
	
}
