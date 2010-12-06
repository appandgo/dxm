package org.jahia.ajax.gwt.client.widget.subscription;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineContainer;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineLoader;
import org.jahia.ajax.gwt.client.widget.toolbar.action.BaseActionItem;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 6, 2010
 * Time: 1:33:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class SubscriptionManagerActionItem extends BaseActionItem {

    @Override public void handleNewLinkerSelection() {
        final GWTJahiaNode n = linker.getSelectionContext().getSingleSelection();
        setEnabled(n != null && n.getNodeTypes().contains("jnt:newsletter") &&
                (n.getAggregatedPublicationInfo().getStatus() != GWTJahiaPublicationInfo.NOT_PUBLISHED));
    }

    @Override public void onComponentSelection() {
        GWT.runAsync(new RunAsyncCallback() {

            public void onSuccess() {
                EngineContainer container = EngineLoader.createContainer(linker);
                new SubscriptionManager(linker.getSelectionContext().getSingleSelection().getUUID(), linker, container);
                container.showEngine();
            }

            public void onFailure(Throwable reason) {
                Window.alert("Error: "+reason);
            }
        });
    }

}
