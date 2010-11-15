package se.vgregion.push.types;

public interface EntryFilter {

    boolean include(Entry entry);
}
