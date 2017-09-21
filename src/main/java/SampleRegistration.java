import io.blocko.coinstack.ECKey;

public class SampleRegistration {
	
	public static void main(String[] args) throws Exception {
		sampleRegistration();
	}
	
	public static String sampleRegistration() throws Exception {
		System.out.println("## sampleRegistration");
		
		String clientPrivateKeyWIF = null;
		OpenkeychainServer server = SampleMain.newServer();
		OpenkeychainClient client = SampleMain.newClient(clientPrivateKeyWIF);
		
		// request challenge
		String challengeSerialized = server.requestChallenge();
		System.out.println("- [client] challenge="+challengeSerialized);
		
		// new key for registration
		String newPrivKey = ECKey.createNewPrivateKey();
		// check challenge and create response
		String responseSerialized = client.challengeResponse(challengeSerialized, newPrivKey);
		System.out.println("- [client] response="+responseSerialized);
		
		// request authorization
		String result = server.requestRegistration(responseSerialized);
		if (result == null) {
			System.out.println("- [client] registration failed");
			return null;
		}
		System.out.printf("- [client] registration succeed, addr=%s, pkey=%s\n",
				ECKey.deriveAddress(newPrivKey), newPrivKey);
		return newPrivKey;
	}
	
}
