package stateless4scala.triggers

import scala.reflect.runtime.universe._

import stateless4scala.OutVar

class IgnoredTriggerBehaviour[S, T](override val trigger: T, guard: => Boolean)
    extends TriggerBehaviour[S, T](trigger, guard) {
  override def resultsInTransitionFrom(source: S, args: Seq[(Type, Any)], dest: OutVar[S]) = false
}
