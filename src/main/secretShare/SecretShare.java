package main.secretShare;

import java.math.BigInteger;

public class SecretShare
{
    public SecretShare(final int number, final BigInteger share)
    {
        this.number = number;
        this.share = share;
    }

    public int getNumber()
    {
        return number;
    }

    public BigInteger getShare()
    {
        return share;
    }

    @Override
    public String toString()
    {
        return "Share"+ number + ": " + share;
    }

    private final int number;
    private final BigInteger share;
}