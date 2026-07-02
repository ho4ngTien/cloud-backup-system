package com.cloudsafe.cli.command;

import com.cloudsafe.cli.ApiClient;
import picocli.CommandLine.*;

import java.util.Map;

@Command(
    name = "login",
    description = "Authenticate with CloudSafe server and save JWT token locally",
    mixinStandardHelpOptions = true
)
public class LoginCommand implements Runnable {

    @Option(names = {"-e", "--email"}, description = "Your account email", required = true, interactive = false)
    private String email;

    @Option(names = {"-p", "--password"}, description = "Your account password",
            required = true, interactive = true, arity = "0..1")
    private char[] password;

    @Option(names = {"-s", "--server"}, description = "Server URL (default: http://localhost:8080)",
            defaultValue = "http://localhost:8080")
    private String serverUrl;

    @Override
    public void run() {
        ApiClient client = new ApiClient(serverUrl);
        try {
            Map<String, Object> resp = client.login(email, new String(password));
            System.out.println();
            System.out.println("✅ Login successful!");
            System.out.println("   Welcome, " + resp.get("displayName"));
            System.out.println("   Email : " + resp.get("email"));
            System.out.println("   Token saved to ~/.cloudsafe/config.json");
            System.out.println();
        } catch (Exception e) {
            System.err.println("❌ Login failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
