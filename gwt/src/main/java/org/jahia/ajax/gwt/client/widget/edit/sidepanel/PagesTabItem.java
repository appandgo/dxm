/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.TreeGridDropTarget;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.NodeColumnConfigList;
import org.jahia.ajax.gwt.client.widget.edit.*;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Selection;
import org.jahia.ajax.gwt.client.widget.node.GWTJahiaNodeTreeFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Side panel tab item for browsing the pages tree.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 2:22:37 PM
 */
public class PagesTabItem extends SidePanelTabItem {
    protected List<String> folderTypes = new ArrayList<String>();
    private List<String> paths = new ArrayList<String>();

    protected transient TreeGrid<GWTJahiaNode> pageTree;
    protected transient GWTJahiaNodeTreeFactory pageFactory;
    protected transient String path;

    public TabItem create(GWTSidePanelTab config) {
        super.create(config);
        VBoxLayout l = new VBoxLayout();
        l.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
        tab.setLayout(new FitLayout());
        return tab;
    }

    private void initPageTree() {
        GWTJahiaNodeTreeFactory factory = new GWTJahiaNodeTreeFactory(paths);
        factory.setNodeTypes(folderTypes);
        factory.setFields(config.getTreeColumnKeys());
        this.pageFactory = factory;
        this.pageFactory.setSelectedPath(path);

        NodeColumnConfigList columns = new NodeColumnConfigList(config.getTreeColumns());
        columns.init();
        columns.get(0).setRenderer(new TreeGridCellRenderer());

        pageTree = factory.getTreeGrid(new ColumnModel(columns));

        pageTree.setAutoExpandColumn(columns.getAutoExpand());
        pageTree.getTreeView().setRowHeight(25);
        pageTree.getTreeView().setForceFit(true);
        pageTree.setHeight("100%");
        pageTree.setIconProvider(ContentModelIconProvider.getInstance());
        pageTree.getTreeStore().setStoreSorter(new StoreSorter<GWTJahiaNode>() {
            @Override public int compare(Store<GWTJahiaNode> gwtJahiaNodeStore, GWTJahiaNode m1, GWTJahiaNode m2,
                                         String property) {
                if (!m1.getInheritedNodeTypes().contains("jmix:navMenuItem") && m2.getInheritedNodeTypes().contains("jmix:navMenuItem")) {
                    return 1;
                } else if (!m2.getInheritedNodeTypes().contains("jmix:navMenuItem") && m1.getInheritedNodeTypes().contains("jmix:navMenuItem")) {
                    return -1;
                } else {
                    return gwtJahiaNodeStore.getModels().indexOf(m2) - gwtJahiaNodeStore.getModels().indexOf(m1);
                }
            }
        });
        pageTree.setSelectionModel(new TreeGridClickSelectionModel());
        this.pageTree.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> se) {
                final GWTJahiaNode node = se.getSelectedItem();
                if (node != null && !node.getPath().equals(editLinker.getMainModule().getPath()) &&
                    !node.getNodeTypes().contains("jnt:virtualsite") &&
                        !node.getInheritedNodeTypes().contains("jmix:link")) {
                    editLinker.onMainSelection(node.getPath(), null, null);
                }
            }
        });
        this.pageTree.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        
        pageTree.setContextMenu(createContextMenu(config.getTreeContextMenu(), pageTree.getSelectionModel()));

        tab.add(pageTree);
    }

    @Override public void handleNewMainSelection(String path) {
        if (pageTree != null && (pageTree.getSelectionModel().getSelectedItem() == null || !path.equals(
                pageTree.getSelectionModel().getSelectedItem().getPath()))) {
            pageFactory.setSelectedPath(Arrays.asList(path));
        }
    }

    private void initDND() {
        EditModeTreeGridDragSource source = new PageTreeGridDragSource();
        TreeGridDropTarget target = new PageTreeGridDropTarget();
        target.setAllowDropOnLeaf(true);
        target.setAllowSelfAsSource(true);
        target.setAutoExpand(true);
        target.setFeedback(DND.Feedback.INSERT);

        source.addDNDListener(editLinker.getDndListener());
        target.addDNDListener(editLinker.getDndListener());
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        path = linker.getMainModule().getPath();
        initPageTree();
        initDND();
    }

    @Override
    public void refresh(int flag) {
        if ((flag & Linker.REFRESH_PAGES) != 0) {
            pageTree.getTreeStore().removeAll();
            pageTree.getTreeStore().getLoader().load();
        }
    }

    public void addOpenPath(String path) {
        pageFactory.setOpenPath(path);
    }

    public class PageTreeGridDropTarget extends TreeGridDropTarget {
        public PageTreeGridDropTarget() {
            super(PagesTabItem.this.pageTree);
        }

        @Override
        protected void showFeedback(DNDEvent e) {
            super.showFeedback(e);
            e.getStatus().setData("type", status);
            if (activeItem != null) {
                GWTJahiaNode activeNode = (GWTJahiaNode) activeItem.getModel();
                GWTJahiaNode parent = pageTree.getTreeStore().getParent(activeNode);
                if (status == 1) {
                    List<GWTJahiaNode> children = pageTree.getTreeStore().getChildren(parent);
                    int next = children.indexOf(activeNode) + 1;
                    if (next < children.size()) {
                        GWTJahiaNode n = children.get(next);
                        e.getStatus().setData(EditModeDNDListener.TARGET_NEXT_NODE, n);
                    } else {
                        e.getStatus().setData(EditModeDNDListener.TARGET_NEXT_NODE, null);
                    }
                }

                if (activeNode.getInheritedNodeTypes().contains("jmix:navMenuItem")) {
                    e.getStatus().setData(EditModeDNDListener.TARGET_TYPE, EditModeDNDListener.PAGETREE_TYPE);
                } else if (activeNode.getNodeTypes().contains("jnt:templatesFolder")
                        && EditModeDNDListener.PAGETREE_TYPE.equals(e.getStatus().getData(EditModeDNDListener.SOURCE_TYPE))) {
                    e.getStatus().setData(EditModeDNDListener.TARGET_TYPE, EditModeDNDListener.TEMPLATETREE_TYPE);
                } else {
                    e.getStatus().setStatus(false);
                }

                e.getStatus().setData(EditModeDNDListener.TARGET_NODE, activeNode);
                e.getStatus().setData(EditModeDNDListener.TARGET_PARENT, parent);
                e.getStatus().setData(EditModeDNDListener.TARGET_PATH, activeNode.get("path"));
            } else {
                e.getStatus().setData(EditModeDNDListener.TARGET_NODE, null);
                e.getStatus().setData(EditModeDNDListener.TARGET_PARENT, null);
                e.getStatus().setData(EditModeDNDListener.TARGET_PATH, null);
            }
        }

        @Override
        protected void onDragDrop(DNDEvent event) {
            //
        }

        public AsyncCallback<Object> getCallback() {
            AsyncCallback<Object> callback = new BaseAsyncCallback<Object>() {
                public void onSuccess(Object o) {
                    editLinker.refresh(Linker.REFRESH_MAIN + Linker.REFRESH_PAGES);
                }

                public void onApplicationFailure(Throwable throwable) {
                    Window.alert("Failed : "+throwable);
                }
            };
            return callback;
        }
    }

    private class PageTreeGridDragSource extends EditModeTreeGridDragSource {
        public PageTreeGridDragSource() {
            super(PagesTabItem.this.pageTree);
        }

        @Override
        protected void onDragStart(DNDEvent e) {
            super.onDragStart(e);
            Selection.getInstance().hide();

            List<GWTJahiaNode> l = new ArrayList<GWTJahiaNode>();
            final GWTJahiaNode node = PagesTabItem.this.pageTree.getSelectionModel().getSelectedItem();
            if (node.getInheritedNodeTypes().contains("jmix:navMenuItem") && node.isWriteable() && !node.isLocked()) {
                l.add(node);
                e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, EditModeDNDListener.PAGETREE_TYPE);
                e.getStatus().setData(EditModeDNDListener.SOURCE_NODES, l);
            } else {
                e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, null);
                e.getStatus().setStatus(false);
                e.setCancelled(true);
            }
        }


        @Override
        protected void onDragDrop(DNDEvent event) {
            // do nothing
        }
    }

    public List<String> getFolderTypes() {
        return folderTypes;
    }

    public void setFolderTypes(List<String> folderTypes) {
        this.folderTypes = folderTypes;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

}
