package org.nervos.appchain.console;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.nervos.appchain.crypto.Credentials;
import org.nervos.appchain.crypto.WalletUtils;
import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.NervosjFactory;
import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.appchain.protocol.core.methods.response.Web3ClientVersion;
import org.nervos.appchain.protocol.exceptions.TransactionException;
import org.nervos.appchain.protocol.http.HttpService;
import org.nervos.appchain.protocol.infura.InfuraHttpService;
import org.nervos.appchain.tx.Transfer;
import org.nervos.appchain.utils.Convert;

import static org.nervos.appchain.codegen.Console.exitError;

/**
 * Simple class for creating a wallet file.
 */
public class WalletSendFunds extends WalletManager {

    private static final String USAGE = "send <walletfile> <destination-address>";

    public static void main(String[] args) {
        if (args.length != 2) {
            exitError(USAGE);
        } else {
            new WalletSendFunds().run(args[0], args[1]);
        }
    }

    private void run(String walletFileLocation, String destinationAddress) {
        File walletFile = new File(walletFileLocation);
        Credentials credentials = getCredentials(walletFile);
        console.printf("Wallet for address " + credentials.getAddress() + " loaded\n");

        if (!WalletUtils.isValidAddress(destinationAddress)) {
            exitError("Invalid destination address specified");
        }

        Nervosj nervosj = getEthereumClient();

        BigDecimal amountToTransfer = getAmountToTransfer();
        Convert.Unit transferUnit = getTransferUnit();
        BigDecimal amountInWei = Convert.toWei(amountToTransfer, transferUnit);

        confirmTransfer(amountToTransfer, transferUnit, amountInWei, destinationAddress);

        TransactionReceipt transactionReceipt = performTransfer(
                nervosj, destinationAddress, credentials, amountInWei);

        console.printf("Funds have been successfully transferred from %s to %s%n"
                        + "Transaction hash: %s%nMined block number: %s%n",
                credentials.getAddress(),
                destinationAddress,
                transactionReceipt.getTransactionHash(),
                transactionReceipt.getBlockNumber());
    }

    private BigDecimal getAmountToTransfer() {
        String amount = console.readLine("What amound would you like to transfer "
                + "(please enter a numeric value): ")
                .trim();
        try {
            return new BigDecimal(amount);
        } catch (NumberFormatException e) {
            exitError("Invalid amount specified");
        }
        throw new RuntimeException("Application exit failure");
    }

    private Convert.Unit getTransferUnit() {
        String unit = console.readLine("Please specify the unit (ether, wei, ...) [ether]: ")
                .trim();

        Convert.Unit transferUnit;
        if (unit.equals("")) {
            transferUnit = Convert.Unit.ETHER;
        } else {
            transferUnit = Convert.Unit.fromString(unit.toLowerCase());
        }

        return transferUnit;
    }

    private void confirmTransfer(
            BigDecimal amountToTransfer, Convert.Unit transferUnit, BigDecimal amountInWei,
            String destinationAddress) {

        console.printf("Please confim that you wish to transfer %s %s (%s %s) to address %s%n",
                amountToTransfer.stripTrailingZeros().toPlainString(), transferUnit,
                amountInWei.stripTrailingZeros().toPlainString(),
                Convert.Unit.WEI, destinationAddress);
        String confirm = console.readLine("Please type 'yes' to proceed: ").trim();
        if (!confirm.toLowerCase().equals("yes")) {
            exitError("OK, some other time perhaps...");
        }
    }

    private TransactionReceipt performTransfer(
            Nervosj nervosj, String destinationAddress, Credentials credentials,
            BigDecimal amountInWei) {

        console.printf("Commencing transfer (this may take a few minutes) ");
        try {
            Future<TransactionReceipt> future = Transfer.sendFunds(
                    nervosj, credentials, destinationAddress, amountInWei, Convert.Unit.WEI)
                    .sendAsync();

            while (!future.isDone()) {
                console.printf(".");
                Thread.sleep(500);
            }
            console.printf("$%n%n");
            return future.get();
        } catch (InterruptedException | ExecutionException | TransactionException | IOException e) {
            exitError("Problem encountered transferring funds: \n" + e.getMessage());
        }
        throw new RuntimeException("Application exit failure");
    }

    private Nervosj getEthereumClient() {
        String clientAddress = console.readLine(
                "Please confirm address of running Ethereum client you wish to send "
                + "the transfer request to [" + HttpService.DEFAULT_URL + "]: ")
                .trim();

        Nervosj nervosj;
        if (clientAddress.equals("")) {
            nervosj = NervosjFactory.build(new HttpService());
        } else if (clientAddress.contains("infura.io")) {
            nervosj = NervosjFactory.build(new InfuraHttpService(clientAddress));
        } else {
            nervosj = NervosjFactory.build(new HttpService(clientAddress));
        }

        try {
            Web3ClientVersion web3ClientVersion = nervosj.web3ClientVersion().sendAsync().get();
            if (web3ClientVersion.hasError()) {
                exitError("Unable to process response from client: "
                        + web3ClientVersion.getError());
            } else {
                console.printf("Connected successfully to client: %s%n",
                        web3ClientVersion.getWeb3ClientVersion());
                return nervosj;
            }
        } catch (InterruptedException | ExecutionException e) {
            exitError("Problem encountered verifying client: " + e.getMessage());
        }
        throw new RuntimeException("Application exit failure");
    }
}
