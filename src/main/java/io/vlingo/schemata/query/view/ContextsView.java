// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.schemata.query.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContextsView {
    private final List<ContextItem> contexts;

    public static ContextsView empty() {
        return new ContextsView();
    }

    private ContextsView() {
        this.contexts = new ArrayList<>();
    }

    private ContextsView(List<ContextItem> contexts) {
        this.contexts = contexts;
    }

    public ContextsView add(final ContextItem unit) {
        if (contexts.contains(unit)) {
            return this;
        } else {
            ContextsView result = new ContextsView(new ArrayList<>(contexts));
            result.contexts.add(unit);

            return result;
        }
    }

    public ContextItem get(final String contextId) {
        ContextItem context = ContextItem.only(contextId);

        final int index = contexts.indexOf(context);

        if (index >= 0) {
            context = contexts.get(index);
        }

        return context;
    }

    public ContextsView replace(final ContextItem context) {
        final int index = contexts.indexOf(context);
        if (index >= 0) {
            ContextsView result = new ContextsView(new ArrayList<>(contexts));
            result.contexts.set(index, context);

            return result;
        } else {
            return this;
        }
    }

    public List<ContextItem> all() {
        return Collections.unmodifiableList(contexts);
    }

    @Override
    public String toString() {
        return "ContextsView [contexts=" + contexts + "]";
    }

    public static class ContextItem {
        public final String contextId;
        public final String namespace;

        public static ContextItem of(final String contextId, final String namespace) {
            return new ContextItem(contextId, namespace);
        }

        public static ContextItem only(final String contextId) {
            return new ContextItem(contextId, "");
        }

        public ContextItem(final String contextId, final String namespace) {
            this.contextId = contextId;
            this.namespace = namespace;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
            result = prime * result + ((contextId == null) ? 0 : contextId.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }

            if (other == null || getClass() != other.getClass()) {
                return false;
            }

            return contextId.equals(((ContextItem) other).contextId);
        }

        @Override
        public String toString() {
            return "ContextItem [contextId=" + contextId + ", namespace=" + namespace + "]";
        }
    }
}