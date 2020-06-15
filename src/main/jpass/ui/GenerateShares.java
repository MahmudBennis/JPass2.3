package main.jpass.ui;

import main.jpass.ui.helper.EntryHelper;
import main.jpass.ui.helper.FileHelper;
import main.jpass.util.CryptUtils;
import main.jpass.util.SpringUtilities;
import main.jpass.xml.bind.Entry;
import main.secretShare.SecretShare;
import main.secretShare.Shamir;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;

import javax.swing.*;

import static main.jpass.ui.helper.EntryHelper.copyEntryField;

public class GenerateShares extends JDialog implements ActionListener
{
    private static final long serialVersionUID = -8551022862532925034L;

    private final JPanel fieldPanel;
    private final JPanel buttonsPanel;

    private final JPasswordField passwordField;
    private final JTextField filesLink;
    private final JTextField totalShares;
    private final JTextField neededShares;
    private final JButton generatePasswordButton;
    private  JButton[] copyShareButton;
    private JButton copyAllButton;
    private JButton exportButton;
    private JButton generateSharesButton;
    private JToggleButton[] showShareButton;
    private JToggleButton showPasswordButton;
    private JPasswordField[] sharei;
    private JPasswordField primeNum;
    private JButton copyPrimeButton;
    private JToggleButton showPrimeButton;

    private String secMasterPassword;
    private String linkToFiles;
    private int totNuShares;
    private int nedNuShares;

    String primeAndAllShares = "";

    private Entry formData;

    private char ORIGINAL_ECHO;
    private static final char NULL_ECHO = '\0';

    private JPassFrame parent;


    public GenerateShares (final JPassFrame parent, final String title)
    {
        super(parent, title, true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        this.parent = parent;

        this.formData = null;

        JPanel northPanel = new JPanel (new SpringLayout ());
        this.fieldPanel = new JPanel(new SpringLayout ());

        this.fieldPanel.add(new JLabel("Second Master Password:"));
        this.passwordField = TextComponentFactory.newPasswordField(true);
        this.passwordField.setEditable (false);
        this.ORIGINAL_ECHO = this.passwordField.getEchoChar();
        this.fieldPanel.add(this.passwordField);

        this.fieldPanel.add(new JLabel("Link where to find files:"));
        this.filesLink = TextComponentFactory.newTextField();
        this.fieldPanel.add(this.filesLink);

        this.fieldPanel.add(new JLabel("Total Number of Shares:"));
        this.totalShares = TextComponentFactory.newTextField();
        this.fieldPanel.add(this.totalShares);

        this.fieldPanel.add(new JLabel("Needed Shares:"));
        this.neededShares = TextComponentFactory.newTextField ();
        this.fieldPanel.add (this.neededShares);

        this.buttonsPanel = new JPanel(new GridLayout ());

        this.showPasswordButton = new JToggleButton("Show", MessageDialog.getIcon ("show"));
        this.showPasswordButton.setActionCommand ("show_button");
        this.showPasswordButton.setMnemonic (KeyEvent.VK_S);
        this.showPasswordButton.addActionListener (this);
        this.buttonsPanel.add (this.showPasswordButton);

        this.generatePasswordButton = new JButton("Generate", MessageDialog.getIcon ("generate"));
        this.generatePasswordButton.setActionCommand ("generate_Password_button");
        this.generatePasswordButton.setMnemonic (KeyEvent.VK_G);
        this.generatePasswordButton.addActionListener (this);
        this.buttonsPanel.add (this.generatePasswordButton);

//        copyAllButton = new JButton("Copy All", MessageDialog.getIcon("keyring"));
//        copyAllButton.setActionCommand("copy_all_button");
//        copyAllButton.setMnemonic(KeyEvent.VK_C);
//        copyAllButton.addActionListener(this);
//        copyAllButton.setEnabled (false);
//        this.buttonsPanel.add (this.copyAllButton);

        generateSharesButton = new JButton("Submit", MessageDialog.getIcon("accept"));
        generateSharesButton.setActionCommand("generate_shares_button");
        generateSharesButton.setMnemonic(KeyEvent.VK_C);
        generateSharesButton.addActionListener(this);
        this.buttonsPanel.add (this.generateSharesButton);

        exportButton = new JButton("Export", MessageDialog.getIcon("export"));
        exportButton.setActionCommand("export_button");
        exportButton.setMnemonic(KeyEvent.VK_C);
        exportButton.addActionListener(this);
        exportButton.setEnabled (false);
        this.buttonsPanel.add (this.exportButton);

        northPanel.add (this.fieldPanel);
        northPanel.add (this.buttonsPanel);
        SpringUtilities.makeCompactGrid (this.fieldPanel,4,2,5,5,5,5);
        SpringUtilities.makeCompactGrid (northPanel,2,1,5,5,5,5);
        getContentPane().add (northPanel, BorderLayout.NORTH);

        setSize(450, 400);
        setMinimumSize(new Dimension(370, 300));
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void setFormData(Entry formData)
    {
        this.formData = formData;
    }

    public Entry getFormData ()
    {
        return this.formData;
    }

    @Override
    public void actionPerformed (ActionEvent e)
    {
        String command = e.getActionCommand ();
        if ("generate_shares_button".equals (command))
        {
            generateShares ();
        }
        if ("generate_Password_button".equals (command))
        {
            GeneratePasswordDialog gpd = new GeneratePasswordDialog (this);
            String generatedPassword = gpd.getGeneratedPassword ();
            if (generatedPassword != null && !generatedPassword.isEmpty ())
            {
                this.passwordField.setText (generatedPassword);
            }
        }
        if ("show_button".equals (command))
        {
            this.passwordField.setEchoChar(this.showPasswordButton.isSelected() ? NULL_ECHO : this.ORIGINAL_ECHO);
        }
        if ((command).matches ("copy_share_button[0-9]{1,2}"))
        {
            int shareNum = 0;
            StringBuilder sb = new StringBuilder ();
            for (int i = 0; i < command.length (); i++)
            {
                final char c = command.charAt (i);
                if (c > 47 && c < 58)
                {
                    sb.append (c);
                }
            }
            shareNum = Integer.parseInt (String.valueOf (sb));

            copyEntryField(parent, String.valueOf(sharei[shareNum].getPassword()));
        }
        if ((command).matches ("show_share_button[0-9]{1,2}"))
        {
            int shareNum = 0;
            StringBuilder sb = new StringBuilder ();
            for (int i = 0; i < command.length (); i++)
            {
                final char c = command.charAt (i);
                if (c > 47 && c < 58)
                {
                    sb.append (c);
                }
            }
            shareNum = Integer.parseInt (String.valueOf (sb));
            sharei[shareNum].setEchoChar (this.showShareButton[shareNum].isSelected () ? NULL_ECHO : this.ORIGINAL_ECHO);
        }
        if ("show_prime_button".equals (command))
        {
            this.primeNum.setEchoChar(this.showPrimeButton.isSelected() ? NULL_ECHO : this.ORIGINAL_ECHO);
        }
        if ("copy_prime_button".equals (command))
        {
            copyEntryField (parent, String.valueOf (this.primeNum.getPassword ()));
        }
        if ("copy_all_button".equals (command))
        {
            copyEntryField (parent, String.valueOf (primeAndAllShares) );
        }
        if ("export_button".equals (command))
        {
            writingShares();
            MessageDialog.showInformationMessage (parent, "All shares has been exported into \"database\" file");
        }
    }

    public void generateShares ()
    {
        try
        {
            String pass = String.valueOf (passwordField.getPassword ());
            String link = String.valueOf (filesLink.getText ()).trim ();
            String totShares = totalShares.getText ().trim ();
            String nedShares = neededShares.getText ().trim ();

            boolean lengthRule = pass.length() >= 8 && pass.length() <= 50;
            boolean upperRule = !pass.equals(pass.toLowerCase());
            boolean lowerRule = !pass.equals(pass.toUpperCase());
            boolean numeralRule = pass.matches("(.*)[0-9](.*)");
            boolean nonAlphaRule = pass.matches("(.*)[^A-Za-z0-9](.*)");

            if (pass.isEmpty ())
            {
                MessageDialog.showWarningMessage(this, "Please fill the password field.");
            }
            else if (!lengthRule || !upperRule || !lowerRule || !numeralRule || !nonAlphaRule)
            {
                MessageDialog.showWarningMessage(parent, "The Master Password must be/contain: \n" +
                                                         "- Between 8 and 50 characters long.\n" +
                                                         "- At least one uppercase letter.\n" +
                                                         "- At least one lowercase letter.\n" +
                                                         "- At least one numeral number.\n" +
                                                         "- At least one non alphanumeric character." +
                                                         "\n\nPlease remember that you don't have to remember it.");
            }
            else if (totShares.isEmpty ())
            {
                MessageDialog.showWarningMessage(this, "Please specify the number of total password shares.");
            }
            else if (nedShares.isEmpty ())
            {
                int totNuShares = Integer.parseInt (totShares); //to invoke the Exception if "totShares" was a String.
                MessageDialog.showWarningMessage(this, "Please specify the number of needed password shares.");
            }
            else if (Integer.parseInt (nedShares) >= Integer.parseInt (totShares))
            {
                MessageDialog.showWarningMessage(this, "The number of needed shares must be smaller than " +
                                                       "the total number of shares.");
            }
            else if (link.length ()>0 && !EntryHelper.isLinkValid (link))
            {
                MessageDialog.showWarningMessage(this, "The link is not valid. You can leave it empty if wouldn't " +
                                                       "like to keep a copy of your files somewhere in the cloud.");
            }
            else if (link.length ()>100)
            {
                MessageDialog.showWarningMessage (this,
                                                  "Sorry, your Link length is \"" + link.length () + "\".\n" +
                                                  " It shouldn't exceed 100 characters");
            }
            else
            {
                this.generatePasswordButton.setEnabled (false);
                this.generateSharesButton.setEnabled (false);
//                copyAllButton.setEnabled (true);
                exportButton.setEnabled (true);

                secMasterPassword = pass;
                linkToFiles = link;
                totNuShares = Integer.parseInt (totShares);
                nedNuShares = Integer.parseInt (nedShares);

                byte[] ndPasswordHash = CryptUtils.getPKCS5Sha256Hash (secMasterPassword.toCharArray ());
                byte[] stPasswordHash = parent.getModel ().getPassword ();
                FileHelper.savePassword (ndPasswordHash, stPasswordHash,".stPassword", true);
                FileHelper.savePassword (stPasswordHash, ndPasswordHash,".ndPassword", true);

                JPanel sharesPanel = new JPanel ();

                sharesPanel.add(new JLabel("Prime Num:"));
                primeNum = TextComponentFactory.newPasswordField(true);
                this.ORIGINAL_ECHO = primeNum.getEchoChar();
                primeNum.setEditable (false);
                sharesPanel.add(primeNum);

                showPrimeButton = new JToggleButton("Show", MessageDialog.getIcon("show"));
                showPrimeButton.setActionCommand("show_prime_button");
                showPrimeButton.setMnemonic(KeyEvent.VK_S);
                showPrimeButton.addActionListener(this);
                sharesPanel.add(showPrimeButton);

                copyPrimeButton = new JButton("Copy", MessageDialog.getIcon("keyring"));
                copyPrimeButton.setActionCommand("copy_prime_button");
                copyPrimeButton.setMnemonic(KeyEvent.VK_C);
                copyPrimeButton.addActionListener(this);
                sharesPanel.add(copyPrimeButton);

                sharei = new JPasswordField[totNuShares];
                copyShareButton = new JButton[totNuShares];
                showShareButton = new JToggleButton [totNuShares];
                for (int i=0; i < totNuShares; i++)
                {
                    sharesPanel.add(new JLabel("Share"+ (i+1) +":"));
                    sharei[i] = TextComponentFactory.newPasswordField(true);
                    this.ORIGINAL_ECHO = sharei[i].getEchoChar();
                    sharei[i].setEditable (false);
//                    sharei[i].setText ("Test"+(i+1));
                    sharesPanel.add(sharei[i]);

                    showShareButton[i] = new JToggleButton("Show", MessageDialog.getIcon ("show"));
                    showShareButton[i].setActionCommand ("show_share_button" + i);
                    showShareButton[i].setMnemonic (KeyEvent.VK_S);
                    showShareButton[i].addActionListener (this);
                    sharesPanel.add (showShareButton[i]);

                    copyShareButton[i] = new JButton("Copy", MessageDialog.getIcon ("keyring"));
                    copyShareButton[i].setActionCommand ("copy_share_button" + i);
                    copyShareButton[i].setMnemonic (KeyEvent.VK_C);
                    copyShareButton[i].addActionListener (this);
                    sharesPanel.add (copyShareButton[i]);
                }
                sharesPanel.setLayout (new SpringLayout ());
                SpringUtilities.makeCompactGrid (sharesPanel,
                                                 totNuShares+1, 4, //rows, columns
                                                 5, 5, //initX, initY
                                                 5, 5);    //xPad, yPad

                getContentPane().add (new JScrollPane (sharesPanel), BorderLayout.CENTER);
                revalidate();
                generateShamirShares ();
            }
        }
        catch (NumberFormatException nfe)
        {
            MessageDialog.showWarningMessage(this, "Please check again, the total and needed number of " +
                                                   "shares have to be an integer.");
        } catch (Exception e)
        {
            e.printStackTrace ();
        }
    }

    private void generateShamirShares ()
    {
        String secretWithLink =
                "The second Master Password is: "+secMasterPassword+ "\nPlease follow this link: \"" + linkToFiles +
                "\" and download the \".jpass\", \".stPassword\" and \".ndPassword\" files. And then come back to " +
                "this JPass client and press on Restore access and use the given second Master Password to restore " +
                "access to the database.";
        String secretWithoutLink =
                "The second Master Password is: "+secMasterPassword+ "\nPlease locate the \".jpass\", \".stPassword\"" +
                " and \".ndPassword\" files, (they should be in a folder named \"database\", on the same directory " +
                "as this JPass client). After having the files come back to this JPass client and press on Restore " +
                "access button and use the given second Master Password to restore access to the database.";
        final BigInteger bigIntegerSecret = new BigInteger(linkToFiles.isEmpty () ? secretWithoutLink.getBytes () :
                                                             secretWithLink.getBytes ());
        final int CERTAINTY = 256;
        final SecureRandom random = new SecureRandom();
        final BigInteger prime = new BigInteger(bigIntegerSecret.bitLength() + 1, CERTAINTY, random);
        final SecretShare[] shares = Shamir.generateShares (bigIntegerSecret, nedNuShares, totNuShares, prime, random);

        String coPrimeNum = nedNuShares + "P:" + String.valueOf (prime);
        this.primeNum.setText (coPrimeNum);

//        String username = parent.getModel ().getUsername ();
        StringBuilder primeAndShares = new StringBuilder (coPrimeNum);
//        primeAndShares.append ("\n").append (coPrimeNum);

        for (SecretShare share: shares)
        {
            String coShare = share.getShareNumber () + ":" + String.valueOf (share.getShareValue ());
            this.sharei[share.getShareNumber () - 1].setText (coShare);

            primeAndShares.append ("\n").append (coShare);
        }
        primeAndAllShares = String.valueOf (primeAndShares);
    }

    private void writingShares ()
    {
        try
        {
            String filePath = sharesFilePath ();
            for (JPasswordField share: sharei)
            {
                String strShareNum = String.valueOf (share.getPassword ()).substring (0,
                                                                                      String.valueOf (share.getPassword ()).indexOf (":"));
                String fileName = filePath + "/Share#" + (strShareNum) + ".txt";
                PrintWriter writer = new PrintWriter(fileName, "UTF-8");
                writer.println(primeNum.getPassword ());
                writer.println ();
                writer.println(share.getPassword ());
                writer.close();

                int intShareNum = Integer.parseInt (strShareNum);
                QRcode.generateQRcode (String.valueOf(primeNum.getPassword()) + "\n" +
                                       String.valueOf(sharei[intShareNum-1].getPassword()),
                                       sharesFilePath ()+"/share#"+intShareNum+"QRcode.png",
                                       "UTF-8");
            }
        } catch (FileNotFoundException e)
        {
            MessageDialog.showInformationMessage (parent, "The specified file is not found");
        } catch (UnsupportedEncodingException e)
        {
            MessageDialog.showInformationMessage (parent, "The Encoding is nor supported");
        }
    }

    private String sharesFilePath ()
    {
        File ret = null;
        String filename = parent.getModel ().getFileName ();
        String userFileName = filename.substring (filename.lastIndexOf ("\\")+1, filename.lastIndexOf (".jpass"));
        File sharesFile = new File ("./database/"+userFileName+"_shares/");
        if (!sharesFile.exists ())
        {
            sharesFile.mkdir ();
        }
        return sharesFile.getPath ();
    }
}
