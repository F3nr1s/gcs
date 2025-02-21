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

package com.trollworks.gcs.notes;

import com.trollworks.gcs.widgets.outline.RowEditor;
import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.layout.ColumnLayout;
import com.trollworks.toolkit.ui.layout.RowDistribution;
import com.trollworks.toolkit.utility.Localization;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

/** The detailed editor for {@link Note}s. */
public class NoteEditor extends RowEditor<Note> {
    @Localize("Note")
    private static String NAME;

    static {
        Localization.initialize();
    }

    private JTextArea mEditor;

    /**
     * Creates a new {@link Note} editor.
     *
     * @param note The {@link Note} to edit.
     */
    public NoteEditor(Note note) {
        super(note);

        JPanel content = new JPanel(new ColumnLayout(2, RowDistribution.GIVE_EXCESS_TO_LAST));
        JLabel icon = new JLabel(note.getIcon(true));

        mEditor = new JTextArea(note.getDescription());
        mEditor.setLineWrap(true);
        mEditor.setWrapStyleWord(true);
        mEditor.setEnabled(mIsEditable);
        JScrollPane scroller = new JScrollPane(mEditor, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroller.setMinimumSize(new Dimension(400, 300));
        icon.setVerticalAlignment(SwingConstants.TOP);
        icon.setAlignmentY(-1f);
        content.add(icon);
        content.add(scroller);
        add(content);
    }

    @Override
    public boolean applyChangesSelf() {
        return mRow.setDescription(mEditor.getText());
    }

    @Override
    public void finished() {
        // Unused
    }
}
