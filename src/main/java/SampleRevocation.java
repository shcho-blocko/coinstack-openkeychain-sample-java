import io.blocko.coinstack.ECKey;

public class SampleRevocation {

	public static void main(String[] args) throws Exception {
		sampleRevocation(ECKey.createNewPrivateKey());
	}
	
	public static String sampleRevocation(String clientPrivateKeyWIF) throws Exception {
		System.out.println("## sampleRevocation");
		
		OpenkeychainServer server = SampleMain.newServer();
		OpenkeychainClient client = SampleMain.newClient(clientPrivateKeyWIF);
		
		// request challenge
		String challengeSerialized = server.requestChallenge();
		System.out.println("- [client] challenge="+challengeSerialized);
		
		// check challenge and create response
		String responseSerialized = client.challengeResponse(challengeSerialized);
		System.out.println("- [client] response="+responseSerialized);
		
		// request authorization
		String result = server.requestRevocation(responseSerialized);
		if (result == null) {
			System.out.println("- [client] revocation failed");
			return null;
		}
		System.out.printf("- [client] revocation succeed: addr=%s, pkey=%s\n",
				ECKey.deriveAddress(clientPrivateKeyWIF), clientPrivateKeyWIF);
		return clientPrivateKeyWIF;
	}

}
