package de.hpi.dbs2

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.options.Option
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault
abstract class ExerciseSpecificTask : DefaultTask() {
    companion object {
        val REGISTERED_EXERCISES = listOf(0, 1)
    }

    @get:Input
    abstract val exerciseId: Property<Int>

    @Option(option = "exerciseId", description = "Exercise ID")
    fun setExerciseIdOption(value: String) {
        exerciseId.set(value.toInt())
    }

    @get:Internal
    val _exerciseId: Int
        get() = (exerciseId.orNull ?: error("Please provide the exerciseId via --exerciseId=x"))
            .also { require(it in REGISTERED_EXERCISES) { "Unknown exerciseId (available=$REGISTERED_EXERCISES)" } }
}
