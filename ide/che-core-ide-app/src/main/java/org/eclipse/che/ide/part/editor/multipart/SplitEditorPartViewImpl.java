/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.part.editor.multipart;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.constraints.Direction;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.ide.api.constraints.Direction.HORIZONTALLY;
import static org.eclipse.che.ide.api.constraints.Direction.VERTICALLY;

/**
 * @author Roman Nikitenko
 */
public class SplitEditorPartViewImpl extends Composite implements SplitEditorPartView {
    private final static String VERTICAL_DRAGGER_CLASS   = "gwt-SplitLayoutPanel-VDragger";
    private final static String HORIZONTAL_DRAGGER_CLASS = "gwt-SplitLayoutPanel-HDragger";


    private SimpleLayoutPanel specimenPanel;
    private IsWidget neighbor;
    private IsWidget           specimen;
    private Direction        direction;
    private SplitLayoutPanel parent;
    private IsWidget replica;



//    public SplitEditorPartViewImpl(IsWidget replica, Direction direction) {
//        this.specimenPanel = specimenPanel;
//        this.replica = replica;
//        this.direction = direction;
//
//        contentPanel = new SplitLayoutPanel(5);
//        contentPanel.ensureDebugId("editor-splitlayout-panel");
//
//        if (direction == VERTICALLY) {
//            contentPanel.addWest(specimenPanel, specimenPanel.getOffsetWidth());
//            contentPanel.add(replica);
//        }
//
//        if (direction == HORIZONTALLY) {
//            contentPanel.addNorth(specimenPanel, specimenPanel.getOffsetHeight());
//            contentPanel.add(replica);
//        }
//        tuneSplitter();
//    }

    @AssistedInject
    public SplitEditorPartViewImpl(@Assisted IsWidget specimen) {
        this.specimen = specimen;
        specimenPanel = new SimpleLayoutPanel();
        specimenPanel.add(specimen);

        //TODO
        specimenPanel.ensureDebugId("specimen-panel");
    }

    @AssistedInject
    public SplitEditorPartViewImpl(@Assisted IsWidget specimen, @Assisted SplitLayoutPanel parent) {
        this.specimen = specimen;
        this.parent = parent;

        specimenPanel = new SimpleLayoutPanel();
        specimenPanel.add(specimen);


        //TODO
        specimenPanel.ensureDebugId("specimen-panel");
    }

    @Override
    public SplitEditorPartView split(IsWidget replica, Direction direction) {
        this.replica = replica;
        SplitLayoutPanel splitLayoutPanel = new SplitLayoutPanel(5);


        //TODO
        parent = splitLayoutPanel;
        splitLayoutPanel.ensureDebugId("splitLayoutPanel");

        if (direction == VERTICALLY) {
            return splitVertically(replica, splitLayoutPanel);
        }

        if (direction == HORIZONTALLY) {
            return splitHorizontally(replica, splitLayoutPanel);
        }
        return null;
    }

    @Override
    public void setNeighbor(IsWidget neighbor) {
        this.neighbor = neighbor;
    }

    private SplitEditorPartView splitVertically(IsWidget replica, SplitLayoutPanel container) {
//        Log.error(getClass(), "***** constraints.direction == VERTICALLY");

        int newSize = specimenPanel.getOffsetWidth() / 2;

        SplitEditorPartView splitEditorPartView = new SplitEditorPartViewImpl(replica, container);



        SimpleLayoutPanel newSpecimenPanel = new SimpleLayoutPanel();
        newSpecimenPanel.add(specimen);
        newSpecimenPanel.ensureDebugId("newSpecimenPanel_panel");

        splitEditorPartView.setNeighbor(specimen);

        container.addWest(newSpecimenPanel, newSize);
        container.add(splitEditorPartView);
        specimenPanel.add(container);
        specimenPanel = newSpecimenPanel;

        tuneSplitter(container);
        return splitEditorPartView;
    }

    private SplitEditorPartView splitHorizontally(IsWidget replica, SplitLayoutPanel container) {
//        Log.error(getClass(), "***** constraints.direction == HORIZONTALLY");

        int newSize = specimenPanel.getOffsetHeight() / 2;

        SplitEditorPartView splitEditorPartView = new SplitEditorPartViewImpl(replica, container);

        SimpleLayoutPanel newSpecimenPanel = new SimpleLayoutPanel();
        newSpecimenPanel.add(specimen);

        splitEditorPartView.setNeighbor(specimen);

        container.addNorth(newSpecimenPanel, newSize);
        container.add(splitEditorPartView);
        specimenPanel.add(container);
        specimenPanel = newSpecimenPanel;

        tuneSplitter(container);
        return splitEditorPartView;
    }


    @Override
    public void addPartStack(@NotNull PartStack partStack, PartStack relativePartStack, Constraints constraints) {

    }

    @Override
    public void removePartStack(@NotNull PartStack partStack) {
        Log.error(getClass(), "******************************************************************** ");

        if ( replica != null) {
            boolean isPresent = parent.remove(specimenPanel);
            Log.error(getClass(), "parent.remove(specimenPanel)=== " + isPresent);


//            NodeList<Node> nodes = parent.getElement().getChildNodes();
//            for (int i = 0; i < nodes.getLength(); i++) {
//                Node node = nodes.getItem(i);
//                if (node.hasChildNodes()) {
//                    Element el = node.getFirstChild().cast();
//                    String className = el.getClassName();
//                    if (HORIZONTAL_DRAGGER_CLASS.equals(className)) {
//                    Log.error(getClass(), "//////////// HORIZONTAL_DRAGGER_CLASS");
//                        node.removeFromParent();
//                    } else if (VERTICAL_DRAGGER_CLASS.equals(className)) {
//                    Log.error(getClass(), "//////////// VERTICAL_DRAGGER_CLASS");
//                        node.removeFromParent();
//                    }
//                }
//            }

//            if (neighbor != null) {
//                neighbor.asWidget().getParent().removeFromParent();
//                parent.add(neighbor);
//            }





            parent.forceLayout();
        } else {



            specimenPanel.removeFromParent();
        if (neighbor != null) {
            Log.error(getClass(), "neighbor != null");
            neighbor.asWidget().getParent().removeFromParent();
            parent.add(neighbor);
        } else {
            Log.error(getClass(), "neighbor = null");
        }


        }
        Log.error(getClass(), "********************************************************************");
    }

    /**
     * Improves splitter visibility.
     */
    private void tuneSplitter(SplitLayoutPanel splitLayoutPanel) {
        NodeList<Node> nodes = splitLayoutPanel.getElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.getItem(i);
            if (node.hasChildNodes()) {
                Element el = node.getFirstChild().cast();
                String className = el.getClassName();
                if (HORIZONTAL_DRAGGER_CLASS.equals(className)) {
//                    Log.error(getClass(), "//////////// HORIZONTAL_DRAGGER_CLASS");
                    tuneVerticalSplitter(el);
                } else if (VERTICAL_DRAGGER_CLASS.equals(className)) {
//                    Log.error(getClass(), "//////////// VERTICAL_DRAGGER_CLASS");
                    tuneHorizontalSplitter(el);
                }
            }
        }
    }

    /**
     * Tunes splitter. Makes it wider and adds double border to seem rich.
     *
     * @param el
     *         element to tune
     */
    private void tuneVerticalSplitter(Element el) {
        /** Add Z-Index to move the splitter on the top and make content visible */
        el.getParentElement().getStyle().setProperty("zIndex", "1000");
        el.getParentElement().getStyle().setProperty("overflow", "visible");

        /** Tune splitter catch panel */
        el.getStyle().setProperty("boxSizing", "border-box");
        el.getStyle().setProperty("width", "5px");
        el.getStyle().setProperty("overflow", "hidden");
        el.getStyle().setProperty("marginLeft", "-3px");
        el.getStyle().setProperty("backgroundColor", "transparent");

        /** Add small border */
        DivElement smallBorder = Document.get().createDivElement();
        smallBorder.getStyle().setProperty("position", "absolute");
        smallBorder.getStyle().setProperty("width", "1px");
        smallBorder.getStyle().setProperty("height", "100%");
        smallBorder.getStyle().setProperty("left", "3px");
        smallBorder.getStyle().setProperty("top", "0px");
        smallBorder.getStyle().setProperty("backgroundColor", Style.getSplitterSmallBorderColor());
        el.appendChild(smallBorder);
    }

    /**
     * Tunes bottom splitter. Makes it tiny but with a transparent area for easy resizing.
     *
     * @param el
     *         element to tune
     */
    private void tuneHorizontalSplitter(Element el) {
        /** Add Z-Index to move the splitter on the top and make content visible */
        el.getParentElement().getStyle().setProperty("zIndex", "1000");
        el.getParentElement().getStyle().setProperty("overflow", "visible");

        el.getStyle().setProperty("height", "3px");
        el.getStyle().setProperty("marginTop", "-2px");
        el.getStyle().setProperty("backgroundColor", "transparent");

        /** Add small border */
        DivElement delimiter = Document.get().createDivElement();
        delimiter.getStyle().setProperty("position", "absolute");
        delimiter.getStyle().setProperty("width", "100%");
        delimiter.getStyle().setProperty("height", "1px");
        delimiter.getStyle().setProperty("left", "0px");
        delimiter.getStyle().setProperty("backgroundColor", Style.getSplitterSmallBorderColor());
        delimiter.getStyle().setProperty("top", "2px");
        el.appendChild(delimiter);
    }

    @Override
    public Widget asWidget() {
        return specimenPanel;
    }
}
