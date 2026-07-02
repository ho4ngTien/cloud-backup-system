package com.cloudsafe.cli.command;

import com.cloudsafe.cli.ApiClient;
import picocli.CommandLine.*;
import java.util.*;

@Command(name = "history", description = "Show backup history",
         mixinStandardHelpOptions = true)
public class HistoryCommand implements Runnable {

    @Option(names = {"-n", "--count"}, description = "Number of records to show (default: 10)",
            defaultValue = "10")
    private int count;

    @Override
    public void run() {
        ApiClient client = new ApiClient();
        if (!client.isLoggedIn()) {
            System.err.println("❌ Not logged in. Run: cloudsafe login"); System.exit(1);
        }
        try {
            Map<String, Object> resp = client.getHistory();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content =
                    (List<Map<String, Object>>) resp.getOrDefault("content", List.of());

            System.out.println();
            System.out.println("☁️  CloudSafe Backup History");
            System.out.printf("   %-36s  %-12s  %-10s  %-8s  %s%n",
                    "Job ID", "Type", "Status", "Files", "Created");
            System.out.println("   " + "-".repeat(90));

            content.stream().limit(count).forEach(job -> {
                System.out.printf("   %-36s  %-12s  %-10s  %-8s  %s%n",
                        job.get("id"),
                        job.getOrDefault("backupType", "–"),
                        job.getOrDefault("status", "–"),
                        job.getOrDefault("fileCount", 0),
                        ((String) job.getOrDefault("createdAt", "")).substring(0, 19));
            });
            System.out.println();

            Number total = (Number) resp.getOrDefault("totalElements", 0);
            System.out.println("   Total records: " + total);
            System.out.println();
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage()); System.exit(1);
        }
    }
}
