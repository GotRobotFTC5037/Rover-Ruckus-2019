//package org.firstinspires.ftc.teamcode.lib.feature
//
//import org.firstinspires.ftc.teamcode.lib.pipeline.PipelineContext
//import org.firstinspires.ftc.teamcode.lib.action.Action
//import org.firstinspires.ftc.teamcode.lib.action.MoveAction
//import org.firstinspires.ftc.teamcode.lib.action.TurnTo
//import org.firstinspires.ftc.teamcode.lib.robot.robot
//
//class TargetHeading(initialTargetHeading: Double) : Feature {
//
//    var targetHeading: Double = initialTargetHeading
//        private set
//
//    private fun interceptor(context: PipelineContext<Action, robot>) {
//        val action = context.subject
//        if (action is MoveAction) {
//            val type = action.context.type
//            if (type is TurnTo) {
//                targetHeading = type.targetHeading
//            }
//        }
//    }
//
//    companion object Installer : FeatureInstaller<Configuration, TargetHeading> {
//        override fun install(robot: robot, configure: Configuration.() -> Unit): TargetHeading {
//            val configuration = Configuration().apply(configure)
//            val targetHeading = TargetHeading(configuration.initialTargetHeading)
//            robot.actionPipeline.intercept {
//                targetHeading.interceptor(this)
//            }
//            return targetHeading
//        }
//    }
//
//    class Configuration : FeatureConfiguration {
//        var initialTargetHeading = 0.0
//    }
//}