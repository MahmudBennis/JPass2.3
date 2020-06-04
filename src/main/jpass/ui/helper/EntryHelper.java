/*
 * JPass
 *
 * Copyright (c) 2009-2019 Gabor Bata
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package main.jpass.ui.helper;

import main.jpass.data.DataModel;
import main.jpass.ui.*;
import main.jpass.util.ClipboardUtils;
import main.jpass.xml.bind.Entry;
import org.apache.commons.validator.routines.UrlValidator;

/**
 * Helper class for entry operations.
 *
 * @author Gabor_Bata
 *
 */
public final class EntryHelper
{

    private EntryHelper ()
    {
        // not intended to be instantiated
    }

    /**
     * Deletes an entry.
     *
     * @param parent parent component
     */
    public static void deleteEntry (JPassFrame parent)
    {
        if (parent.getEntryTitleList ().getSelectedIndex () == -1)
        {
            MessageDialog.showWarningMessage (parent, "Please select an entry.");
            return;
        }
        int option = MessageDialog.showQuestionMessage (parent, "Do you really want to delete this entry?",
                                                        MessageDialog.YES_NO_OPTION);
        if (option == MessageDialog.YES_OPTION)
        {
            String title = (String) parent.getEntryTitleList ().getSelectedValue ();
            parent.getModel ().getEntries ().getEntry ().remove (parent.getModel ().getEntryByTitle (title));
            parent.getModel ().setModified (true);
            parent.refreshFrameTitle ();
            parent.refreshEntryTitleList (null);
        }
    }

    /**
     * Duplicates an entry.
     *
     * @param parent parent component
     */
    public static void duplicateEntry (JPassFrame parent)
    {
        if (parent.getEntryTitleList ().getSelectedIndex () == -1)
        {
            MessageDialog.showWarningMessage (parent, "Please select an entry.");
            return;
        }
        String title = (String) parent.getEntryTitleList ().getSelectedValue ();
        Entry oldEntry = parent.getModel ().getEntryByTitle (title);
        EntryDialog ed = new EntryDialog (parent, "Duplicate Entry", oldEntry, true);
        if (ed.getFormData () != null)
        {
            parent.getModel ().getEntries ().getEntry ().add (ed.getFormData ());
            parent.getModel ().setModified (true);
            parent.refreshFrameTitle ();
            parent.refreshEntryTitleList (ed.getFormData ().getTitle ());
        }
    }

    /**
     * Edits the entry.
     *
     * @param parent parent component
     */
    public static void editEntry (JPassFrame parent)
    {
        if (parent.getEntryTitleList ().getSelectedIndex () == -1)
        {
            MessageDialog.showWarningMessage (parent, "Please select an entry.");
            return;
        }
        String title = (String) parent.getEntryTitleList ().getSelectedValue ();
        Entry oldEntry = parent.getModel ().getEntryByTitle (title);
        EntryDialog ed = new EntryDialog (parent, "Edit Entry", oldEntry, false);
        if (ed.getFormData () != null)
        {
            parent.getModel ().getEntries ().getEntry ().remove (oldEntry);
            parent.getModel ().getEntries ().getEntry ().add (ed.getFormData ());
            parent.getModel ().setModified (true);
            parent.refreshFrameTitle ();
            parent.refreshEntryTitleList (ed.getFormData ().getTitle ());
        }
    }

    /**
     * Adds an entry.
     *
     * @param parent parent component
     */
    public static void addEntry (JPassFrame parent)
    {
        EntryDialog ed = new EntryDialog (parent, "Add New Entry", null, true);
        if (ed.getFormData () != null)
        {
            parent.getModel ().getEntries ().getEntry ().add (ed.getFormData ());
            parent.getModel ().setModified (true);
            parent.refreshFrameTitle ();
            parent.refreshEntryTitleList (ed.getFormData ().getTitle ());
        }
    }

    /**
     * Gets the selected entry.
     *
     * @param parent the parent frame
     * @return the entry or null
     */
    public static Entry getSelectedEntry (JPassFrame parent)
    {
        if (parent.getEntryTitleList ().getSelectedIndex () == -1)
        {
            MessageDialog.showWarningMessage (parent, "Please select an entry.");
            return null;
        }
        return parent.getModel ().getEntryByTitle (parent.getEntryTitleList ().getSelectedValue ());
    }

    /**
     * Copy entry field value to clipboard.
     *
     * @param parent  the parent frame
     * @param content the content to copy
     */
    public static void copyEntryField (JPassFrame parent, String content)
    {
        try
        {
            ClipboardUtils.setClipboardContent (content);
        } catch (Exception e)
        {
            MessageDialog.showErrorMessage (parent, e.getMessage ());
        }
    }

    /**
     *
     */
    public static void generateShares (JPassFrame parent)
    {
        if (DataModel.getInstance ().getFileName () == null)
            MessageDialog.showWarningMessage(parent, "You have to open your file first.");
        else
        {
            MessageDialog.showInformationMessage (parent, "- Please choose your new second Master Password.\n" +
                                                          "- Specify a link to where to find your files, (you can " +
                                                          "leave it empty if you want).\n" +
                                                          "- Specify the total number of shares.\n" +
                                                          "- How many shares are required to restore access to " +
                                                          "you file.\n\n" +
                                                          "Note: You don't have to remember your second Master " +
                                                          "Password.");

            GenerateShares gs = new GenerateShares (JPassFrame.getInstance (), "Generate Secret Shares");

            if (gs.getFormData () != null)
            {
                JPassFrame.getInstance ().getModel ().getEntries ().getEntry ().add (gs.getFormData ());
                JPassFrame.getInstance ().getModel ().setModified (true);
                JPassFrame.getInstance ().refreshFrameTitle ();
                JPassFrame.getInstance ().refreshEntryTitleList (gs.getFormData ().getTitle ());
            }
        }
    }

    public static void combineShares (JPassFrame parent)
    {
        MessageDialog.showInformationMessage (parent, "You can type-in, copy&paste or locate the share file " +
                                                      "(weather text file or QR code image file).\n" +
                                                      "Note: For the Prime number field, you can select " +
                                                      "any Share file (.txt or .png) and the Prime number will be " +
                                                      "extracted for it.");
        CombineShares cs = new CombineShares (JPassFrame.getInstance (), "Reveal Secret");

        if (cs.getFormData () != null)
        {
            JPassFrame.getInstance ().getModel ().getEntries ().getEntry ().add (cs.getFormData ());
            JPassFrame.getInstance ().getModel ().setModified (true);
            JPassFrame.getInstance ().refreshFrameTitle ();
            JPassFrame.getInstance ().refreshEntryTitleList (cs.getFormData ().getTitle ());
        }
    }

    public static void restoreAccess (JPassFrame parent)
    {
        RestoreAccess ra = new RestoreAccess (JPassFrame.getInstance (), "Restore Account Access");

        if (ra.getFormData () != null)
        {
            JPassFrame.getInstance ().getModel ().getEntries ().getEntry ().add (ra.getFormData ());
            JPassFrame.getInstance ().getModel ().setModified (true);
            JPassFrame.getInstance ().refreshFrameTitle ();
            JPassFrame.getInstance ().refreshEntryTitleList (ra.getFormData ().getTitle ());
        }
    }

    public static boolean isLinkValid (String link)
    {
        String[] schemes = {"http","https"}; // DEFAULT schemes = "http", "https", "ftp"
        UrlValidator urlValidator = new UrlValidator (schemes);
        return urlValidator.isValid (link);
    }
}
