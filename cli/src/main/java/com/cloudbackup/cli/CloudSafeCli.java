package com.cloudsafe.cli;

import com.cloudsafe.cli.command.*;
import picocli.CommandLine;
import picocli.CommandLine.*;

/**
 * CloudSafe CLI – Main entry point.
 *
 * Usage:
 *   java -jar cloudsafe.jar <command> [options]
 *
 * Commands:
 *   login    – Authenticate and save JWT token locally
 *   backup   – Upload files to cloud backup
 *   restore  – Download and restore files from cloud
 *   storage  – Show storage usage
 *   history  – Show backup history
 */
@Command(
    name = "cloudsafe",
    description = "☁️  CloudSafe – Cloud Backup & Disaster Recovery CLI",
    mixinStandardHelpOptions = true,
    version = "CloudSafe CLI 1.0.0",
    subcommands = {
        LoginCommand.class,
        BackupCommand.class,
        RestoreCommand.class,
        StorageCommand.class,
        HistoryCommand.class,
        DaemonCommand.class,
        SyncCommand.class,
        CommandLine.HelpCommand.class
    }
)
public class CloudSafeCli implements Runnable {

    @Override
    public void run() {
        // Print banner when run with no subcommand
        System.out.println();
        System.out.println("  ☁️  CloudSafe CLI v1.0.0");
        System.out.println("  Cloud Backup & Disaster Recovery");
        System.out.println();
        System.out.println("  Run 'cloudsafe --help' to see available commands.");
        System.out.println();
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new CloudSafeCli())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(args);
        System.exit(exitCode);
    }
}
