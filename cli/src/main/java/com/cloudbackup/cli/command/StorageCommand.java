package com.cloudsafe.cli.command;

import com.cloudsafe.cli.ApiClient;
import picocli.CommandLine.*;
import java.util.Map;

@Command(name = "storage", description = "Show storage usage and quota",
         mixinStandardHelpOptions = true)
public class StorageCommand implements Runnable {
    @Override
    public void run() {
        ApiClient client = new ApiClient();
        if (!client.isLoggedIn()) {
            System.err.println("❌ Not logged in. Run: cloudsafe login"); System.exit(1);
        }
        try {
            Map<String, Object> usage = client.getStorage();
            long used  = ((Number) usage.get("usedBytes")).longValue();
            long quota = ((Number) usage.get("quotaBytes")).longValue();
            double pct = ((Number) usage.get("usedPercent")).doubleValue();
            System.out.println();
            System.out.println("☁️  CloudSafe Storage");
            System.out.println("   Used      : " + formatBytes(used));
            System.out.println("   Quota     : " + formatBytes(quota));
            System.out.println("   Available : " + formatBytes(quota - used));
            System.out.printf ("   Used %%   : %.1f%%%n", pct);
            System.out.println();
            printBar(pct);
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage()); System.exit(1);
        }
    }
    private String formatBytes(long bytes) {
        if (bytes >= 1_073_741_824L) return String.format("%.2f GB", bytes / 1_073_741_824.0);
        if (bytes >= 1_048_576L)    return String.format("%.2f MB", bytes / 1_048_576.0);
        return bytes + " B";
    }
    private void printBar(double pct) {
        int filled = (int) Math.round(pct / 5); // 20 chars = 100%
        System.out.print("   [");
        for (int i = 0; i < 20; i++) System.out.print(i < filled ? "█" : "░");
        System.out.printf("] %.1f%%%n%n", pct);
    }
}
