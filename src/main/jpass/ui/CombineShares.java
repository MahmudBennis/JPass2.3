package main.jpass.ui;

import main.jpass.ui.helper.FileHelper;
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
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;

import static main.jpass.ui.helper.EntryHelper.copyEntryField;

/**
 * Combining Shares class for combining shares in order to reveal the Secret.
 *
 * @author Mahmud Ibr Bennis.
 *
 */
public class CombineShares extends JDialog implements ActionListener
{
    private static final long serialVersionUID = -8551022862532925065L;

    private final JPanel fieldPanel;
    private JPanel passwordPanel;
    private final JButton submitButton;
    private JButton submitSharesButton;
    private JPasswordField[] sharei;
    private JToggleButton[] showShareButton;
    private JButton[] locateShareButton;
    private JTextField primeNum;
    private JToggleButton showPrimeButton;
    private JScrollPane sharesPanelJScrollPane;
    private JButton locatePrimeButton;

    String passwordStr;
    String autoFilledShare = null;

    private int numOfShares;

    private Entry formData;

    private char ORIGINAL_ECHO;
    private static final char NULL_ECHO = '\0';

    private final JPassFrame parent;

    /**
     * The class constructor, which builds the Reveal Secret window.
     * @param parent - The parent frame.
     * @param title - The Window title.
     */
    public CombineShares (final JPassFrame parent, final String title)
    {
        super (parent, title, true);
        setDefaultCloseOperation (WindowConstants.DISPOSE_ON_CLOSE);

        this.parent = parent;

        this.formData = null;

        this.fieldPanel = new JPanel ();

        fieldPanel.add (new JLabel ("Prime Number:"));
        primeNum = TextComponentFactory.newTextField ();
        this.fieldPanel.add (primeNum);

        locatePrimeButton = new JButton ("Locate", MessageDialog.getIcon ("accept"));
        locatePrimeButton.setActionCommand ("locate_prime_button");
        locatePrimeButton.setMnemonic (KeyEvent.VK_S);
        locatePrimeButton.addActionListener (this);
        this.fieldPanel.add (locatePrimeButton);

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

    @Override
    public void actionPerformed (ActionEvent e)
    {
        String command = e.getActionCommand ();
        if ("locate_prime_button".equals (command))
        {
            locatePrimeFile();
        }
        if ("Submit_coPrime_Num".equals (command))
        {
            submitPrime ();
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
        if ((command).matches ("locate_share_button[0-9]{1,2}"))
        {
            locateShareFile(command);
        }
        if ("Submit_Shares".equals (command))
        {
            submitShares ();
        }
    }

    /**
     * To update the window when "Submit" button clicked, meaning submitting Prime Number.
     */
    public void submitPrime ()
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

                if (autoFilledShare != null && sharei != null)
                    sharei[0].setText (autoFilledShare.trim ());
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

    /**
     * To submit the share values and display the secret on the screen.
     */
    public void submitShares ()
    {
        boolean disableSubmitButton = true;
        try
        {
            boolean sharesAreMissed = false;
            boolean notJustNum = false;
            Set<String> s = new HashSet<String> ();
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
                s.add (String.valueOf (share.getPassword ()));
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
            else if (s.size() != sharei.length)
            {
                MessageDialog.showWarningMessage (this, "Sorry, all shares must be distinct.");
                disableSubmitButton = false;
            }
            else
            {

                combineShamirShares ();

                JPanel secretPanel = new JPanel (new SpringLayout ());
                secretPanel.add (new JLabel ("Here is the Secret:"));
                JTextArea secret = TextComponentFactory.newTextArea (passwordStr);
                secret.setLineWrap (true);
                secret.setWrapStyleWord(true);
                secret.setEditable (false);
                secretPanel.add (secret);

                SpringUtilities.makeCompactGrid (secretPanel,
                                                 2, 1, //rows, columns
                                                 5, 5, //initX, initY
                                                 5, 5);    //xPad, yPad

                getContentPane ().add (secretPanel, BorderLayout.CENTER);

                getContentPane ().remove (sharesPanelJScrollPane);
                getContentPane ().remove (this.passwordPanel);
                getContentPane ().add (secretPanel, BorderLayout.CENTER);
                revalidate ();
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

    /**
     * To load the share value from the selected share file
     * @param shareName - The share name
     */
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
                if (matcher != null && String.valueOf (sharei[shareNum].getPassword ()).isEmpty ())
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

    /**
     * To load the Prime value from the selected share file
     */
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
                    if (line.matches ("^[0-9]{1,2}:[0-9]+$"))
                        autoFilledShare = line;
                }
                if (primeNum.getText ().isEmpty ())
                    MessageDialog.showWarningMessage (parent, "The Prime number can't be found");
            }
            if (primeFile!=null && primeFile.getName ().toLowerCase ().endsWith (".png"))
            {
                String share = QRcode.readQRcode (primeFile.getPath ());
                Pattern primePattern = Pattern.compile ("^[0-9]{1,2}P:[0-9]+");
                Pattern sharePattern = Pattern.compile ("\n[0-9]{1,2}:[0-9]+$");
                Matcher primeMatcher = (share != null ? primePattern.matcher (share) : null);
                Matcher shareMatcher = (share != null ? sharePattern.matcher (share) : null);
                if (primeMatcher != null && primeMatcher.find())
                    primeNum.setText (primeMatcher.group ());
                if (shareMatcher != null && shareMatcher.find ())
                    autoFilledShare = shareMatcher.group ();
                if (primeMatcher != null && primeNum.getText ().isEmpty ())
                    MessageDialog.showWarningMessage (parent, "The Prime number can't be found");
            }
            if (autoFilledShare != null && sharei != null)
                sharei[0].setText (autoFilledShare);
        } catch (FileNotFoundException fileNotFoundException)
        {
            MessageDialog.showWarningMessage (parent, "The file can't be found.");
        } catch (IOException ioException)
        {
            ioException.printStackTrace ();
        }
    }

    /**
     * To combine shares and reveal the secret.
     */
    private void combineShamirShares ()
    {
        int avaSharesNum = numOfShares;
        SecretShare[] sharesToRevealSecret = new SecretShare[avaSharesNum];
        for (int i = 0; i < avaSharesNum; i++)
        {
            String coShareStr = String.valueOf (sharei[i].getPassword ()).trim ();
            String shareStr = coShareStr.substring (coShareStr.indexOf (":") + 1);
            int shareNum = Integer.parseInt (coShareStr.substring (0, coShareStr.indexOf (":")));
            sharesToRevealSecret[i] = new SecretShare (shareNum, new BigInteger (shareStr));
        }
        String coPrimeStr = String.valueOf (primeNum.getText ()).trim ();
        String primeStr = coPrimeStr.substring (coPrimeStr.indexOf (":") + 1);
        //int primeNum = Integer.parseInt (coPrimeStr.substring (0,coPrimeStr.indexOf ("P:")));
        BigInteger prime = new BigInteger (primeStr);
        String result = Shamir.combineShares (sharesToRevealSecret, prime);
        passwordStr = result;
    }
}
