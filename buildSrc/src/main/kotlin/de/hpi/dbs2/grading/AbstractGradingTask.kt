package de.hpi.dbs2.grading

import de.hpi.dbs2.ExerciseSpecificTask

abstract class AbstractGradingTask : ExerciseSpecificTask() {
    init {
        group = "grading"
        enabled = project.hasProperty("tutor")
    }
}
