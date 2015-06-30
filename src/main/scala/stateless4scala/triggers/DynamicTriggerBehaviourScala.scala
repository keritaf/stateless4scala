package stateless4scala.triggers

import scala.reflect.runtime.universe._

import stateless4scala.OutVar

class DynamicTriggerBehaviour[S, T](override val trigger: T, private val destination: (Seq[(Type, Any)]) => S, guard: => Boolean)
    extends TriggerBehaviour[S, T](trigger, guard) {
  require(destination != null, "destination is null")

  override def resultsInTransitionFrom(source: S, args: Seq[(Type, Any)], dest: OutVar[S]): Boolean = {
    dest.value = destination(args)
    true
  }
}
