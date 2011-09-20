/**
 * Copyright 2010 Västra Götalandsregionen
 *
 *   This library is free software; you can redistribute it and/or modify
 *   it under the terms of version 2.1 of the GNU Lesser General Public
 *   License as published by the Free Software Foundation.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the
 *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *   Boston, MA 02111-1307  USA
 *
 */

package se.vgregion.pubsub.content;

import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.Field;
import se.vgregion.pubsub.Namespaces;
import se.vgregion.pubsub.impl.XmlUtil;

public class AtomSerializer extends AbstractSerializer {

    @Override
    public Document print(Feed feed, EntryFilter entryFilter) {
        Element feedElm = new Element("feed", Namespaces.ATOM);

        List<Field> fields = feed.getFields();

        for(Field field : fields) {
            feedElm.appendChild(XmlUtil.fieldToXml(field));
        }

        for(Entry entry : feed.getEntries()) {
            if(include(entry, entryFilter)) {
                feedElm.appendChild(print(entry));
            }
        }

        return new Document(feedElm);
    }

    private Element print(Entry entry) {
        Element entryElm = new Element("entry", Namespaces.ATOM);

        List<Field> fields = entry.getFields();

        for(Field field : fields) {
            entryElm.appendChild(XmlUtil.fieldToXml(field));
        }

        return entryElm;

    }
}
