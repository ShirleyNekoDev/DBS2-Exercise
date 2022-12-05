package de.hpi.dbs2.exercise2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

public class BPlusTreeNodeTests {
    @Test
    public void testTreeBuilder() {
        BPlusTreeNode<?> builtTree = BPlusTreeNode.buildTree(4,
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
        );
        builtTree.checkValidity(true);
        InnerNode expectedTree = new InnerNode(4,
            new LeafNode(4,
                new AbstractBPlusTree.Entry(2, new ValueReference(0)),
                new AbstractBPlusTree.Entry(3, new ValueReference(1)),
                new AbstractBPlusTree.Entry(5, new ValueReference(2))
            ),
            new LeafNode(4,
                new AbstractBPlusTree.Entry(7, new ValueReference(3)),
                new AbstractBPlusTree.Entry(11, new ValueReference(4))
            )
        );
        expectedTree.fixLeafLinks();
        expectedTree.checkValidity(true);
        Assertions.assertEquals(expectedTree, builtTree);
    }

    @Test
    public void testExampleTreeIsValid() {
        TestFixtures.exampleRoot.checkValidity(true);
    }

    @Test
    public void testNodeHeight() {
        Assertions.assertEquals(2, TestFixtures.exampleRoot.getHeight());
        Assertions.assertEquals(1, ((InnerNode) TestFixtures.exampleRoot).getChildNode(0).getHeight());
        Assertions.assertEquals(0, TestFixtures.leaves[0].getHeight());
    }

    @Test
    public void testNodeSize() {
        LeafNode leafNode0 = new LeafNode(4);
        Assertions.assertThrows(IllegalStateException.class, () -> leafNode0.checkValidity(false));
        LeafNode leafNode1 = new LeafNode(4,
            TestFixtures.getOrCreateEntry(0)
        );
        Assertions.assertThrows(IllegalStateException.class, () -> leafNode1.checkValidity(false));
        LeafNode leafNode2 = new LeafNode(4,
            TestFixtures.getOrCreateEntry(0),
            TestFixtures.getOrCreateEntry(1)
        );
        leafNode2.checkValidity(false);
        LeafNode leafNode3 = new LeafNode(4,
            TestFixtures.getOrCreateEntry(0),
            TestFixtures.getOrCreateEntry(1),
            TestFixtures.getOrCreateEntry(2)
        );
        leafNode3.checkValidity(false);
        Assertions.assertThrowsExactly(IllegalArgumentException.class,
            // leaves can only have order-1 references
            () -> new LeafNode(4,
                TestFixtures.getOrCreateEntry(0),
                TestFixtures.getOrCreateEntry(1),
                TestFixtures.getOrCreateEntry(2),
                TestFixtures.getOrCreateEntry(3)
            )
        );

        Assertions.assertEquals(0, leafNode0.getNodeSize());
        Assertions.assertEquals(1, leafNode1.getNodeSize());
        Assertions.assertEquals(2, leafNode2.getNodeSize());

        InnerNode innerNode0 = new InnerNode(4);
        Assertions.assertThrows(IllegalStateException.class, () -> innerNode0.checkValidity(false));
        InnerNode innerNode1 = new InnerNode(4,
            TestFixtures.leaves[0]
        );
        Assertions.assertThrows(IllegalStateException.class, () -> innerNode1.checkValidity(false));
        InnerNode innerNode4 = new InnerNode(4,
            TestFixtures.leaves[0],
            TestFixtures.leaves[1],
            TestFixtures.leaves[2],
            TestFixtures.leaves[3]
        );
        innerNode4.checkValidity(false);

        Assertions.assertEquals(0, innerNode0.getNodeSize());
        Assertions.assertEquals(1, innerNode1.getNodeSize());
        Assertions.assertEquals(4, innerNode4.getNodeSize());

        Assertions.assertTrue(leafNode0.isEmpty());
        Assertions.assertTrue(innerNode0.isEmpty());

        Assertions.assertTrue(leafNode3.isFull());
        Assertions.assertTrue(innerNode4.isFull());
    }

    @Test
    public void testGetSmallestKey() {
        LeafNode leafNode0 = new LeafNode(4);
        Assertions.assertThrows(NoSuchElementException.class, leafNode0::getSmallestKey);
        InnerNode innerNode0 = new InnerNode(4);
        Assertions.assertThrows(NoSuchElementException.class, innerNode0::getSmallestKey);

        LeafNode leafNode3 = TestFixtures.leaves[0];
        Assertions.assertEquals(2, leafNode3.getSmallestKey());
        Assertions.assertEquals(2, TestFixtures.exampleRoot.getSmallestKey());
    }

    @Test
    public void testGetLargestKey() {
        LeafNode leafNode0 = new LeafNode(4);
        Assertions.assertThrows(NoSuchElementException.class, leafNode0::getLargestKey);
        InnerNode innerNode0 = new InnerNode(4);
        Assertions.assertThrows(NoSuchElementException.class, innerNode0::getLargestKey);

        LeafNode leafNode3 = TestFixtures.leaves[0];
        Assertions.assertEquals(5, leafNode3.getLargestKey());
        Assertions.assertEquals(47, TestFixtures.exampleRoot.getLargestKey());
    }

    @Test
    public void testFind() {
        Assertions.assertEquals(TestFixtures.leaves[0], TestFixtures.exampleRoot.findLeaf(0));
        Assertions.assertEquals(TestFixtures.leaves[0], TestFixtures.exampleRoot.findLeaf(5));
        Assertions.assertEquals(TestFixtures.leaves[1], TestFixtures.exampleRoot.findLeaf(7));
        Assertions.assertEquals(TestFixtures.leaves[2], TestFixtures.exampleRoot.findLeaf(13));
        Assertions.assertEquals(TestFixtures.leaves[2], TestFixtures.exampleRoot.findLeaf(19));
        Assertions.assertEquals(TestFixtures.leaves[3], TestFixtures.exampleRoot.findLeaf(30));
        Assertions.assertEquals(TestFixtures.leaves[4], TestFixtures.exampleRoot.findLeaf(35));
        Assertions.assertEquals(TestFixtures.leaves[5], TestFixtures.exampleRoot.findLeaf(44));
        Assertions.assertEquals(TestFixtures.leaves[5], TestFixtures.exampleRoot.findLeaf(99));

        InnerNode node = new InnerNode(4,
            TestFixtures.leaves[0],
            TestFixtures.leaves[1],
            TestFixtures.leaves[2]
        );
        Assertions.assertEquals(node.getChildNode(0), node.findLeaf(Integer.MIN_VALUE));
        Assertions.assertEquals(node.getChildNode(2), node.findLeaf(Integer.MAX_VALUE));
    }

    @Test
    public void testGetOrNull() {
        Assertions.assertNull(TestFixtures.exampleRoot.getOrNull(0));
        Assertions.assertEquals(TestFixtures.entries.get(5).getValue(), TestFixtures.exampleRoot.getOrNull(5));
        Assertions.assertEquals(TestFixtures.entries.get(7).getValue(), TestFixtures.exampleRoot.getOrNull(7));
        Assertions.assertEquals(TestFixtures.entries.get(13).getValue(), TestFixtures.exampleRoot.getOrNull(13));
        Assertions.assertEquals(TestFixtures.entries.get(19).getValue(), TestFixtures.exampleRoot.getOrNull(19));
        Assertions.assertNull(TestFixtures.exampleRoot.getOrNull(30));
        Assertions.assertNull(TestFixtures.exampleRoot.getOrNull(35));
        Assertions.assertNull(TestFixtures.exampleRoot.getOrNull(44));
        Assertions.assertNull(TestFixtures.exampleRoot.getOrNull(99));
    }
}
