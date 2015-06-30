package stateless4scala

import scala.collection.immutable
import scala.collection.mutable
import scala.reflect.runtime.universe._

import stateless4scala.triggers.TriggerBehaviour

class StateRepresentation[S, T](private val state: S) {

  val triggerBehaviours = mutable.Map[T, mutable.Buffer[TriggerBehaviour[S, T]]]()
  private val entryActions = mutable.Buffer[(Transition[S, T], Seq[(Type, Any)]) => Unit]()
  private val exitActions = mutable.ListBuffer[Transition[S, T] => Unit]()
  private val substates = mutable.ListBuffer[StateRepresentation[S, T]]()

  var superstate: Option[StateRepresentation[S, T]] = None

  def canHandle(trigger: T) = tryFindHandler(trigger).isDefined

  def tryFindHandler(trigger: T): Option[TriggerBehaviour[S, T]] =
    tryFindLocalHandler(trigger) orElse superstate.flatMap(_.tryFindHandler(trigger))

  def tryFindLocalHandler(trigger: T): Option[TriggerBehaviour[S, T]] = {
    triggerBehaviours
      .get(trigger)
      .map({ value =>
        val actual = mutable.Set[TriggerBehaviour[S, T]]()
        actual ++= value.filter(_.isGuardConditionMet)
        if (actual.size > 1)
          throw new IllegalStateException(s"Multiple permitted exit transitions are configured from state '$state' for trigger '$trigger'. Guard clauses must be mutually exclusive.")
        return actual.headOption
      })
  }

  def addEntryAction(trigger: T, action: (Transition[S, T], Seq[(Type, Any)]) => Unit): Unit = {
    require(action != null, "action is null")
    entryActions += ((t: Transition[S, T], args: Seq[(Type, Any)]) => {
      if (t.trigger == trigger) {
        action(t, args)
      }
    })
  }

  def addEntryAction(action: (Transition[S, T], Seq[(Type, Any)]) => Unit): Unit = {
    require(action != null, "action is null")
    entryActions += action
  }

  def insertEntryAction(action: (Transition[S, T], Seq[(Type, Any)]) => Unit): Unit = {
    require(action != null, "action is null")
    entryActions.prepend(action)
  }

  def addExitAction(action: Transition[S, T] => Unit): Unit = {
    require(action != null, "action is null")
    exitActions += action
  }

  def enter(transition: Transition[S, T], entryArgs: (Type, Any)*): Unit = {
    require(transition != null, "transition is null")
    if (transition.isReentry) {
      executeEntryActions(transition, entryArgs: _*)
    } else if (!includes(transition.source)) {
      superstate.foreach(_.enter(transition, entryArgs: _*))
      executeEntryActions(transition, entryArgs: _*)
    }
  }

  def exit(transition: Transition[S, T]): Unit = {
    require(transition != null, "transition is null")
    if (transition.isReentry) {
      executeExitActions(transition)
    } else if (!includes(transition.destination)) {
      executeExitActions(transition)
      superstate.foreach(_.exit(transition))
    }
  }

  def executeEntryActions(transition: Transition[S, T], entryArgs: (Type, Any)*): Unit = {
    require(transition != null, "transition is null")
    require(entryArgs != null, "entryArgs is null")
    for (action <- entryActions) {
      action(transition, entryArgs)
    }
  }

  def executeExitActions(transition: Transition[S, T]): Unit = {
    require(transition != null, "transition is null")
    for (action <- exitActions) {
      action(transition)
    }
  }

  def addTriggerBehaviour(triggerBehaviour: TriggerBehaviour[S, T]): Unit = {
    (triggerBehaviours.getOrElseUpdate(triggerBehaviour.trigger, mutable.ListBuffer[TriggerBehaviour[S, T]]())) += triggerBehaviour
  }

  def getUnderlyingState(): S = state

  def addSubstate(substate: StateRepresentation[S, T]): Unit = {
    require(substate != null, "substate is null")
    substates += substate
  }

  def includes(stateToCheck: S): Boolean = {
    substates.find(_.includes(stateToCheck)).map(_ => true)
      .getOrElse(this.state == stateToCheck)
  }

  def isIncludedIn(stateToCheck: S): Boolean = {
    this.state == stateToCheck || superstate.fold(false)(_.isIncludedIn(stateToCheck))
  }

  def permittedTriggers: immutable.Set[T] = {
    val result = mutable.Set[T]()
    triggerBehaviours.filter {
      case (_, behaviours) => behaviours.find(_.isGuardConditionMet).isDefined
    } map { case (key, _) => key } forall result.add

    superstate.foreach(result ++= _.permittedTriggers)

    result.toSet
  }
}
