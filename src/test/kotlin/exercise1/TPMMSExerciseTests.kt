package exercise1

import de.hpi.dbs2.dbms.*
import de.hpi.dbs2.dbms.utils.RelationUtils
import de.hpi.dbs2.dbms.utils.RelationUtils.fill
import de.hpi.dbs2.dbms.utils.RelationUtils.loadCSV
import de.hpi.dbs2.exercise1.SortOperation
import de.hpi.dbs2.exerciseframework.getChosenImplementation
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TPMMSExerciseTests {
    private fun getImplementation(blockManager: BlockManager, sortColumnIndex: Int): SortOperation =
        getChosenImplementation(
            TPMMSJava(blockManager, sortColumnIndex),
            TPMMSKotlin(blockManager, sortColumnIndex)
        )

    @Test
    fun `TPMMS sorts test file by column 0`() {
        val columnDefinition = ColumnDefinition(
            ColumnDefinition.ColumnType.INTEGER,
            ColumnDefinition.ColumnType.STRING,
            ColumnDefinition.ColumnType.DOUBLE,
        )

        with(
            DBMS(
                totalBlocks = 3,
                blockCapacity = 2
            )
        ) {
            val inputRelation = createRelation(
                blockManager, columnDefinition,
            ).apply {
                loadCSV(
                    blockManager,
                    TPMMSExerciseTests::class.java.getResourceAsStream("input.csv")!!
                )
            }
            val outputRelation = createRelation(
                blockManager, columnDefinition
            )

            val cost = trackIOCost {
                val sortOperation = getImplementation(blockManager, 0)

                assert(blockManager.usedBlocks == 0)
                sortOperation.execute(inputRelation, outputRelation)
                assert(blockManager.usedBlocks == 0)
            }

            val controlRelation = createRelation(
                blockManager, columnDefinition
            ).apply {
                loadCSV(
                    blockManager,
                    TPMMSExerciseTests::class.java.getResourceAsStream("sorted_by_col0.output.csv")!!
                )
            }
            assertEquals(controlRelation.joinToString(), outputRelation.joinToString())

            assertEquals(3 * 6, cost.ioCost)
        }
    }

    @Test
    fun `TPMMS sorts test file by column 2`() {
        val columnDefinition = ColumnDefinition(
            ColumnDefinition.ColumnType.INTEGER,
            ColumnDefinition.ColumnType.STRING,
            ColumnDefinition.ColumnType.DOUBLE,
        )

        with(
            DBMS(
                totalBlocks = 3,
                blockCapacity = 2
            )
        ) {
            val inputRelation = createRelation(
                blockManager, columnDefinition
            ).apply {
                loadCSV(
                    blockManager,
                    TPMMSExerciseTests::class.java.getResourceAsStream("input.csv")!!
                )
            }
            val outputRelation = createRelation(
                blockManager, columnDefinition
            )

            val cost = trackIOCost {
                val sortOperation = getImplementation(blockManager, 2)

                assert(blockManager.usedBlocks == 0)
                sortOperation.execute(inputRelation, outputRelation)
                assert(blockManager.usedBlocks == 0)
            }

            val controlRelation = createRelation(
                blockManager, columnDefinition
            ).apply {
                loadCSV(
                    blockManager,
                    TPMMSExerciseTests::class.java.getResourceAsStream("sorted_by_col2.output.csv")!!
                )
            }
            assertTrue(
                RelationUtils.equalContent(blockManager, controlRelation, outputRelation),
                "tuple values deviate"
            )

            assertEquals(3 * 6, cost.ioCost)
        }
    }

    @Test
    fun `TPMMS sorts random data correctly`() {
        val columnDefinition = ColumnDefinition(
            ColumnDefinition.ColumnType.DOUBLE,
        )

        with(
            DBMS(
                totalBlocks = 25,
                blockCapacity = 10
            )
        ) {
            val inputRelation = createRelation(blockManager, columnDefinition)
            val outputRelation = createRelation(blockManager, columnDefinition)
            val controlRelation = createRelation(blockManager, columnDefinition)
            val comparator = columnDefinition.getColumnComparator(0)

            val random = Random(0)
            val count = 5858
            val column = List(count) {
                columnDefinition.createTuple()
                    .apply { set(0, random.nextDouble()) }
            }
            inputRelation.fill(blockManager) { filler ->
                column.forEach { filler.add(it) }
            }
            controlRelation.fill(blockManager) { filler ->
                column.sortedWith(comparator).forEach { filler.add(it) }
            }

            fun Relation.countTuples(): Int = iterator().asSequence().sumOf { block ->
                blockManager.load(block).use {
                    block.size
                }
            }

            assertEquals(count, inputRelation.countTuples())

            val sortOperation = getImplementation(blockManager, 0)

            val estimateIOCostCost = trackIOCost {
                assertEquals(0, blockManager.usedBlocks)
                val estimatedCost = sortOperation.estimatedIOCost(inputRelation)
                assertEquals(0, blockManager.usedBlocks)
                assert(estimatedCost == inputRelation.estimatedBlockCount()*3 || estimatedCost == inputRelation.estimatedBlockCount()*4)
            }
            assertEquals(0, estimateIOCostCost.ioCost)

            val sortCost = trackIOCost {
                assertEquals(0, blockManager.usedBlocks)
                sortOperation.execute(inputRelation, outputRelation)
                assertEquals(0, blockManager.usedBlocks)
            }

            assertEquals(
                controlRelation.iterator().asSequence().count(),
                outputRelation.iterator().asSequence().count(),
                "block count deviates"
            )

            assertEquals(count, outputRelation.countTuples())

            assertTrue(
                RelationUtils.equalContent(blockManager, controlRelation, outputRelation),
                "tuple values deviate"
            )

            assertEquals(2 * inputRelation.estimatedBlockCount(), sortCost.inputCost)
            assertEquals(1 * inputRelation.estimatedBlockCount(), sortCost.outputCost)
        }
    }

    @Test
    fun `TPMMS throws error when relation is too large to sort`() {
        val columnDefinition = ColumnDefinition(
            ColumnDefinition.ColumnType.INTEGER,
        )

        with(
            DBMS(
                totalBlocks = 3,
                blockCapacity = 2
            )
        ) {
            val inputRelation = object : Relation {
                val blocks = Array(13) {
                    blockManager.allocate(false)
                }

                override val columns: ColumnDefinition = columnDefinition
                override fun estimatedBlockCount(): Int = blocks.size

                override fun clear() = TODO()
                override fun iterator(): Iterator<Block> = blocks.iterator()
                override fun getBlockOutput(): BlockOutput = TODO()
            }
            val outputRelation = createRelation(
                blockManager, columnDefinition
            )

            val sortOperation = getImplementation(blockManager, 0)

            assertFailsWith<Operation.RelationSizeExceedsCapacityException> {
                sortOperation.execute(inputRelation, outputRelation)
            }
        }
    }
}
