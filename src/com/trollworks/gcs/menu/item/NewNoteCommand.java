/*
 * Copyright (c) 1998-2017 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

package com.trollworks.gcs.menu.item;

import com.trollworks.gcs.character.SheetDockable;
import com.trollworks.gcs.common.DataFile;
import com.trollworks.gcs.notes.Note;
import com.trollworks.gcs.notes.NotesDockable;
import com.trollworks.gcs.template.TemplateDockable;
import com.trollworks.gcs.widgets.outline.ListOutline;
import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.utility.Localization;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/** Provides the "New Note" command. */
public class NewNoteCommand extends Command {
    @Localize("New Note")
    private static String NOTE;
    @Localize("New Note Container")
    private static String NOTE_CONTAINER;

    static {
        Localization.initialize();
    }

    /** The action command this command will issue. */
    public static final String         CMD_NEW_NOTE           = "NewNote";                                                                                                																								//$NON-NLS-1$
    /** The action command this command will issue. */
    public static final String         CMD_NEW_NOTE_CONTAINER = "NewNoteContainer";                                                                                       																						//$NON-NLS-1$
    /** The "New Note" command. */
    public static final NewNoteCommand INSTANCE               = new NewNoteCommand(false, NOTE, CMD_NEW_NOTE, KeyEvent.VK_M, COMMAND_MODIFIER);
    /** The "New Note Container" command. */
    public static final NewNoteCommand CONTAINER_INSTANCE     = new NewNoteCommand(true, NOTE_CONTAINER, CMD_NEW_NOTE_CONTAINER, KeyEvent.VK_M, SHIFTED_COMMAND_MODIFIER);
    private boolean                    mContainer;

    private NewNoteCommand(boolean container, String title, String cmd, int keyCode, int modifiers) {
        super(title, cmd, keyCode, modifiers);
        mContainer = container;
    }

    @Override
    public void adjust() {
        NotesDockable note = getTarget(NotesDockable.class);
        if (note != null) {
            setEnabled(!note.getOutline().getModel().isLocked());
        } else {
            SheetDockable sheet = getTarget(SheetDockable.class);
            if (sheet != null) {
                setEnabled(true);
            } else {
                setEnabled(getTarget(TemplateDockable.class) != null);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        ListOutline outline;
        DataFile dataFile;
        NotesDockable eqpDockable = getTarget(NotesDockable.class);
        if (eqpDockable != null) {
            dataFile = eqpDockable.getDataFile();
            outline = eqpDockable.getOutline();
            if (outline.getModel().isLocked()) {
                return;
            }
        } else {
            SheetDockable sheet = getTarget(SheetDockable.class);
            if (sheet != null) {
                dataFile = sheet.getDataFile();
                outline = sheet.getSheet().getNoteOutline();
            } else {
                TemplateDockable template = getTarget(TemplateDockable.class);
                if (template != null) {
                    dataFile = template.getDataFile();
                    outline = template.getTemplate().getNoteOutline();
                } else {
                    return;
                }
            }
        }
        Note note = new Note(dataFile, mContainer);
        outline.addRow(note, getTitle(), false);
        outline.getModel().select(note, false);
        outline.scrollSelectionIntoView();
        outline.openDetailEditor(true);
    }
}
