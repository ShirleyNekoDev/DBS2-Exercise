package de.hpi.dbs2.exercise2;

import de.hpi.dbs2.exercise2.utils.AssertionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

public class ReadOnlyBPlusTreeTests {
    @Test
    public void testTreesAreValid() {
        TestFixtures.exampleTree.checkValidity();

        AbstractBPlusTree emptyTree1 = new ReadOnlyBPlusTree(4);
        emptyTree1.checkValidity();

        AbstractBPlusTree emptyTree2 = new ReadOnlyBPlusTree(new LeafNode(4));
        emptyTree2.checkValidity();

        AbstractBPlusTree singleEntryTree = new ReadOnlyBPlusTree(
            new LeafNode(4,
                TestFixtures.getOrCreateEntry(0)
            )
        );
        singleEntryTree.checkValidity();

        AbstractBPlusTree dualEntryTree = new ReadOnlyBPlusTree(
            new LeafNode(4,
                TestFixtures.getOrCreateEntry(0),
                TestFixtures.getOrCreateEntry(1)
            )
        );
        dualEntryTree.checkValidity();
    }

    @Test
    public void testGetOrNull() {
        Assertions.assertNull(TestFixtures.exampleTree.getOrNull(0));
        Assertions.assertEquals(TestFixtures.entries.get(5).getValue(), TestFixtures.exampleTree.getOrNull(5));
        Assertions.assertEquals(TestFixtures.entries.get(7).getValue(), TestFixtures.exampleTree.getOrNull(7));
        Assertions.assertEquals(TestFixtures.entries.get(13).getValue(), TestFixtures.exampleTree.getOrNull(13));
        Assertions.assertEquals(TestFixtures.entries.get(19).getValue(), TestFixtures.exampleTree.getOrNull(19));
        Assertions.assertNull(TestFixtures.exampleTree.getOrNull(30));
        Assertions.assertNull(TestFixtures.exampleTree.getOrNull(35));
        Assertions.assertNull(TestFixtures.exampleTree.getOrNull(44));
        Assertions.assertNull(TestFixtures.exampleTree.getOrNull(99));
    }

    @Test
    public void testGetRange() {
        AssertionUtils.assertStreamEquals(
            Stream.empty(),
            TestFixtures.exampleTree.getRange(0, 0)
        );
        AssertionUtils.assertStreamEquals(
            Stream.empty(),
            TestFixtures.exampleTree.getRange(40, 0)
        );
        AssertionUtils.assertStreamEquals(
            Stream.of(TestFixtures.getOrCreateEntry(2).getValue()),
            TestFixtures.exampleTree.getRange(2, 2)
        );
        AssertionUtils.assertStreamEquals(
            Stream.of(
                TestFixtures.getOrCreateEntry(5).getValue(),
                TestFixtures.getOrCreateEntry(7).getValue()
            ),
            TestFixtures.exampleTree.getRange(4, 8)
        );
        AssertionUtils.assertStreamEquals(
            TestFixtures.exampleTree.getEntries().map(AbstractBPlusTree.Entry::getValue),
            TestFixtures.exampleTree.getRange(Integer.MIN_VALUE, Integer.MAX_VALUE)
        );
    }

    @Test
    public void treeHigherOrderValidTests() {
        AbstractBPlusTree emptyTreeOrder5 = new ReadOnlyBPlusTree(5);
        emptyTreeOrder5.checkValidity();

        AbstractBPlusTree filledLeafTreeOrder5 = new ReadOnlyBPlusTree(
            BPlusTreeNode.buildTree(5,
                (Object[]) new AbstractBPlusTree.Entry[]{
                    new AbstractBPlusTree.Entry(1, new ValueReference(0)),
                    new AbstractBPlusTree.Entry(3, new ValueReference(1)),
                    new AbstractBPlusTree.Entry(5, new ValueReference(2))
                }
            )
        );
        filledLeafTreeOrder5.checkValidity();

        AbstractBPlusTree fullLeafTreeOrder5 = new ReadOnlyBPlusTree(
            BPlusTreeNode.buildTree(5,
                (Object[]) new AbstractBPlusTree.Entry[]{
                    new AbstractBPlusTree.Entry(1, new ValueReference(0)),
                    new AbstractBPlusTree.Entry(3, new ValueReference(1)),
                    new AbstractBPlusTree.Entry(5, new ValueReference(2)),
                    new AbstractBPlusTree.Entry(7, new ValueReference(3)),
                }
            )
        );
        fullLeafTreeOrder5.checkValidity();

        AbstractBPlusTree treeOrder5 = new ReadOnlyBPlusTree(
            BPlusTreeNode.buildTree(5,
                (Object[]) new AbstractBPlusTree.Entry[][]{
                    new AbstractBPlusTree.Entry[]{
                        new AbstractBPlusTree.Entry(2, new ValueReference(0)),
                        new AbstractBPlusTree.Entry(3, new ValueReference(1)),
                        new AbstractBPlusTree.Entry(5, new ValueReference(2))
                    },
                    new AbstractBPlusTree.Entry[]{
                        new AbstractBPlusTree.Entry(7, new ValueReference(3)),
                        new AbstractBPlusTree.Entry(11, new ValueReference(4))
                    }
                }
            )
        );
        treeOrder5.checkValidity();

        AbstractBPlusTree emptyTreeOrder6 = new ReadOnlyBPlusTree(6);
        emptyTreeOrder6.checkValidity();

        AbstractBPlusTree filledLeafTreeOrder6 = new ReadOnlyBPlusTree(
            BPlusTreeNode.buildTree(6,
                (Object[]) new AbstractBPlusTree.Entry[]{
                    new AbstractBPlusTree.Entry(1, new ValueReference(0)),
                    new AbstractBPlusTree.Entry(3, new ValueReference(1)),
                    new AbstractBPlusTree.Entry(5, new ValueReference(2))
                }
            )
        );
        filledLeafTreeOrder6.checkValidity();

        AbstractBPlusTree fullLeafTreeOrder6 = new ReadOnlyBPlusTree(
            BPlusTreeNode.buildTree(6,
                (Object[]) new AbstractBPlusTree.Entry[]{
                    new AbstractBPlusTree.Entry(1, new ValueReference(0)),
                    new AbstractBPlusTree.Entry(3, new ValueReference(1)),
                    new AbstractBPlusTree.Entry(5, new ValueReference(2)),
                    new AbstractBPlusTree.Entry(7, new ValueReference(3)),
                    new AbstractBPlusTree.Entry(9, new ValueReference(4)),
                }
            )
        );
        fullLeafTreeOrder6.checkValidity();

        AbstractBPlusTree nestedTreeOrder6 = new ReadOnlyBPlusTree(
            BPlusTreeNode.buildTree(6,
                (Object[]) new AbstractBPlusTree.Entry[][]{
                    new AbstractBPlusTree.Entry[]{
                        new AbstractBPlusTree.Entry(2, new ValueReference(0)),
                        new AbstractBPlusTree.Entry(3, new ValueReference(1)),
                        new AbstractBPlusTree.Entry(5, new ValueReference(2))
                    },
                    new AbstractBPlusTree.Entry[]{
                        new AbstractBPlusTree.Entry(7, new ValueReference(3)),
                        new AbstractBPlusTree.Entry(11, new ValueReference(4)),
                        new AbstractBPlusTree.Entry(15, new ValueReference(5))
                    }
                }
            )
        );
        nestedTreeOrder6.checkValidity();
    }
}
