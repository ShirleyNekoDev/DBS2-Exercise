package de.hpi.dbs2.exercise2;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Streams;
import de.hpi.dbs2.exercise2.utils.StreamUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public non-sealed class InnerNode extends BPlusTreeNode<BPlusTreeNode<?>> {
    public InnerNode(int order, BPlusTreeNode<?>... nodes) {
        super(order);
        references = new BPlusTreeNode[order];
        // we can't insert more nodes than the given order
        Preconditions.checkArgument(nodes.length <= order, "Inner node size exceeded");
        for (int i = 0; i < nodes.length; i++) {
            references[i] = nodes[i];
            if (i > 0) {
                keys[i - 1] = nodes[i].getSmallestKey();
            }
        }
    }

    /**
     * Fixes leaf references after manually creating InnerNodes.
     * You are _NOT_ allowed to use this method in your exercise implementation.
     */
    public void fixLeafLinks() {
        List<LeafNode> leafs = getDepthFirstNodeStream()
            .filter(node -> node instanceof LeafNode)
            .map(leafNode -> (LeafNode) leafNode)
            .toList();
        Streams.forEachPair(
            leafs.stream(),
            leafs.stream().skip(1),
            (first, second) -> {
                first.nextSibling = second;
            }
        );
        leafs.get(leafs.size() - 1).nextSibling = null;
    }

    @Override
    public int getHeight() {
        // this reference requires to exist, because otherwise this InnerNode would be empty
        return references[0].getHeight() + 1;
    }

    @NotNull
    @Override
    public Integer getSmallestKey() {
        if (isEmpty()) throw new NoSuchElementException();
        BPlusTreeNode<?> leftNode = references[0];
        return leftNode.getSmallestKey();
    }

    @NotNull
    @Override
    public Integer getLargestKey() {
        if (isEmpty()) throw new NoSuchElementException();
        BPlusTreeNode<?> rightNode = StreamUtils.reverseStream(references)
            .dropWhile(Objects::isNull)
            .findFirst().get();
        return rightNode.getLargestKey();
    }

    /**
     * Returns the child-node in which the given searchKey could be located.
     */
    @NotNull
    public BPlusTreeNode<?> selectChild(@NotNull Integer searchKey) {
        for (int i = 0; i < keys.length; i++) {
            Integer key = keys[i];
            if (key != null) {
                if (searchKey < key) {
                    return references[i]; // "left" reference
                }
            } else {
                return references[i]; // "right" reference
            }
        }
        return references[order - 1]; // "rightest" reference
    }

    @NotNull
    public LeafNode findLeaf(@NotNull Integer searchKey) {
        return selectChild(searchKey).findLeaf(searchKey);
    }

    /**
     * @return the n-th child node of this node
     */
    public BPlusTreeNode<?> getChildNode(int i) {
        return references[i];
    }

    @Nullable
    @Override
    public ValueReference getOrNull(@NotNull Integer searchKey) {
        return findLeaf(searchKey).getOrNull(searchKey);
    }

    @Override
    public void checkValidity(boolean isRoot) {
        Preconditions.checkState(keys.length == n, "Inner node has invalid keys size");
        Preconditions.checkState(references.length == order, "Inner node has invalid references size");

        // check node size in bounds
        int size = getNodeSize();
        Preconditions.checkState(!isEmpty(), "Inner node is empty");
        if (isRoot) {
            Preconditions.checkState(
                size >= 2,
                "Root node is underfilled (size=%s)", size
            );
        } else {
            Preconditions.checkState(
                size >= (int) Math.ceil(order / 2.0),
                "Inner node is underfilled (size=%s)", size
            );
        }

        // check that there are not any superfluous (leftover) keys
        Preconditions.checkState(
            Arrays.stream(keys).filter(Objects::nonNull).count() == size - 1,
            "Inner node contains superfluous keys"
        );

        int height = getHeight();
        for (int i = 0; i < n && keys[i] != null; i++) {
            // check that each key has a left and right reference
            if (i == 0)
                Preconditions.checkState(
                    references[i] != null,
                    "Inner node key[0] is missing left subtree"
                );
            Preconditions.checkState(
                references[i + 1] != null,
                "Inner node keys[%s] is missing right subtree", i + 1
            );

            // check that key is correct
            Preconditions.checkState(
                references[i + 1].getSmallestKey().equals(keys[i]),
                "Inner node keys[%s] is does not match leaf key", i
            );
        }

        // check all references for validity
        for (int i = 0; i < order && references[i] != null; i++) {
            BPlusTreeNode<?> node = references[i];
            Preconditions.checkState(
                node.getHeight() == height - 1,
                "Inner node child[%s] has wrong height", i
            );
            Preconditions.checkState(
                node.order == order,
                "Inner node child[%s] has wrong order", i
            );
            node.checkValidity(false);
        }

        if (isRoot) {
            // validate leaf node references
            List<LeafNode> leafsByTreeTraversal = getDepthFirstNodeStream()
                .filter(node -> node instanceof LeafNode)
                .map(leaf -> (LeafNode) leaf)
                .toList();
            List<LeafNode> leafsBySiblingReference = Stream.iterate(
                findLeaf(getSmallestKey()),
                Objects::nonNull,
                leaf -> leaf.nextSibling
            ).toList();
            Preconditions.checkState(
                leafsByTreeTraversal.equals(leafsBySiblingReference),
                "Tree leaf sibling references are incorrect (traversal mismatch)"
            );
            Preconditions.checkState(
                findLeaf(getLargestKey()).nextSibling == null,
                "Tree leaf sibling references are incorrect (rightmost is not null)"
            );
        }
    }

    @Override
    public void stringifyTree(StringBuilder builder, int depth, boolean hideEmptyReferences) {
        String indentation = Strings.repeat("\t", depth);
        String keysString = Arrays.stream(keys)
            .map((Integer key) -> (key == null) ? "-" : key.toString())
            .collect(Collectors.joining(",", "[", "]"));
        builder.append(indentation).append("Node").append(keysString).append(" {\n");
        for (int i = 0; i < order; i++) {
            BPlusTreeNode<?> reference = references[i];
            if (reference == null) {
                if (hideEmptyReferences) continue;
                builder.append(indentation).append("\t").append("-");
            } else {
                reference.stringifyTree(builder, depth + 1, hideEmptyReferences);
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
    public Stream<BPlusTreeNode<?>> getDepthFirstNodeStream() {
        return Stream.concat(
            Arrays.stream(references)
                .takeWhile(Objects::nonNull)
                .flatMap(BPlusTreeNode::getDepthFirstNodeStream),
            Stream.of(this)
        );
    }

    @Override
    public Stream<AbstractBPlusTree.Entry> getEntries() {
        return Arrays.stream(references)
            .takeWhile(Objects::nonNull)
            .flatMap(BPlusTreeNode::getEntries);
    }
}
