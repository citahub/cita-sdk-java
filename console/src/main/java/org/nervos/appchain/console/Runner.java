package org.nervos.appchain.console;

import org.nervos.appchain.codegen.Console;
import org.nervos.appchain.codegen.SolidityFunctionWrapperGenerator;
import org.nervos.appchain.codegen.TruffleJsonFunctionWrapperGenerator;
import org.nervos.appchain.utils.Version;

import static org.nervos.appchain.utils.Collection.tail;

/**
 * Main entry point for running command line utilities.
 */

public class Runner {
    private static String USAGE = "Usage: appChainj version|solidity ...";

    private static String LOGO = "\n" // generated at http://patorjk.com/software/taag
            + "                            _             _          _\n"
            + "  __ _  _ __   _ __    ___ | |__    __ _ (_) _ __   (_)\n"
            + " / _` || '_ \\ | '_ \\  / __|| '_ \\  / _` || || '_ \\  | |\n"
            + "| (_| || |_) || |_) || (__ | | | || (_| || || | | | | |\n"
            + " \\__,_|| .__/ | .__/  \\___||_| |_| \\__,_||_||_| |_|_/ |\n"
            + "       |_|    |_|                                 |__/\n";



    public static void main(String[] args) throws Exception {
        System.out.println(LOGO);
        if (args.length < 1) {
            Console.exitError(USAGE);
        } else {
            switch (args[0]) {
                case "solidity":
                    SolidityFunctionWrapperGenerator.run(tail(args));
                    break;
                case "truffle":
                    TruffleJsonFunctionWrapperGenerator.run(tail(args));
                    break;
                case "version":
                    Console.exitSuccess("Version: " + Version.getVersion() + "\n"
                            + "Build timestamp: " + Version.getTimestamp());
                    break;
                default:
                    Console.exitError(USAGE);
            }
        }
    }
}
