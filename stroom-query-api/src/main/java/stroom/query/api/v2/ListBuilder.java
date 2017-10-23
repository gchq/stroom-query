package stroom.query.api.v2;

import stroom.util.shared.OwnedBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A general purpose list builder, tied to our OwnedBuilder hierarchy allowing us to embed it in the build chain
 * @param <OwningBuilder> The class of the owning builder
 * @param <ListedPojo> The classes being put into the list under construction
 */
public class ListBuilder<OwningBuilder extends OwnedBuilder, ListedPojo>
        extends OwnedBuilder<OwningBuilder, List<ListedPojo>, ListBuilder<OwningBuilder, ListedPojo>> {
    private final List<ListedPojo> childValues = new ArrayList<>();

    /**
     * Add values to our list
     * @param values The values to add to our list
     * @return This builder, allowing method chaining
     */
    public ListBuilder<OwningBuilder, ListedPojo> value(final ListedPojo...values) {
        this.childValues.addAll(Arrays.asList(values));
        return self();
    }

    @Override
    protected List<ListedPojo> pojoBuild() {
        return childValues;
    }

    @Override
    public ListBuilder<OwningBuilder, ListedPojo> self() {
        return this;
    }
}