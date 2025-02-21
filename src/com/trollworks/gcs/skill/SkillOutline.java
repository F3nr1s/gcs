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

package com.trollworks.gcs.skill;

import com.trollworks.gcs.character.GURPSCharacter;
import com.trollworks.gcs.common.DataFile;
import com.trollworks.gcs.common.ListFile;
import com.trollworks.gcs.library.LibraryFile;
import com.trollworks.gcs.menu.edit.Incrementable;
import com.trollworks.gcs.menu.edit.SkillLevelIncrementable;
import com.trollworks.gcs.menu.edit.TechLevelIncrementable;
import com.trollworks.gcs.template.Template;
import com.trollworks.gcs.widgets.outline.ListOutline;
import com.trollworks.gcs.widgets.outline.ListRow;
import com.trollworks.gcs.widgets.outline.MultipleRowUndo;
import com.trollworks.gcs.widgets.outline.RowPostProcessor;
import com.trollworks.gcs.widgets.outline.RowUndo;
import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.collections.FilteredIterator;
import com.trollworks.toolkit.ui.widget.outline.OutlineModel;
import com.trollworks.toolkit.ui.widget.outline.Row;
import com.trollworks.toolkit.utility.Localization;
import com.trollworks.toolkit.utility.text.Numbers;

import java.awt.EventQueue;
import java.awt.dnd.DropTargetDragEvent;
import java.util.ArrayList;
import java.util.List;

/** An outline specifically for skills. */
public class SkillOutline extends ListOutline implements Incrementable, TechLevelIncrementable, SkillLevelIncrementable {
    @Localize("Increment Points")
    @Localize(locale = "de", value = "Punkte erhöhen")
    @Localize(locale = "ru", value = "Увеличить очки")
    @Localize(locale = "es", value = "Incrementar Puntos")
    private static String INCREMENT;
    @Localize("Decrement Points")
    @Localize(locale = "de", value = "Punkte verringern")
    @Localize(locale = "ru", value = "Уменьшить очки")
    @Localize(locale = "es", value = "Disminuir Puntos")
    private static String DECREMENT;
    @Localize("Increment Skill Level")
    @Localize(locale = "ru", value = "Увеличить уровень умения")
    private static String INCREMENT_SKILL_LEVEL;
    @Localize("Decrement Skill Level")
    @Localize(locale = "ru", value = "Уменьшить уровень умения")
    private static String DECREMENT_SKILL_LEVEL;

    static {
        Localization.initialize();
    }

    private static OutlineModel extractModel(DataFile dataFile) {
        if (dataFile instanceof GURPSCharacter) {
            return ((GURPSCharacter) dataFile).getSkillsRoot();
        }
        if (dataFile instanceof Template) {
            return ((Template) dataFile).getSkillsModel();
        }
        if (dataFile instanceof LibraryFile) {
            return ((LibraryFile) dataFile).getSkillList().getModel();
        }
        return ((ListFile) dataFile).getModel();
    }

    /**
     * Create a new skills outline.
     *
     * @param dataFile The owning data file.
     */
    public SkillOutline(DataFile dataFile) {
        this(dataFile, extractModel(dataFile));
    }

    /**
     * Create a new skills outline.
     *
     * @param dataFile The owning data file.
     * @param model The {@link OutlineModel} to use.
     */
    public SkillOutline(DataFile dataFile, OutlineModel model) {
        super(dataFile, model, Skill.ID_LIST_CHANGED);
        SkillColumn.addColumns(this, dataFile);
    }

    @Override
    public String getDecrementTitle() {
        return DECREMENT;
    }

    @Override
    public String getIncrementTitle() {
        return INCREMENT;
    }

    @Override
    public boolean canDecrement() {
        return (mDataFile instanceof GURPSCharacter || mDataFile instanceof Template) && selectionHasLeafRows(true);
    }

    @Override
    public boolean canIncrement() {
        return (mDataFile instanceof GURPSCharacter || mDataFile instanceof Template) && selectionHasLeafRows(false);
    }

    private boolean selectionHasLeafRows(boolean requirePointsAboveZero) {
        for (Skill skill : new FilteredIterator<>(getModel().getSelectionAsList(), Skill.class)) {
            if (!skill.canHaveChildren() && (!requirePointsAboveZero || skill.getPoints() > 0)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    @Override
    public void decrement() {
        List<RowUndo> undos = new ArrayList<>();
        for (Skill skill : new FilteredIterator<>(getModel().getSelectionAsList(), Skill.class)) {
            if (!skill.canHaveChildren()) {
                int points = skill.getPoints();
                if (points > 0) {
                    RowUndo undo = new RowUndo(skill);

                    skill.setPoints(points - 1);
                    if (undo.finish()) {
                        undos.add(undo);
                    }
                }
            }
        }
        if (!undos.isEmpty()) {
            repaintSelection();
            new MultipleRowUndo(undos);
        }
    }

    @SuppressWarnings("unused")
    @Override
    public void increment() {
        List<RowUndo> undos = new ArrayList<>();
        for (Skill skill : new FilteredIterator<>(getModel().getSelectionAsList(), Skill.class)) {
            if (!skill.canHaveChildren()) {
                RowUndo undo = new RowUndo(skill);
                skill.setPoints(skill.getPoints() + 1);
                if (undo.finish()) {
                    undos.add(undo);
                }
            }
        }
        if (!undos.isEmpty()) {
            repaintSelection();
            new MultipleRowUndo(undos);
        }
    }

    @Override
    public String getIncrementSkillLevelTitle() {
        return INCREMENT_SKILL_LEVEL;
    }

    @Override
    public String getDecrementSkillLevelTitle() {
        return DECREMENT_SKILL_LEVEL;
    }

    @Override
    public boolean canIncrementSkillLevel() {
        return canIncrement();
    }

    @Override
    public boolean canDecrementSkillLevel() {
        return canDecrement();
    }

    @SuppressWarnings("unused")
    @Override
    public void incrementSkillLevel() {
        List<RowUndo> undos = new ArrayList<>();
        for (Skill skill : new FilteredIterator<>(getModel().getSelectionAsList(), Skill.class)) {
            if (!skill.canHaveChildren()) {
                int basePoints = skill.getPoints() + 1;
                int maxPoints = basePoints + (skill.getDifficulty() == SkillDifficulty.W ? 12 : 4);
                int oldLevel = skill.getLevel();
                RowUndo undo = new RowUndo(skill);
                for (int points = basePoints; points < maxPoints; points++) {
                    skill.setPoints(points);
                    if (skill.getLevel() > oldLevel) {
                        break;
                    }
                }
                // if skill level didn't change, perhaps we hit the limit and should reset points to
                // old value
                if (skill.getLevel() == oldLevel) {
                    skill.setPoints(basePoints - 1);
                }
                if (undo.finish()) {
                    undos.add(undo);
                }
            }
        }
        if (!undos.isEmpty()) {
            repaintSelection();
            new MultipleRowUndo(undos);
        }
    }

    @SuppressWarnings("unused")
    @Override
    public void decrementSkillLevel() {
        List<RowUndo> undos = new ArrayList<>();
        for (Skill skill : new FilteredIterator<>(getModel().getSelectionAsList(), Skill.class)) {
            if (!skill.canHaveChildren()) {
                RowUndo undo = new RowUndo(skill);
                int oldLevel = skill.getLevel();
                int points = skill.getPoints() - 1;
                skill.setPoints(points);
                if (skill.getLevel() == Integer.MIN_VALUE) {
                    skill.setPoints(0);
                } else {
                    while (points > 0) {
                        skill.setPoints(--points);
                        if (skill.getLevel() < oldLevel - 1) {
                            skill.setPoints(points + 1);
                            break;
                        }
                    }
                }
                if (undo.finish()) {
                    undos.add(undo);
                }
            }
        }
        if (!undos.isEmpty()) {
            repaintSelection();
            new MultipleRowUndo(undos);
        }
    }

    @Override
    public boolean canIncrementTechLevel() {
        return checkTechLevel(0);
    }

    @Override
    public boolean canDecrementTechLevel() {
        return checkTechLevel(1);
    }

    private boolean checkTechLevel(int minLevel) {
        for (Skill skill : new FilteredIterator<>(getModel().getSelectionAsList(), Skill.class)) {
            if (!skill.canHaveChildren() && getCurrentTechLevel(skill) >= minLevel) {
                return true;
            }
        }
        return false;
    }

    private static int getCurrentTechLevel(Skill skill) {
        String tl = skill.getTechLevel();
        if (tl != null) {
            tl = tl.trim();
            if (!tl.isEmpty()) {
                for (int i = tl.length(); --i >= 0;) {
                    if (!Character.isDigit(tl.charAt(i))) {
                        return -1;
                    }
                }
                return Numbers.extractInteger(tl, -1, 0, Integer.MAX_VALUE, false);
            }
        }
        return -1;
    }

    @SuppressWarnings("unused")
    @Override
    public void incrementTechLevel() {
        List<RowUndo> undos = new ArrayList<>();
        for (Skill skill : new FilteredIterator<>(getModel().getSelectionAsList(), Skill.class)) {
            if (!skill.canHaveChildren()) {
                int tl = getCurrentTechLevel(skill);
                if (tl >= 0) {
                    RowUndo undo = new RowUndo(skill);
                    skill.setTechLevel(Integer.toString(tl + 1));
                    if (undo.finish()) {
                        undos.add(undo);
                    }
                }
            }
        }
        if (!undos.isEmpty()) {
            repaintSelection();
            new MultipleRowUndo(undos);
        }
    }

    @SuppressWarnings("unused")
    @Override
    public void decrementTechLevel() {
        List<RowUndo> undos = new ArrayList<>();
        for (Skill skill : new FilteredIterator<>(getModel().getSelectionAsList(), Skill.class)) {
            if (!skill.canHaveChildren()) {
                int tl = getCurrentTechLevel(skill);
                if (tl >= 1) {
                    RowUndo undo = new RowUndo(skill);
                    skill.setTechLevel(Integer.toString(tl - 1));
                    if (undo.finish()) {
                        undos.add(undo);
                    }
                }
            }
        }
        if (!undos.isEmpty()) {
            repaintSelection();
            new MultipleRowUndo(undos);
        }
    }

    /**
     * Returns {@code true} if all selected skills can switch defaults.
     *
     * @return {@code true} if all selected skills can switch defaults.
     * @see Skill#canSwapDefaults(Skill)
     */
    public boolean canSwapDefaults() {
        for (Skill skill : new FilteredIterator<>(getModel().getSelectionAsList(), Skill.class)) {
            if (skill.canHaveChildren() || !skill.canSwapDefaults(skill.getDefaultSkill()) && findBestSwappableSkill(skill) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Switches Defaults for selected Skills.
     */
    @SuppressWarnings("unused")
    public void swapDefaults() {
        ArrayList<RowUndo> undos = new ArrayList<>();
        for (Skill skill : new FilteredIterator<>(getModel().getSelectionAsList(), Skill.class)) {
            if (!skill.canHaveChildren()) {
                if (skill.canSwapDefaults(skill.getDefaultSkill())) {
                    swapDeafaults(undos, skill);
                } else {
                    swapDeafaults(undos, findBestSwappableSkill(skill));
                }
            }
        }
        if (!undos.isEmpty()) {
            repaintSelection();
            new MultipleRowUndo(undos);
        }
    }

    /**
     * Swaps {@code skill} default with its current default.
     *
     * @param undos Undos that are created
     * @param skill Skill to have its default swapped.
     */
    private static void swapDeafaults(ArrayList<RowUndo> undos, Skill skill) {
        if (skill != null) {
            RowUndo undo1 = new RowUndo(skill);
            RowUndo undo2 = new RowUndo(skill.getDefaultSkill());
            skill.swapDefault();
            if (undo1.finish()) {
                undos.add(undo1);
            }
            if (undo2.finish()) {
                undos.add(undo2);
            }
        }
    }

    /**
     * Finds the best skill to swap its default with. The resulting skill must default to
     * {@code skill} and must be swappable with {@code skill}.
     *
     * @param skillToSwapWith Skill to find a partner for.
     * @return best skill to swap its default with.
     */
    private Skill findBestSwappableSkill(Skill skillToSwapWith) {
        Skill result = null;
        for (Skill skill : new FilteredIterator<>(getModel().getRows(), Skill.class)) {
            if (skillToSwapWith.equals(skill.getDefaultSkill()) && skill.canSwapDefaults(skillToSwapWith)) {
                if (result == null || result.getLevel() < skill.getLevel()) {
                    result = skill;
                }
            }
        }
        return result;
    }

    @Override
    protected boolean isRowDragAcceptable(DropTargetDragEvent dtde, Row[] rows) {
        return !getModel().isLocked() && rows.length > 0 && rows[0] instanceof Skill;
    }

    @Override
    public void convertDragRowsToSelf(List<Row> list) {
        OutlineModel model = getModel();
        Row[] rows = model.getDragRows();
        boolean forSheetOrTemplate = mDataFile instanceof GURPSCharacter || mDataFile instanceof Template;
        ArrayList<ListRow> process = forSheetOrTemplate ? new ArrayList<>() : null;

        for (Row element : rows) {
            ListRow row;

            if (element instanceof Technique) {
                row = new Technique(mDataFile, (Technique) element, forSheetOrTemplate);
            } else {
                row = new Skill(mDataFile, (Skill) element, true, forSheetOrTemplate);
            }

            model.collectRowsAndSetOwner(list, row, false);
            if (forSheetOrTemplate) {
                addRowsToBeProcessed(process, row);
            }
        }

        if (forSheetOrTemplate) {
            EventQueue.invokeLater(new RowPostProcessor(this, process));
        }
    }
}
