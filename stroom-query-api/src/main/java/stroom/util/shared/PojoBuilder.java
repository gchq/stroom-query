package stroom.util.shared;

import java.util.function.Consumer;

public abstract class PojoBuilder<ParentBuilder extends PojoBuilder, Pojo, BuilderClass extends PojoBuilder<?, Pojo, ?>> {
    private ParentBuilder parentBuilder;
    private Consumer<Pojo> childComplete;
    private PojoBuilder pendingChild;

    public final Pojo build() {
        this.completeAnyPendingChildren();
        return this.pojoBuild();
    }

    protected abstract Pojo pojoBuild();

    void setPendingChild(final PojoBuilder builder) {
        this.pendingChild = builder;
    }

    void removePendingChild() {
        this.pendingChild = null;
    }

    protected void completeAnyPendingChildren() {
        if (this.pendingChild != null) {
            this.pendingChild.end();
            this.pendingChild = null;
        }
    }

    public BuilderClass parent(final ParentBuilder parentBuilder,
                               final Consumer<Pojo> theChildComplete) {
        this.parentBuilder = parentBuilder;
        this.parentBuilder.completeAnyPendingChildren();
        this.parentBuilder.setPendingChild(this);

        this.childComplete = (pojo) -> {
            this.parentBuilder.removePendingChild();
            theChildComplete.accept(pojo);
        };

        return self();
    }

    /**
     * If this builder instance was created as a child of another builder instance then this method returns the
     * parent builder instance so you can continue to add items to the parent. See the example in
     * {@link ParentBuilder}
     * @return The parent builder instance if there is one, else returns this builder instance
     */
    public ParentBuilder end() {
        final Pojo childPojo = this.build();

        if (this.parentBuilder != null) {
            if (this.childComplete != null) {
                this.childComplete.accept(childPojo);
            }

            return parentBuilder;
        }

        return null;
    }

    public abstract BuilderClass self();
}
