package org.nervos.appchain.crypto;

public abstract class Signature {

    public abstract byte[] getSignature(byte[] tx);

    public abstract String getPublicKey();

    public abstract String getAddress();
}
