import sbt.AutoPlugin
import sbt.Keys._

object FixUnresolvedImports extends AutoPlugin {
  override def trigger = allRequirements
  override def projectSettings = Seq(
    updateConfiguration in updateSbtClassifiers := (updateConfiguration in updateSbtClassifiers).value.withMissingOk(true)
  )
}
