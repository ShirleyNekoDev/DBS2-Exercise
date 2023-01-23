package de.hpi.dbs2.exercise2;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import de.hpi.dbs2.exercise2.utils.StreamUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public non-sealed class LeafNode extends BPlusTreeNode<ValueReference> {
    public LeafNode nextSibling;

    public LeafNode(int order, AbstractBPlusTree.Entry... entries) {
        super(order);
        references = new ValueReference[order - 1];
        // we can't insert more entries than the given order-1
        Preconditions.checkArgument(entries.length < order, "Leaf node size exceeded");
        for (int i = 0; i < entries.length; i++) {
            AbstractBPlusTree.Entry entry = entries[i];
            keys[i] = entry.getKey();
            references[i] = entry.getValue();
        }
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @NotNull
    @Override
    public Integer getSmallestKey() {
        if (isEmpty()) throw new NoSuchElementException();
        return keys[0];
    }

    @NotNull
    @Override
    public Integer getLargestKey() {
        if (isEmpty()) throw new NoSuchElementException();
        return StreamUtils.reverseStream(keys)
            .dropWhile(Objects::isNull)
            .findFirst().get();
    }

    @NotNull
    @Override
    public LeafNode findLeaf(@NotNull Integer searchKey) {
        return this;
    }

    @Nullable
    public ValueReference getOrNull(@NotNull Integer searchKey) {
        for (int i = 0; i < keys.length; i++) {
            if (searchKey.equals(keys[i])) {
                return references[i];
            }
        }
        return null;
    }

    @Override
    public void stringifyTree(StringBuilder builder, int depth, boolean hideEmptyReferences) {
        String indentation = Strings.repeat("\t", depth);
        builder.append(indentation).append("Leaf {\n");
        for (int i = 0; i < order - 1; i++) {
            Integer key = keys[i];
            ValueReference reference = references[i];
            if (key == null) {
                if (hideEmptyReferences) continue;
                builder.append(indentation).append("\t").append("-");
            } else {
                builder.append(indentation).append("\t")
                    .append("[").append(key).append("] -> ").append(reference);
            }
            if (i < references.length - 1) {
                builder.append(",\n");
            } else {
                builder.append("\n");
            }
        }
        builder.append(indentation).append("}");
    }

    @Override
    public void checkValidity(boolean isRoot) {
        Preconditions.checkState(keys.length == n, "Leaf node has invalid keys size");
        Preconditions.checkState(references.length == n, "Leaf node has invalid references size");

        // check node size in bounds
        int size = getNodeSize();
        if (!isRoot) {
            Preconditions.checkState(!isEmpty(), "Leaf node is empty");
            Preconditions.checkState(
                size >= (int) Math.floor(order / 2.0),
                "Leaf node is underfilled (size=%s)", size
            );
        }

        // check that there are not any superfluous (leftover) keys
        Preconditions.checkState(
            Arrays.stream(keys).filter(Objects::nonNull).count() == size,
            "Leaf node contains superfluous keys"
        );

        for (int i = 0; i < n && keys[i] != null; i++) {
            // check that each key has a referenced value
            Preconditions.checkState(
                references[i] != null,
                "Leaf node keys[%s] is missing a value", i
            );
        }
    }

    @Override
    public Stream<BPlusTreeNode<?>> getDepthFirstNodeStream() {
        return Stream.of(this);
    }

    @Override
    public Stream<AbstractBPlusTree.Entry> getEntries() {
        return IntStream.range(0, getNodeSize())
            .mapToObj(i -> new AbstractBPlusTree.Entry(keys[i], references[i]));
    }
}
