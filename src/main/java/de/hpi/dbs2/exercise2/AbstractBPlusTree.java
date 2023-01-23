package de.hpi.dbs2.exercise2;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * This class lays the foundation for your BPlusTree implementation.
 * For this exercise, we use [Integer] as keys and [ValueReference] as value.
 *
 * <p>We have already implemented some features for you.
 */
public abstract class AbstractBPlusTree implements Index<Integer, ValueReference> {
    public final int order;
    protected BPlusTreeNode<?> rootNode;

    /**
     * An empty B+-Tree starts with a LeafNode as an initial root node.
     * This LeafNode will be replaced with an InnerNode once there are enough entries in the tree.
     */
    public AbstractBPlusTree(int order) {
        this.order = order;
        rootNode = new InitialRootNode(order);
    }

    /**
     * Initializes a B+-Tree using an existing tree structure which must be valid.
     */
    public AbstractBPlusTree(BPlusTreeNode<?> rootNode) {
        this.rootNode = rootNode;
        this.order = rootNode.order;
        checkValidity();
    }

    public int getHeight() {
        return rootNode.getHeight();
    }

    public BPlusTreeNode<?> getRootNode() {
        return rootNode;
    }

    /**
     * Find the value mapped to the given key or null, if the key is not in the index.
     * @return associated value for the given key, if existing
     */
    @Nullable
    @Override
    public ValueReference getOrNull(@NotNull Integer searchKey) {
        return rootNode.getOrNull(searchKey);
    }

    /**
     * Find the value mapped to the given key.
     * @throws NoSuchElementException If the key is not in the index.
     * @return associated value for the given key
     */
    @NotNull
    @Override
    public ValueReference get(@NotNull Integer searchKey) {
        ValueReference value = getOrNull(searchKey);
        if (value == null) {
            throw new NoSuchElementException("Key not found in index: $searchKey");
        }
        return value;
    }

    /**
     * Insert a new key-value mapping into the index, replacing the old value for existing keys.
     * @return previously associated value for the given key, if existing, otherwise null
     */
    @Nullable
    @Override
    public abstract ValueReference insert(@NotNull Integer key, @NotNull ValueReference value);

    /**
     * Remove a key-value mapping from the index.
     * @return previously associated value for the given key, if existing, otherwise null
     */
    @Override
    public ValueReference remove(@NotNull Integer key) {
        throw new UnsupportedOperationException("You don't have to implement this :)");
    }

    /**
     * Finds all values for the keys between the given bounds.
     * @return an iterator over all found values
     *         will be empty if lowerBound > upperBound
     *         will contain only a single value if lowerBound = upperBound and the key maps to a value
     */
    @NotNull
    @Override
    public Stream<ValueReference> getRange(@NotNull Integer lowerBound, @NotNull Integer upperBound) {
        if(lowerBound > upperBound)
            return Stream.empty();
        return Stream
            .iterate(
                rootNode.findLeaf(lowerBound),
                Objects::nonNull,
                leaf -> leaf.nextSibling
            )
            .flatMap(LeafNode::getEntries)
            .dropWhile(entry -> entry.getKey() < lowerBound)
            .takeWhile(entry -> entry.getKey() <= upperBound)
            .map(Entry::getValue);
    }

    /**
     * Insert a new key-value mapping into the index, replacing the old value for existing keys.
     * @return previously associated value for the given key, if existing, otherwise null
     */
    public ValueReference insert(@NotNull Entry entry) {
        return insert(entry.getKey(), entry.getValue());
    }

    /**
     * @return a stream over all entries of this tree
     */
    public Stream<Entry> getEntries() {
        return rootNode.getEntries();
    }

    public static class Entry extends IndexEntry<Integer, ValueReference> {
        public Entry(@NotNull Integer key, @NotNull ValueReference value) {
            super(key, value);
        }
    }

    /**
     * For testing purposes.
     * @throws IllegalStateException if tree is not valid
     */
    public void checkValidity() {
        Preconditions.checkState(rootNode.order == order, "Tree root order mismatch");
        rootNode.checkValidity(true);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BPlusTree = ");
        rootNode.stringifyTree(builder, 0, false);
        return builder.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof AbstractBPlusTree that)) return false;
        return this.rootNode.equals(that.rootNode);
    }
}
