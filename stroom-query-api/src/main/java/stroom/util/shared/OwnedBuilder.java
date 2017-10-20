package stroom.util.shared;

import java.util.function.Consumer;

/**
 * A general purpose builder class that allows heirarchical building.
 * Each builder has an owning builder, and potentially a pending child that is under construction.
 *
 * @param <OwningBuilder> The class of the owning builder, allows the build to pop back up
 * @param <Pojo> The class being constructed by this builder
 * @param <BuilderClass> This builder class, allows for type safe method chaining
 */
public abstract class OwnedBuilder<OwningBuilder extends OwnedBuilder, Pojo, BuilderClass extends OwnedBuilder<?, Pojo, ?>> {
    private OwningBuilder owningBuilder;
    private Consumer<Pojo> onCompletion;
    private OwnedBuilder pendingChild;

    /**
     * Called by users of the builder to complete the build. This function will ensure that any pending child builders
     * are completed. Forcing child completion may be required depending on how the user of the builder is chaining things up.
     * @return The built Pojo
     */
    public final Pojo build() {
        this.completeAnyPendingChildren();
        return this.pojoBuild();
    }

    /**
     * If this builder instance was created as a child of another builder instance then this method returns the
     * popToWhenComplete builder instance so you can continue to add items to the popToWhenComplete.
     * @return The popToWhenComplete builder instance if there is one, else returns this builder instance
     */
    public OwningBuilder end() {
        final Pojo childPojo = this.build();

        if (this.owningBuilder != null) {
            if (this.onCompletion != null) {
                this.onCompletion.accept(childPojo);
            }

            return owningBuilder;
        }

        return null;
    }

    /**
     * When an owning builder creates a child builder, it should call this function to pass itself in as the 'owner'
     * Thereby allowing the construction to pop back up
     * @param owningBuilder A direct reference to the owning builder, this will be returned when the user calls end()
     * @param theChildComplete A function to call when the child is completed, this will normally be a setter on the owning builder.
     * @return The builder itself to allow method chaining
     */
    public BuilderClass popToWhenComplete(final OwningBuilder owningBuilder,
                                          final Consumer<Pojo> theChildComplete) {
        this.owningBuilder = owningBuilder;
        this.owningBuilder.completeAnyPendingChildren();
        this.owningBuilder.setPendingChild(this);

        this.onCompletion = (pojo) -> {
            this.owningBuilder.removePendingChild();
            theChildComplete.accept(pojo);
        };

        return self();
    }

    /**
     * Child classes will be responsible for actually building their respective Pojos
     * @return The built pojo
     */
    protected abstract Pojo pojoBuild();

    void setPendingChild(final OwnedBuilder builder) {
        this.pendingChild = builder;
    }

    void removePendingChild() {
        this.pendingChild = null;
    }

    void completeAnyPendingChildren() {
        if (this.pendingChild != null) {
            this.pendingChild.end();
            this.pendingChild = null;
        }
    }

    public abstract BuilderClass self();
}
