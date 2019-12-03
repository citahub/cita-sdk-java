package com.cryptape.cita.console;

import com.cryptape.cita.codegen.Console;
import com.cryptape.cita.codegen.SolidityFunctionWrapperGenerator;
import com.cryptape.cita.codegen.TruffleJsonFunctionWrapperGenerator;
import com.cryptape.cita.utils.Collection;
import com.cryptape.cita.utils.Version;

/**
 * Main entry point for running command line utilities.
 */

public class Runner {
    private static String USAGE = "Usage: citaj version|solidity ...";

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
                    SolidityFunctionWrapperGenerator.run(Collection.tail(args));
                    break;
                case "truffle":
                    TruffleJsonFunctionWrapperGenerator.run(Collection.tail(args));
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
