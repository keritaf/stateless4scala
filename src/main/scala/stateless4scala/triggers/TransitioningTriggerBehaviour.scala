package stateless4scala.triggers

import scala.reflect.runtime.universe._

import stateless4scala.OutVar

class TransitioningTriggerBehaviour[S, T](trigger: T, private val destination: S, guard: => Boolean)
    extends TriggerBehaviour[S, T](trigger, guard) {

  override def resultsInTransitionFrom(source: S, args: Seq[(Type, Any)], dest: OutVar[S]): Boolean = {
    dest.value = destination
    true
  }
}
