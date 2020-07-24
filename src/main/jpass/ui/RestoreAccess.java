package main.jpass.ui;

import main.jpass.ui.helper.FileHelper;
import main.jpass.util.CryptUtils;
import main.jpass.util.SpringUtilities;
import main.jpass.util.StringUtils;
import main.jpass.xml.bind.Entry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * Restore Access class for restoring access to the database file.
 *
 * @author Mahmud Ibr Bennis.
 *
 */
public class RestoreAccess extends JDialog implements ActionListener
{
    private final JPanel fieldPanel;
    private final JPasswordField passwordField;
    private final JTextField dbFilename;
    private final JToggleButton showStPasswordButton;
    private JButton locateDBFileButton;
    private JButton restoreAccessButton;

    String filePath;

    private Entry formData;

    private char ORIGINAL_ECHO;
    private static final char NULL_ECHO = '\0';

    /**
     * The class constructor, which builds the Restore Access window.
     * @param parent - The parent frame.
     * @param title - The Window title.
     */
    public RestoreAccess (final JPassFrame parent, final String title)
    {
        super (parent, title, true);
        setDefaultCloseOperation (WindowConstants.DISPOSE_ON_CLOSE);

        this.formData = null;

        this.fieldPanel = new JPanel(new SpringLayout ());

        this.fieldPanel.add(new JLabel("Master Password:"));
        this.passwordField = TextComponentFactory.newPasswordField(true);
        this.ORIGINAL_ECHO = this.passwordField.getEchoChar();
        this.fieldPanel.add(this.passwordField);

        this.showStPasswordButton = new JToggleButton("Show", MessageDialog.getIcon ("show"));
        this.showStPasswordButton.setActionCommand ("show_button");
        this.showStPasswordButton.setMnemonic (KeyEvent.VK_S);
        this.showStPasswordButton.addActionListener (this);
        this.fieldPanel.add (this.showStPasswordButton);

        this.fieldPanel.add (new JLabel ("Database File:"));
        this.dbFilename = TextComponentFactory.newTextField ();
        this.dbFilename.setEditable (false);
        this.fieldPanel.add (this.dbFilename);

        this.locateDBFileButton = new JButton ("Locate file", MessageDialog.getIcon ("accept"));
        this.locateDBFileButton.setActionCommand ("Locate_dbFile");
        this.locateDBFileButton.setMnemonic (KeyEvent.VK_G);
        this.locateDBFileButton.addActionListener (this);
        this.fieldPanel.add (this.locateDBFileButton);

        this.fieldPanel.add (new JLabel (""));
        this.restoreAccessButton = new JButton ("Restore Access", MessageDialog.getIcon ("accept"));
        this.restoreAccessButton.setActionCommand ("Restore_Access");
        this.restoreAccessButton.setMnemonic (KeyEvent.VK_G);
        this.restoreAccessButton.addActionListener (this);
        this.fieldPanel.add (this.restoreAccessButton);
        this.fieldPanel.add (new JLabel (""));

        getContentPane ().add (this.fieldPanel, BorderLayout.NORTH);

        SpringUtilities.makeCompactGrid (this.fieldPanel,
                                         3, 3, //rows, columns
                                         5, 5, //initX, initY
                                         5, 5);    //xPad, yPad

        setSize (450, 400);
        setMinimumSize (new Dimension (370, 300));
        setLocationRelativeTo (parent);
        setVisible (true);
    }

    @Override
    public void actionPerformed (ActionEvent e)
    {
        String command = e.getActionCommand ();
        if ("show_button".equals (command))
        {
            this.passwordField.setEchoChar (this.showStPasswordButton.isSelected () ? NULL_ECHO : this.ORIGINAL_ECHO);
        }
        if ("Locate_dbFile".equals (command))
        {
            final File dbFile = FileHelper
                    .showFileChooser (JPassFrame.getInstance (), "Open", new String[]{"jpass"}, "JPass Data Files (*.jpass)");
            String jpassFilePath = (dbFile == null ? null : dbFile.getPath ());
            String strPassFile = (jpassFilePath == null ? null : jpassFilePath.substring (0,
                                                                                          jpassFilePath.lastIndexOf (".")) + ".stPassword");
            if (strPassFile != null)
            {
                final File passFile = new File (strPassFile);
                if (passFile.exists ())
                {
                    this.dbFilename.setText (dbFile.getName ());
                    filePath = dbFile.getPath ();
                }
                else
                {
                    MessageDialog.showErrorMessage (JPassFrame.getInstance (), "Couldn't find the \".stPassword\"" +
                                                                               "file, Please place it in the same " +
                                                                               "folder as the \".jpass\" file");
                }
            }
        }
        if ("Restore_Access".equals (command))
        {
            restoreAccess ();
        }
    }

    /**
     * Restoring access to the database file
     */
    public void restoreAccess ()
    {
        try
        {
            String secMasterPassword = String.valueOf (passwordField.getPassword ());
            if (secMasterPassword.isEmpty ())
                MessageDialog.showWarningMessage(this, "Please fill the \"Master Password\" field.");
            else if (dbFilename.getText ().isEmpty ())
                MessageDialog.showWarningMessage(this, "Please fill the \"Database File\" field.");
            else
            {
                byte[] ndPasswordHash = CryptUtils.getPBKDF2Hash (secMasterPassword.toCharArray ());
                byte[] stPasswordHash = FileHelper.openPasswordDoc (ndPasswordHash, filePath, ".stPassword");
                FileHelper.doOpenFile (filePath , JPassFrame.getInstance (), true, stPasswordHash);
                dispose();
            }
        }
        catch (Exception e)
        {
            MessageDialog.showErrorMessage (JPassFrame.getInstance (),
                                            "Cannot generate password hash:\n" + StringUtils
                                                    .stripString (e.getMessage ()) + "\n\nOpening and saving files are not possible!");
        }
    }
}
