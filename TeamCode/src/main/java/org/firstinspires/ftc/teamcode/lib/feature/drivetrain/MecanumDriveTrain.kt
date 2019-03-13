package org.firstinspires.ftc.teamcode.lib.feature.drivetrain

import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.control.PIDCoefficients
import com.acmerobotics.roadrunner.drive.MecanumDrive
import com.acmerobotics.roadrunner.followers.MecanumPIDVAFollower
import com.acmerobotics.roadrunner.trajectory.Trajectory
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.TickerMode
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.selects.select
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureSet
import org.firstinspires.ftc.teamcode.lib.feature.KeyedFeatureInstaller
import org.firstinspires.ftc.teamcode.lib.feature.localizer.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.pipeline.Pipeline
import org.firstinspires.ftc.teamcode.lib.robot.RobotFeatureInstallContext
import org.firstinspires.ftc.teamcode.lib.util.get
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.math.PI

class MecanumDriveTrain(
    private val frontLeftMotor: DcMotorEx,
    private val frontRightMotor: DcMotorEx,
    private val backLeftMotor: DcMotorEx,
    private val backRightMotor: DcMotorEx,
    private val parentContext: CoroutineContext = EmptyCoroutineContext
) : Feature(), OmnidirectionalDriveTrain, InterceptableDriveTrain<MecanumDriveTrain.MotorPowers>,
    CoroutineScope {

    private val job: Job = Job(parentContext[Job])

    override val coroutineContext: CoroutineContext
        get() = parentContext + CoroutineName("Mecanum Drive Train") + job

    private val powerChannel: Channel<MotorPowers> = Channel(Channel.CONFLATED)

    override val powerPipeline = Pipeline<MotorPowers, DriveTrain>()

    data class MotorPowers(
        var frontLeftPower: Double = 0.0,
        var frontRightPower: Double = 0.0,
        var backLeftPower: Double = 0.0,
        var backRightPower: Double = 0.0
    ) : DriveTrainMotorPowers {
        override fun adjustHeadingPower(power: Double) {
            frontLeftPower -= power
            frontRightPower += power
            backLeftPower -= power
            backRightPower += power
        }
    }

    override suspend fun setLinearPower(power: Double) {
        powerChannel.offer(MotorPowers(power, power, power, power))
    }

    override suspend fun setLateralPower(power: Double) {
        powerChannel.offer(MotorPowers(power, -power, -power, power))
    }

    override suspend fun setRotationalPower(power: Double) {
        powerChannel.offer(MotorPowers(-power, power, -power, power))
    }

    override suspend fun setDirectionPower(
        linearPower: Double,
        lateralPower: Double,
        rotationalPower: Double
    ) {
        powerChannel.offer(
            MotorPowers(
                frontLeftPower = linearPower + lateralPower + rotationalPower,
                frontRightPower = linearPower - lateralPower - rotationalPower,
                backLeftPower = linearPower - lateralPower + rotationalPower,
                backRightPower = linearPower + lateralPower - rotationalPower
            )
        )
    }

    private fun CoroutineScope.startUpdatingPowers(ticker: ReceiveChannel<Unit>) = launch {
        var targetPowers = MotorPowers()
        while (isActive) {
            select<Unit> {
                powerChannel.onReceive {
                    targetPowers = it
                }
                ticker.onReceive {
                    val (fl, fr, bl, br) = powerPipeline.execute(
                        targetPowers, this@MecanumDriveTrain
                    )
                    frontLeftMotor.power = fl
                    frontRightMotor.power = fr
                    backLeftMotor.power = bl
                    backRightMotor.power = br
                }
            }
        }
    }

    companion object Installer : KeyedFeatureInstaller<MecanumDriveTrain, Configuration>() {

        override val name: String = "Mecanum Drive Train"

        override suspend fun install(
            context: RobotFeatureInstallContext,
            featureSet: FeatureSet,
            configure: Configuration.() -> Unit
        ): MecanumDriveTrain {
            val (fl, fr, bl, br) = Configuration().apply(configure)
            val hm = context.hardwareMap
            val frontLeft = hm[DcMotorEx::class, fl].apply { setup(); toggleDirection() }
            val frontRight = hm[DcMotorEx::class, fr].apply { setup() }
            val backLeft = hm[DcMotorEx::class, bl].apply { setup(); toggleDirection() }
            val backRight = hm[DcMotorEx::class, br].apply { setup() }
            return MecanumDriveTrain(
                frontLeft, frontRight, backLeft, backRight, coroutineContext
            ).apply {
                @Suppress("EXPERIMENTAL_API_USAGE")
                startUpdatingPowers(ticker(10, 0, this.coroutineContext, TickerMode.FIXED_DELAY))
            }
        }

        private fun DcMotor.setup() {
            this.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            this.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        }

        private fun DcMotor.toggleDirection() {
            direction = if (direction == DcMotorSimple.Direction.FORWARD) {
                DcMotorSimple.Direction.REVERSE
            } else {
                DcMotorSimple.Direction.FORWARD
            }
        }

    }

    class Configuration : FeatureConfiguration {
        var frontLeftMotorName: String = "front left motor"
        var frontRightMotorName: String = "front right motor"
        var backLeftMotorName: String = "back left motor"
        var backRightMotorName: String = "back right motor"
        operator fun component1() = frontLeftMotorName
        operator fun component2() = frontRightMotorName
        operator fun component3() = backLeftMotorName
        operator fun component4() = backRightMotorName
    }

    /* Localizer */

    inner class LocalizerFeature(
        private val wheelDiameter: Double,
        private val gearRatio: Double
    ) : Feature() {

        private val positionChannel = Channel<MotorPositions>(Channel.CONFLATED)

        private val ticksPerRevolution: Double by lazy {
            frontLeftMotor.motorType.ticksPerRev
        }

        private val unitConversionMultiplier: Double by lazy {
            gearRatio * wheelDiameter * PI / ticksPerRevolution
        }

        private fun CoroutineScope.startUpdatingPositions(ticker: ReceiveChannel<Unit>) = launch {
            while (isActive) {
                ticker.receive()
                val positions = MotorPositions(
                    frontLeftMotor.currentUnitPosition,
                    frontRightMotor.currentUnitPosition,
                    backLeftMotor.currentUnitPosition,
                    backRightMotor.currentUnitPosition
                )
                positionChannel.send(positions)
            }
        }

        fun startPositionUpdates(ticker: ReceiveChannel<Unit>) {
            startUpdatingPositions(ticker)
        }

        private val DcMotor.currentUnitPosition: Double
            get() = currentPosition * unitConversionMultiplier

        suspend fun motorPositions(): MotorPositions =
            positionChannel.receive()

    }

    object Localizer : KeyedFeatureInstaller<LocalizerFeature, LocalizerConfiguration>() {

        override val name: String = "Mecanum Drive Train Localizer"

        override suspend fun install(
            context: RobotFeatureInstallContext,
            featureSet: FeatureSet,
            configure: LocalizerConfiguration.() -> Unit
        ): LocalizerFeature {
            val driveTrain = featureSet[MecanumDriveTrain]
            val configuration = LocalizerConfiguration().apply(configure)
            return driveTrain.LocalizerFeature(
                wheelDiameter = configuration.wheelDiameter,
                gearRatio = configuration.gearRatio
            ).apply {
                @Suppress("EXPERIMENTAL_API_USAGE")
                startPositionUpdates(ticker(10, 0, coroutineContext, TickerMode.FIXED_DELAY))
            }
        }

    }

    class LocalizerConfiguration : FeatureConfiguration {
        var wheelDiameter: Double = 0.0
        var gearRatio: Double = 0.0
    }

    data class MotorPositions(
        val frontLeftPosition: Double,
        val frontRightPosition: Double,
        val backLeftPosition: Double,
        val backRightPosition: Double
    )

    fun MotorPositions.toRoadRunnerPositionList(): List<Double> = listOf(
        frontLeftPosition, backLeftPosition, frontRightPosition, backRightPosition
    )

    /* Road Runner Extension */

    inner class RoadRunnerFeature(
        private val driveTrainLocalizer: MecanumDriveTrain.LocalizerFeature,
        private val imuLocalizer: IMULocalizer,
        private val config: RoadRunnerConfig
    ) : Feature() {

        private val drive = object : MecanumDrive(config.trackWidth, config.wheelBase) {

            override fun setMotorPowers(
                frontLeft: Double,
                rearLeft: Double,
                rearRight: Double,
                frontRight: Double
            ) {
                powerChannel.offer(MotorPowers(frontLeft, frontRight, rearLeft, rearRight))
            }

            override fun getWheelPositions(): List<Double> = runBlocking {
                driveTrainLocalizer.motorPositions().toRoadRunnerPositionList()
            }


            override fun getExternalHeading(): Double = runBlocking {
                imuLocalizer.heading()
            }

        }

        private val follower = MecanumPIDVAFollower(
            drive = drive,
            translationalCoeffs = config.translationalCoefficients,
            headingCoeffs = config.headingCoefficients,
            kV = config.kV,
            kA = config.kA,
            kStatic = config.kStatic,
            admissibleError = config.admissibleError,
            timeout = config.timeout
        )

        var trajectory: Trajectory
            get() = follower.trajectory
            set(value) = follower.followTrajectory(value)

        val isFollowing: Boolean
            get() = follower.isFollowing()

        fun updateMotorPowers() {
            drive.updatePoseEstimate()
            follower.update(drive.poseEstimate)
        }

    }

    object RoadRunnerExtension : KeyedFeatureInstaller<RoadRunnerFeature, RoadRunnerConfig>() {

        override val name: String = "Mecanum Drive Train Road Runner Extension"

        override suspend fun install(
            context: RobotFeatureInstallContext,
            featureSet: FeatureSet,
            configure: RoadRunnerConfig.() -> Unit
        ): RoadRunnerFeature {
            val driveTrain = featureSet[MecanumDriveTrain]
            val driveTrainLocalizer = featureSet[MecanumDriveTrain.Localizer]
            val imuLocalizer = featureSet[IMULocalizer]
            val configuration = RoadRunnerConfig().apply(configure)
            return driveTrain.RoadRunnerFeature(driveTrainLocalizer, imuLocalizer, configuration)
        }

    }

    class RoadRunnerConfig : FeatureConfiguration {
        var trackWidth: Double = 0.0
        var wheelBase: Double = 0.0
        lateinit var translationalCoefficients: PIDCoefficients
        lateinit var headingCoefficients: PIDCoefficients
        var kV: Double = 0.0
        var kA: Double = 0.0
        var kStatic: Double = 0.0
        lateinit var admissibleError: Pose2d
        var timeout: Double = 0.0
    }


}
