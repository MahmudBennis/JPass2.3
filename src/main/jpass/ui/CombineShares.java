package main.jpass.ui;

import main.jpass.ui.helper.FileHelper;
import main.jpass.util.CryptUtils;
import main.jpass.util.SpringUtilities;
import main.jpass.util.StringUtils;
import main.jpass.xml.bind.Entry;
import main.secretShare.SecretShare;
import main.secretShare.Shamir;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;

import static main.jpass.ui.helper.EntryHelper.copyEntryField;

public class CombineShares extends JDialog implements ActionListener
{
    private static final long serialVersionUID = -8551022862532925065L;

    private final JPanel fieldPanel;
    private JPanel passwordPanel;

//    private final JPasswordField passwordField;
//    private final JTextField totalShares;
//    private final JTextField neededShares;
    private final JButton submitButton;
    private JButton submitSharesButton;
    private JButton copyButton;
    private JToggleButton showPasswordButton;
    private JPasswordField passwordField;
    private JPasswordField[] sharei;
    private JToggleButton[] showShareButton;
    private JButton[] locateShareButton;
    private JTextField primeNum;
    private JToggleButton showPrimeButton;
    private JScrollPane sharesPanelJScrollPane;
    private JButton locatePrimeButton;
//    private JTextField filename;

    String filePath;
    String passwordStr;

    private int numOfShares;

    private Entry formData;

    private char ORIGINAL_ECHO;
    private static final char NULL_ECHO = '\0';

    private JPassFrame parent;

    public CombineShares (final JPassFrame parent, final String title)
    {
        super (parent, title, true);
        setDefaultCloseOperation (WindowConstants.DISPOSE_ON_CLOSE);

        this.parent = parent;

        this.formData = null;

        this.fieldPanel = new JPanel ();

        fieldPanel.add (new JLabel ("Prime Number:"));
        primeNum = TextComponentFactory.newTextField ();
//        this.ORIGINAL_ECHO = primeNum.getEchoChar ();
        this.fieldPanel.add (primeNum);

        showPrimeButton = new JToggleButton ("Show", MessageDialog.getIcon ("show"));
        showPrimeButton.setActionCommand ("show_prime_button");
        showPrimeButton.setMnemonic (KeyEvent.VK_S);
        showPrimeButton.addActionListener (this);
//        this.fieldPanel.add (showPrimeButton);

        locatePrimeButton = new JButton ("Locate", MessageDialog.getIcon ("accept"));
        locatePrimeButton.setActionCommand ("locate_prime_button");
        locatePrimeButton.setMnemonic (KeyEvent.VK_S);
        locatePrimeButton.addActionListener (this);
        this.fieldPanel.add (locatePrimeButton);

        /*this.fieldPanel.add (new JLabel ("The .jpass file:"));
        this.filename = TextComponentFactory.newTextField ();
        this.filename.setEditable (false);
        this.fieldPanel.add (this.filename);

        this.locateFileButton = new JButton ("Locate file", MessageDialog.getIcon ("accept"));
        this.locateFileButton.setActionCommand ("Locate_File");
        this.locateFileButton.setMnemonic (KeyEvent.VK_G);
        this.locateFileButton.addActionListener (this);
        this.fieldPanel.add (this.locateFileButton);*/

        this.fieldPanel.add (new JLabel (""));
        this.submitButton = new JButton ("Submit", MessageDialog.getIcon ("accept"));
        this.submitButton.setActionCommand ("Submit_coPrime_Num");
        this.submitButton.setMnemonic (KeyEvent.VK_G);
        this.submitButton.addActionListener (this);
        this.fieldPanel.add (this.submitButton);
        this.fieldPanel.add (new JLabel (""));

        this.fieldPanel.setLayout (new SpringLayout ());
        SpringUtilities.makeCompactGrid (this.fieldPanel,
                                         2, 3, //rows, columns
                                         5, 5, //initX, initY
                                         5, 5);    //xPad, yPad

        getContentPane ().add (this.fieldPanel, BorderLayout.NORTH);

        setSize (450, 400);
        setMinimumSize (new Dimension (370, 300));
        setLocationRelativeTo (parent);
        setVisible (true);
    }

    private void setFormData (Entry formData)
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
        if ("Submit_coPrime_Num".equals (command))
        {
            insertShares ();
        }
        if ("show_prime_button".equals (command))
        {
//            this.primeNum.setEchoChar (this.showPrimeButton.isSelected () ? NULL_ECHO : this.ORIGINAL_ECHO);
        }
        if ("Submit_Shares".equals (command))
        {
            submitShares ();
        }
        if ("locate_prime_button".equals (command))
        {
            /*MessageDialog.showInformationMessage (parent, "Please select any Share file, and we will extract the " +
                                                          "Prime number from it.");*/
            locatePrimeFile();
        }

        if ((command).matches ("locate_share_button[0-9]{1,2}"))
        {
            locateShareFile(command);
        }

        if ((command).matches ("show_button[0-9]{1,2}"))
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
            sharei[shareNum]
                    .setEchoChar (this.showShareButton[shareNum].isSelected () ? NULL_ECHO : this.ORIGINAL_ECHO);
        }

        if ("show_button".equals (command))
        {
            this.passwordField.setEchoChar (this.showPasswordButton.isSelected () ? NULL_ECHO : this.ORIGINAL_ECHO);
        }
        if ("copy_button".equals (command))
        {
            copyEntryField (JPassFrame.getInstance (), String.valueOf (this.passwordField.getPassword ()));
        }
        if ("Locate_File".equals (command))
        {
//            final String username = MessageDialog.showUsernameDialog (JPassFrame.getInstance (), true);
//            final File dbFile = (username == null ? null : FileHelper.filePath ("resources/database/", username + ".jpass"));
//            final File passFile = (username == null ? null : FileHelper.filePath ("resources/database/", username + ".stPassword"));
            final File dbFile = FileHelper.showFileChooser(JPassFrame.getInstance (), "Open", new String[]{"jpass"}, "JPass Data Files (*.jpass)");
            String jpassFilePath = (dbFile == null ? null : dbFile.getPath ());
            String strPassFile = (jpassFilePath == null ? null : jpassFilePath.substring (0,
                                                                                    jpassFilePath.lastIndexOf (".")) + ".stPassword");

            /*if (strPassFile != null)
            {
                final File passFile = new File (strPassFile);
                if (passFile.exists ())
                {
                    this.filename.setText (dbFile.getName ());
                    filePath = dbFile.getPath ();
                }
                else
                {
                    MessageDialog.showWarningMessage (JPassFrame.getInstance (), "Couldn't find the .stPassword file, " +
                                                                                 "Please place it in the same folder " +
                                                                                 "as the .jpass file");
                }
            }*/
        }
    }

    public void insertShares ()
    {
        boolean disableSubmitButton = true;
        try
        {
            String coPrimeNum = String.valueOf (primeNum.getText ()).trim ();
            if (coPrimeNum.isEmpty ())
            {
                MessageDialog.showWarningMessage (this, "Please fill the complete Prime Number field.");
                disableSubmitButton = false;
            }
            else if (!coPrimeNum.matches ("^[0-9]{1,2}P:[0-9]+$"))
            {
                MessageDialog.showWarningMessage (this, "you must entered a wrong Prime Number.");
                disableSubmitButton = false;
            }
            /*else if (this.filename.getText ().isEmpty ())
            {
                MessageDialog.showWarningMessage (this, "Please locate the file name.");
                disableSubmitButton = false;
            }*/
            else
            {
                String strNumOfShares = coPrimeNum.substring (0, coPrimeNum.indexOf ("P:"));
                numOfShares = Integer.parseInt (strNumOfShares);

                JPanel sharesPanel = new JPanel ();
                sharei = new JPasswordField[numOfShares];
                showShareButton = new JToggleButton[numOfShares];
                locateShareButton = new JButton[numOfShares];
                for (int i = 0; i < numOfShares; i++)
                {
                    sharesPanel.add (new JLabel ("Share" + (i + 1) + ":"));
                    sharei[i] = TextComponentFactory.newPasswordField (true);
                    this.ORIGINAL_ECHO = sharei[i].getEchoChar ();
                    sharei[i].setEditable (true);
                    sharesPanel.add (sharei[i]);

                    showShareButton[i] = new JToggleButton ("Show", MessageDialog.getIcon ("show"));
                    showShareButton[i].setActionCommand ("show_button" + i);
                    showShareButton[i].setMnemonic (KeyEvent.VK_S);
                    showShareButton[i].addActionListener (this);
                    sharesPanel.add (showShareButton[i]);

                    locateShareButton[i] = new JButton ("Locate", MessageDialog.getIcon ("accept"));
                    locateShareButton[i].setActionCommand ("locate_share_button" + i);
                    locateShareButton[i].setMnemonic (KeyEvent.VK_S);
                    locateShareButton[i].addActionListener (this);
                    sharesPanel.add (locateShareButton[i]);
                }

                this.passwordPanel = new JPanel (new SpringLayout ());
                this.submitSharesButton = new JButton ("Submit", MessageDialog.getIcon ("accept"));
                this.submitSharesButton.setActionCommand ("Submit_Shares");
                this.submitSharesButton.setMnemonic (KeyEvent.VK_G);
                this.submitSharesButton.addActionListener (this);
                this.passwordPanel.add (this.submitSharesButton);


                sharesPanel.setLayout (new SpringLayout ());
                SpringUtilities.makeCompactGrid (sharesPanel,
                                                 numOfShares, 4, //rows, columns
                                                 5, 5, //initX, initY
                                                 5, 5);    //xPad, yPad

                SpringUtilities.makeCompactGrid (passwordPanel,
                                                 1, 1, //rows, columns
                                                 170, 5, //initX, initY
                                                 5, 5);    //xPad, yPad

                getContentPane ().add (sharesPanelJScrollPane = new JScrollPane (sharesPanel), BorderLayout.CENTER);
                getContentPane ().add (this.passwordPanel, BorderLayout.SOUTH);
                revalidate ();
            }
        } catch (NumberFormatException | StringIndexOutOfBoundsException ex)
        {
            MessageDialog.showWarningMessage (this, "Sorry, It should start with an integer followed by" +
                                                    " \"P:\" then followed by a stream of integers." +
                                                    " Ex, 7P:68001482550476051309634236029");
            disableSubmitButton = false;
        } finally
        {
            if (disableSubmitButton)
                this.submitButton.setEnabled (false);
        }
    }

    public void submitShares ()
    {
        boolean disableSubmitButton = true;
        try
        {
            boolean sharesAreMissed = false;
            boolean notJustNum = false;
            for (JPasswordField share : sharei)
            {
                String coShareStr = String.valueOf (share.getPassword ()).trim ();
//                String shareStr = coShareStr.substring (coShareStr.indexOf (":") + 1);
                if (String.valueOf (share.getPassword ()).isEmpty ())
                {
                    sharesAreMissed = true;
                    break;
                }
                else if (!coShareStr.matches ("^[0-9]{1,2}:[0-9]+$"))
                {
                    notJustNum = true;
                    break;
                }
            }
            if (sharesAreMissed)
            {
                MessageDialog.showWarningMessage (this, "Please fill-in all the shares");
                disableSubmitButton = false;
            }
            else if (notJustNum)
            {
                MessageDialog.showWarningMessage (this, "Sorry, all shares should start with an integer " +
                                                        "followed by \":\" then followed by a stream of integers." +
                                                        " Ex, 7:68001482550476051309634236029");
                disableSubmitButton = false;
            }
            else
            {
                // this FOR Loop is just to loop through all the shares and invoke an exception in case that any of
                // them is not starting with "#:"
//                for (JPasswordField share : sharei)
//                {
//                    String coShareStr = String.valueOf (share.getPassword ()).trim ();
//                    int shareNum = Integer.parseInt (coShareStr.substring (0, coShareStr.indexOf (":")));
//                }

                combineShamirShares ();

                JPanel secretPanel = new JPanel (new SpringLayout ());
                secretPanel.add (new JLabel ("Here is the Secret:"));
                JTextArea secret = TextComponentFactory.newTextArea (passwordStr);
                secret.setLineWrap (true);
                secret.setWrapStyleWord(true);
                secret.setEditable (false);
                secretPanel.add (secret);

                /*JPanel copySecretPanel = new JPanel (new SpringLayout ());
                JButton copySecretButton = new JButton ("Copy", MessageDialog.getIcon ("accept"));
                copySecretButton.setActionCommand ("Copy_Secret");
                copySecretButton.setMnemonic (KeyEvent.VK_G);
                copySecretButton.addActionListener (this);
                copySecretPanel.add (copySecretButton);*/

                SpringUtilities.makeCompactGrid (secretPanel,
                                                 2, 1, //rows, columns
                                                 5, 5, //initX, initY
                                                 5, 5);    //xPad, yPad

                /*SpringUtilities.makeCompactGrid (copySecretPanel,
                                                 1, 1, //rows, columns
                                                 170, 5, //initX, initY
                                                 5, 5);    //xPad, yPad*/

                getContentPane ().add (secretPanel, BorderLayout.CENTER);

                getContentPane ().remove (sharesPanelJScrollPane);
                getContentPane ().remove (this.passwordPanel);
                getContentPane ().add (secretPanel, BorderLayout.CENTER);
//                getContentPane ().add (copySecretPanel, BorderLayout.SOUTH);
                revalidate ();


                /*try
                {
//                    String filenameStr = this.filename.getText ().trim ();
//                    String username = filenameStr.substring (0, filenameStr.indexOf (".jpass"));
                    byte[] ndPasswordHash = CryptUtils.getPKCS5Sha256Hash (passwordStr.toCharArray ());
                    byte[] stPasswordHash = FileHelper.openPasswordDoc (ndPasswordHash, filePath, ".stPassword");
                    FileHelper.doOpenFile (filePath , JPassFrame.getInstance (), true, stPasswordHash);
                }
                catch (Exception e)
                {
                    MessageDialog.showErrorMessage (JPassFrame.getInstance (),
                                                    "Cannot generate password hash:\n" + StringUtils.stripString (e.getMessage ()) + "\n\nOpening and saving files are not possible!");
                }
                dispose();*/
            }
        } catch (NumberFormatException | StringIndexOutOfBoundsException ex)
        {
            disableSubmitButton = false;
            MessageDialog.showWarningMessage (this, "Sorry, all shares should start with an integer " +
                                                    "followed by \":\" then followed by a stream of integers." +
                                                    " Ex, 7:68001482550476051309634236029");
        } finally
        {
            if (disableSubmitButton)
                this.submitSharesButton.setEnabled (false);
        }
    }

    public void locateShareFile (String shareName)
    {
        int shareNum = 0;
        StringBuilder sb = new StringBuilder ();
        for (int i = 0; i < shareName.length (); i++)
        {
            final char c = shareName.charAt (i);
            if (c > 47 && c < 58)
            {
                sb.append (c);
            }
        }
        shareNum = Integer.parseInt (String.valueOf (sb));
        final File shareFile = FileHelper
                .showFileChooser (JPassFrame.getInstance (), "Open",
                                  new String[]{"txt", "png"}, "Share file (*.txt) OR (*.png)");
        BufferedReader bufferedReader;
        try
        {
            if (shareFile!=null && shareFile.getName ().toLowerCase ().endsWith (".txt"))
            {
                bufferedReader = new BufferedReader (new FileReader (shareFile));
                String line;
                while ((line = bufferedReader.readLine ()) != null)
                {
                    if (line.matches ("^[0-9]{1,2}:[0-9]+$"))
                        sharei[shareNum].setText (line);
                }
                if (String.valueOf (sharei[shareNum].getPassword ()).isEmpty ())
                    MessageDialog.showWarningMessage (parent, "The Share can't be found");
//                    sharei[shareNum].setEnabled (false);
            }
            if (shareFile!=null && shareFile.getName ().toLowerCase ().endsWith (".png"))
            {
                String share = QRcode.readQRcode (shareFile.getPath ());
                Pattern pattern = Pattern.compile ("[0-9]{1,2}:[0-9]+$");
                Matcher matcher = (share != null ? pattern.matcher (share) : null);
                if (matcher != null && matcher.find())
                {
                    sharei[shareNum].setText (matcher.group ());
                }
                if (matcher != null && sharei[shareNum].getPassword () == null)
                    MessageDialog.showWarningMessage (parent, "The Share can't be found");
            }
        } catch (FileNotFoundException fileNotFoundException)
        {
            MessageDialog.showWarningMessage (parent, "The file can't be found.");
        } catch (IOException ioException)
        {
            ioException.printStackTrace ();
        }
    }

    private void locatePrimeFile ()
    {
        final File primeFile = FileHelper
                .showFileChooser (JPassFrame.getInstance (), "Open",
                                  new String[]{"txt", "png"}, "Share file (*.txt) Or (*.png)");
        BufferedReader bufferedReader;
        try
        {
            if (primeFile!=null && primeFile.getName ().toLowerCase ().endsWith (".txt"))
            {
                bufferedReader = new BufferedReader (new FileReader (primeFile));
                String line;
                while ((line = bufferedReader.readLine ()) != null)
                {
                    if (line.matches ("^[0-9]{1,2}P:[0-9]+$"))
                        primeNum.setText (line);
                }
                if (primeNum.getText ().isEmpty ())
                    MessageDialog.showWarningMessage (parent, "The Prime number can't be found");
            }
            if (primeFile!=null && primeFile.getName ().toLowerCase ().endsWith (".png"))
            {
                String share = QRcode.readQRcode (primeFile.getPath ());
                Pattern pattern = Pattern.compile ("^[0-9]{1,2}P:[0-9]+");
                Matcher matcher = (share != null ? pattern.matcher (share) : null);
                if (matcher != null && matcher.find())
                {
                    primeNum.setText (matcher.group ());
                }
                if (matcher != null && primeNum.getText () == null)
                    MessageDialog.showWarningMessage (parent, "The Prime number can't be found");
            }
        } catch (FileNotFoundException fileNotFoundException)
        {
            MessageDialog.showWarningMessage (parent, "The file can't be found.");
        } catch (IOException ioException)
        {
            ioException.printStackTrace ();
        }
    }

    private void combineShamirShares ()
    {
        int avaSharesNum = numOfShares;
        SecretShare[] sharesToViewSecret = new SecretShare[avaSharesNum];
        for (int i = 0; i < avaSharesNum; i++)
        {
            String coShareStr = String.valueOf (sharei[i].getPassword ()).trim ();
            String shareStr = coShareStr.substring (coShareStr.indexOf (":") + 1);
            int shareNum = Integer.parseInt (coShareStr.substring (0, coShareStr.indexOf (":")));
            sharesToViewSecret[i] = new SecretShare (shareNum, new BigInteger (shareStr));
        }
        String coPrimeStr = String.valueOf (primeNum.getText ()).trim ();
        String primeStr = coPrimeStr.substring (coPrimeStr.indexOf (":") + 1);
        //int primeNum = Integer.parseInt (coPrimeStr.substring (0,coPrimeStr.indexOf ("P:")));
        BigInteger prime = new BigInteger (primeStr);
        String result = Shamir.combine (sharesToViewSecret, prime);
        passwordStr = result;
    }
}
