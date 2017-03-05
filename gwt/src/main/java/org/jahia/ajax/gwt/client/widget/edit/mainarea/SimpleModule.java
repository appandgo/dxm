/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.dnd.DragSource;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Header;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.contentengine.EditContentEnginePopupListener;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;
import org.jahia.ajax.gwt.client.widget.edit.ModuleSelectionListener;

import java.util.*;

/**
 * Represents an editable module (area) in the rendered page.
 * 
 * @author Thomas Draier
 */
public class SimpleModule extends Module {
    protected boolean hasDragDrop = true;
    protected boolean canHover = true;
    protected boolean editable = true;
    protected boolean addIconInHeader = true;

    protected boolean bindable = false;
    protected Boolean bound = null;
    private String boundProperty = "j:bindedComponent";

    public SimpleModule(String id, String path, Element divElement, MainModule mainModule) {
        super(id, path, divElement, mainModule);
        editable = !"false".equals(DOM.getElementAttribute(divElement, "editable"));
    }

    public SimpleModule(String id, final String path, Element divElement, final MainModule mainModule, boolean header) {
        super(id, path, divElement, mainModule);

        hasDragDrop = !"false".equals(DOM.getElementAttribute(divElement, "dragdrop"));
        editable = !"false".equals(DOM.getElementAttribute(divElement, "editable"));
        bindable = "true".equals(DOM.getElementAttribute(divElement, "bindable"));

        if ((header || bindable) && editable) {
            head = new Header();
            add(head);
            setHeaderText(path.substring(path.lastIndexOf('/') + 1));
            head.addStyleName("x-panel-header");
            head.addStyleName("x-panel-header-simplemodule");
            setBorders(false);
        }

        html = new HTML(divElement.getInnerHTML());
        add(html);


        if (bindable) {
            head.addTool(new ToolButton("x-tool-pin", new BindSelectionListener(mainModule)));
        }
    }

    @Override
    public void onParsed() {
        Log.debug("Add drag source for simple module " + path);

        if (mainModule.getConfig().isEnableDragAndDrop()) {
            if (hasDragDrop) {
                DragSource source = new SimpleModuleDragSource(this);
                source.addDNDListener(mainModule.getEditLinker().getDndListener());
                DropTarget target = new ModuleDropTarget(this, EditModeDNDListener.SIMPLEMODULE_TYPE);
                target.setAllowSelfAsSource(true);
                target.addDNDListener(mainModule.getEditLinker().getDndListener());
            } else {
                new DropTarget(this) {
                    @Override
                    protected void onDragEnter(DNDEvent event) {
                        event.getStatus().setStatus(false);
                    }
                };

            }
        }
        if (editable) {
            sinkEvents(Event.ONCLICK + Event.ONDBLCLICK + Event.ONMOUSEOVER + Event.ONMOUSEOUT + Event.ONCONTEXTMENU);

            Listener<ComponentEvent> listener = new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent ce) {
                    if (selectable && !MainModule.isGlobalSelectionDisabled()) {
                        Log.info("click" + path + " : " + scriptInfo);
                        mainModule.setCtrlActive(ce);
                        if (!ce.isRightClick() || !mainModule.getSelections().containsKey(SimpleModule.this)) {
                            mainModule.getEditLinker().onModuleSelection(SimpleModule.this);
                        }
                    }
                }
            };
            addListener(Events.OnClick, listener);
            addListener(Events.OnContextMenu, listener);
            addListener(Events.OnDoubleClick, new EditContentEnginePopupListener(this, mainModule.getEditLinker()));

            Listener<ComponentEvent> hoverListener = new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent ce) {
                    if (canHover) {
                        Hover.getInstance().addHover(SimpleModule.this);
                    }
                }
            };
            Listener<ComponentEvent> outListener = new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent ce) {
                    Hover.getInstance().removeHover(SimpleModule.this);
                }
            };

            addListener(Events.OnMouseOver, hoverListener);
            addListener(Events.OnMouseOut, outListener);
        }
    }

    public void setNode(GWTJahiaNode node) {
        super.setNode(node);


        if (node.isShared()) {
            this.setToolTip(new ToolTipConfig(Messages.get("info_important", "Important"), Messages.get("info_sharednode", "This is a shared node")));
        }
        if (head != null && addIconInHeader) {
            GWTJahiaNodeType nodeType = ModuleHelper.getNodeType(node.getNodeTypes().get(0));
            head.setIcon(ContentModelIconProvider.getInstance().getIcon(nodeType));
        }

        if (overlayLabel != null) {
            setStyleAttribute("position", "relative");
            insert(overlayLabel, 0);
            overlayLabel.setHeight(Integer.toString(html.getOffsetHeight()) + "px");
            overlayLabel.setWidth(Integer.toString(html.getOffsetWidth()) + "px");
            DOM.setStyleAttribute(html.getElement(), "opacity", opacity);
            layout();
            if (head != null)  {
                Component headerHtml = new Text(overlayLabel.getText());
                headerHtml.setStyleAttribute("color",overlayColorText);
                headerHtml.setStyleAttribute("font-weight","bold");
                head.addTool(headerHtml);
            }
        }

        if (bindable) {
            final JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();
            async.getProperties(node.getPath(), null, new BaseAsyncCallback<GWTJahiaGetPropertiesResult>() {
                public void onSuccess(GWTJahiaGetPropertiesResult gwtJahiaGetPropertiesResult) {
                    if (gwtJahiaGetPropertiesResult.getProperties().containsKey(boundProperty)) {
                        final GWTJahiaNodeProperty o = gwtJahiaGetPropertiesResult.getProperties().get(boundProperty);
                        if (o.getValues().get(0).getNode().getPath().equals(mainModule.getPath())) {
                            setHeaderText(headerText + " - Linked to: main resource");
                            bound = Boolean.FALSE;
                        } else {
                            setHeaderText(headerText + " - Linked to: " + o.getValues().get(0).getNode().getName());
                            bound = Boolean.TRUE;
                        }
                    } else {
                        bound = Boolean.FALSE;
                        setHeaderText(head.getHtml() + " - Linked to: main resource");
                    }
                }
            });

        }
    }

    /**
     * Handler class for the bind component action. 
     */
    private class BindSelectionListener extends SelectionListener<IconButtonEvent> {
        private final MainModule mainModule;

        public BindSelectionListener(MainModule mainModule) {
            this.mainModule = mainModule;
        }

        public void componentSelected(IconButtonEvent event) {
            if (bound != null && bound) {
                List<GWTJahiaNodeProperty> properties = new ArrayList<GWTJahiaNodeProperty>();
                final GWTJahiaNodeProperty gwtJahiaNodeProperty = new GWTJahiaNodeProperty(boundProperty,
                        new GWTJahiaNodePropertyValue((String) null,
                                GWTJahiaNodePropertyType.WEAKREFERENCE));
                properties.add(gwtJahiaNodeProperty);
                JahiaContentManagementService.App.getInstance()
                        .saveProperties(Arrays.asList(node), properties, null, new BaseAsyncCallback<Object>() {
                            public void onSuccess(Object o) {
                                Map<String, Object> data = new HashMap<String, Object>();
                                data.put(Linker.REFRESH_MAIN, true);
                                getMainModule().getEditLinker().refresh(data);
                            }

                            public void onApplicationFailure(Throwable throwable) {
                                Window.alert(Messages.getWithArgs("label.gwt.error", "Error: {0}",
                                        new Object[] { throwable }));
                            }
                        });

            } else {
                String s = JahiaGWTParameters.getContextPath();
                if (s.equals("/")) {
                    s = "";
                }
                mainModule.getInnerElement().getStyle().setProperty("cursor",
                        "url('" + s + "/gwt/resources/images/xtheme-jahia/link.cur'), pointer");

                mainModule.getEditLinker().setSelectionListener(new ModuleSelectionListener() {
                    public void onModuleSelection(Module selection) {
                        mainModule.getInnerElement().getStyle().setProperty("cursor", "");
                        mainModule.getEditLinker().setSelectionListener(null);
                        List<GWTJahiaNodeProperty> properties = new ArrayList<GWTJahiaNodeProperty>();
                        final GWTJahiaNodeProperty gwtJahiaNodeProperty;
                        if (selection.getNode() != null && selection.getNode() != node) {
                            gwtJahiaNodeProperty = new GWTJahiaNodeProperty(boundProperty, new GWTJahiaNodePropertyValue(selection.getNode(), GWTJahiaNodePropertyType.WEAKREFERENCE));
                        } else if (selection instanceof AreaModule) {
                            String areaHolder = ((AreaModule) selection).getAreaHolder();
                            gwtJahiaNodeProperty = new GWTJahiaNodeProperty(boundProperty, new GWTJahiaNodePropertyValue(areaHolder, GWTJahiaNodePropertyType.STRING));
                        } else {
                            return;
                        }
                        properties.add(gwtJahiaNodeProperty);
                        JahiaContentManagementService.App.getInstance()
                                .saveProperties(Arrays.asList(node), properties, null, new BaseAsyncCallback<Object>() {
                                    public void onSuccess(Object o) {
                                        Map<String, Object> data = new HashMap<String, Object>();
                                        data.put(Linker.REFRESH_MAIN, true);
                                        getMainModule().getEditLinker().refresh(data);
                                    }

                                    public void onApplicationFailure(Throwable throwable) {
                                        Window.alert(Messages.getWithArgs("label.gwt.error", "Error: {0}",
                                                new Object[] { throwable }));
                                    }
                                });

                    }
                });
            }
        }
    }
}
