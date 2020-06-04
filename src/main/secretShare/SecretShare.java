package main.secretShare;

import java.math.BigInteger;

public class SecretShare
{
    private final int shareNumber;
    private final BigInteger shareValue;

    public SecretShare (final int shareNumber, final BigInteger shareValue)
    {
        this.shareNumber = shareNumber;
        this.shareValue = shareValue;
    }

    public int getShareNumber ()
    {
        return shareNumber;
    }

    public BigInteger getShareValue ()
    {
        return shareValue;
    }

    @Override
    public String toString()
    {
        return "Share" + shareNumber + ": " + shareValue;
    }
}