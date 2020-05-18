package main.secretShare;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Random;

public final class Shamir
{
    public static SecretShare[] split (final BigInteger secret, int neededNum, int totalNum, BigInteger prime, Random random)
    {
        // secret > the Secret.
        // neededNum > the required Shares.
        // totalNum > the totalNum number of shares.

        //System.out.println("Prime Number: " + prime);

        final BigInteger[] coeff = new BigInteger[neededNum];
        coeff[0] = secret;
        for (int i = 1; i < neededNum; i++)
        {
            BigInteger r;
            while (true)
            {
                r = new BigInteger(prime.bitLength(), random);
                if (r.compareTo(BigInteger.ZERO) > 0 && r.compareTo(prime) < 0)
                {
                    break;
                }
            }
            coeff[i] = r;
        }

        final SecretShare[] shares = new SecretShare[totalNum];
        for (int x = 1; x <= totalNum; x++) // for each share of the totalNum shares
        {
            BigInteger accum = secret;

            for (int exp = 1; exp < neededNum; exp++) // for each
            {
                accum = accum.add(coeff[exp].multiply(BigInteger.valueOf(x).pow(exp).mod(prime))).mod(prime);
            }
            shares[x - 1] = new SecretShare (x, accum);
            //System.out.println(shares[x - 1]);
        }
        return shares;
    }

    public static String combine (final SecretShare[] shares, final BigInteger prime)
    {
        BigInteger accum = BigInteger.ZERO;

        for(int formula = 0; formula < shares.length; formula++)
        {
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for(int count = 0; count < shares.length; count++)
            {
                if(formula == count)
                    continue; // If not the same value

                int startposition = shares[formula].getNumber();
                int nextposition = shares[count].getNumber();

                numerator = numerator.multiply(BigInteger.valueOf(nextposition).negate()).mod(prime); // (numerator * -nextposition) % prime;
                denominator = denominator.multiply(BigInteger.valueOf(startposition - nextposition)).mod(prime); // (denominator * (startposition - nextposition)) % prime;
            }
            BigInteger value = shares[formula].getShare();
            BigInteger tmp = value.multiply(numerator) . multiply(modInverse(denominator, prime));
            accum = prime.add(accum).add(tmp) . mod(prime); //  (prime + accum + (value * numerator * modInverse(denominator))) % prime;
        }
        String secret = new String (accum.toByteArray ());
        //System.out.println("The secret is: " + secret + "\n");

        return secret;
    }

    private static BigInteger[] gcdD(BigInteger a, BigInteger b)
    {
        if (b.compareTo(BigInteger.ZERO) == 0)
            return new BigInteger[] {a, BigInteger.ONE, BigInteger.ZERO};
        else
        {
            BigInteger n = a.divide(b);
            BigInteger c = a.mod(b);
            BigInteger[] r = gcdD(b, c);
            return new BigInteger[] {r[0], r[2], r[1].subtract(r[2].multiply(n))};
        }
    }

    private static BigInteger modInverse(BigInteger k, BigInteger prime)
    {
        k = k.mod(prime);
        BigInteger r = (k.compareTo(BigInteger.ZERO) == -1) ? (gcdD(prime, k.negate())[2]).negate() : gcdD(prime,k)[2];
        return prime.add(r).mod(prime);
    }

    public  BigInteger stringToBigInteger(String string){
        byte[] asciiCharacters = string.getBytes (StandardCharsets.US_ASCII);
        StringBuilder asciiString = new StringBuilder();
        for(byte asciiCharacter:asciiCharacters){
            asciiString.append(Byte.toString(asciiCharacter));
        }
        BigInteger bigInteger = new BigInteger(asciiString.toString());
        return bigInteger;
    }

    private static String generateShamirShares (String secret, int nedNuShares, int totNuShares)
    {
        final BigInteger bigIntegerPassword = new BigInteger(secret.getBytes ());
        final int CERTAINTY = 256;
        final SecureRandom random = new SecureRandom();
        final BigInteger prime = new BigInteger(bigIntegerPassword.bitLength() + 1, CERTAINTY, random);
        final SecretShare[] shares = Shamir.split (bigIntegerPassword, nedNuShares, totNuShares, prime, random);

        String coPrimeNum = nedNuShares + "P:" + prime;

        StringBuilder primeAndShares = new StringBuilder (coPrimeNum);

        String[] sharei = new String [totNuShares];
        for (SecretShare share: shares)
        {
            String coShare = share.getNumber () + ":" + share.getShare ();
            sharei[share.getNumber ()-1] = coShare;

            primeAndShares.append ("\n").append (coShare);
        }

        return String.valueOf (primeAndShares);
    }

    private static String combineShamirShares (String prime, String[] shares)
    {
        String strNumOfShares = prime.substring (0,prime.indexOf ("P:"));
        int avaSharesNum = Integer.parseInt (strNumOfShares);

        if (shares.length == avaSharesNum)
        {
            SecretShare[] sharesToViewSecret = new SecretShare[avaSharesNum];
            for (int i=0; i<avaSharesNum; i++)
            {
                String coShareStr = shares[i].trim ();
                String shareStr = coShareStr.substring (coShareStr.indexOf (":")+1);
                int shareNum = Integer.parseInt (coShareStr.substring (0,coShareStr.indexOf (":")));
                sharesToViewSecret[i] = new SecretShare (shareNum, new BigInteger (shareStr));
            }
            String coPrimeStr = prime.trim ();
            String primeStr = coPrimeStr.substring (coPrimeStr.indexOf (":")+1);
            //int primeNum = Integer.parseInt (coPrimeStr.substring (0,coPrimeStr.indexOf ("P:")));
            BigInteger bigIntegerPrime = new BigInteger(primeStr);
            String result = Shamir.combine (sharesToViewSecret, bigIntegerPrime);
            return result;
        }
        return ("Sorry, you have to provide " + avaSharesNum + " shares in order to reconstruct your " +
                "secret.");
    }


    public static void main(final String[] args)
    {
        System.out.println (generateShamirShares ("Password@123", 4, 12));

        System.out.println (combineShamirShares ("4P:57227141335498039497719664191",
                                                 new String[]{"1:46408014906759654811274101243",
                                                              "5:35533534891431853551600686081",
                                                              "3:56475036329164407840164095940",
                                                              "4:3124698633283214142142432517"}));
    }
}