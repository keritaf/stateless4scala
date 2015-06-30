package stateless4scala

import scala.reflect.runtime.universe._

import stateless4scala.triggers._

object StateConfiguration {
  private def NoGuard = true
}

class StateConfiguration[S, T](val representation: StateRepresentation[S, T], val lookup: S => StateRepresentation[S, T]) {
  require(representation != null, "representation is null")
  require(lookup != null, "lookup is null")

  /**
   * Accept the specified trigger and transition to the destination state
   *
   * @param trigger          The accepted trigger
   * @param destinationState The state that the trigger will cause a transition to
   * @return The reciever
   */
  def permit(trigger: T, destinationState: S): StateConfiguration[S, T] = {
    enforceNotIdentityTransition(destinationState)
    permitInternal(trigger, destinationState)
  }

  /**
   * Accept the specified trigger and transition to the destination state
   *
   * @param trigger          The accepted trigger
   * @param destinationState The state that the trigger will cause a transition to
   * @param guard            Function that must return true in order for the trigger to be accepted
   * @return The reciever
   */
  def permitIf(trigger: T, destinationState: S, guard: => Boolean): StateConfiguration[S, T] = {
    enforceNotIdentityTransition(destinationState)
    permitIfInternal(trigger, destinationState, guard)
  }

  /**
   * Accept the specified trigger, execute exit actions and re-execute entry actions. Reentry behaves as though the
   * configured state transitions to an identical sibling state
   * <p>
   * Applies to the current state only. Will not re-execute superstate actions, or  cause actions to execute
   * transitioning between super- and sub-states
   *
   * @param trigger The accepted trigger
   * @return The reciever
   */
  def permitReentry(trigger: T): StateConfiguration[S, T] =
    permitInternal(trigger, representation.getUnderlyingState())

  /**
   * Accept the specified trigger, execute exit actions and re-execute entry actions. Reentry behaves as though the
   * configured state transitions to an identical sibling state
   * <p>
   * Applies to the current state only. Will not re-execute superstate actions, or  cause actions to execute
   * transitioning between super- and sub-states
   *
   * @param trigger The accepted trigger
   * @param guard   Function that must return true in order for the trigger to be accepted
   * @return The reciever
   */
  def permitReentryIf(trigger: T, guard: => Boolean): StateConfiguration[S, T] =
    permitIfInternal(trigger, representation.getUnderlyingState(), guard)

  /**
   * ignore the specified trigger when in the configured state
   *
   * @param trigger The trigger to ignore
   * @return The receiver
   */
  def ignore(trigger: T): StateConfiguration[S, T] =
    ignoreIf(trigger, StateConfiguration.NoGuard)

  /**
   * ignore the specified trigger when in the configured state, if the guard returns true
   *
   * @param trigger The trigger to ignore
   * @param guard   Function that must return true in order for the trigger to be ignored
   * @return The receiver
   */
  def ignoreIf(trigger: T, guard: => Boolean): StateConfiguration[S, T] = {
    representation.addTriggerBehaviour(new IgnoredTriggerBehaviour[S, T](trigger, guard))
    this
  }

  /**
   * Specify an action that will execute when transitioning into the configured state
   *
   * @param entryAction Action to execute
   * @return The receiver
   */
  def onEntry(entryAction: => Unit): StateConfiguration[S, T] = {
    onEntry((x: Transition[S, T]) => entryAction)
  }

  /**
   * Specify an action that will execute when transitioning into the configured state
   *
   * @param entryAction Action to execute, providing details of the transition
   * @return The receiver
   */
  def onEntry(entryAction: Transition[S, T] => Unit): StateConfiguration[S, T] = {
    require(entryAction != null, "entryAction is null")
    representation.addEntryAction((t: Transition[S, T], args: Seq[(Type, Any)]) => entryAction(t))
    this
  }

  /**
   * Specify an action that will execute when transitioning into the configured state
   *
   * @param trigger     The trigger by which the state must be entered in order for the action to execute
   * @param entryAction Action to execute
   * @return The receiver
   */
  def onEntryFrom(trigger: T, entryAction: => Unit): StateConfiguration[S, T] = {
    onEntryFrom(trigger, (_: Transition[S, T]) => entryAction)
  }

  /**
   * Specify an action that will execute when transitioning into the configured state
   *
   * @param trigger     The trigger by which the state must be entered in order for the action to execute
   * @param entryAction Action to execute, providing details of the transition
   * @return The receiver
   */
  def onEntryFrom(trigger: T, entryAction: Transition[S, T] => Unit): StateConfiguration[S, T] = {
    require(entryAction != null, "entryAction is null")
    representation.addEntryAction(trigger, (t: Transition[S, T], args: Seq[(Type, Any)]) => entryAction(t))
    this
  }

  /**
   * Specify an action that will execute when transitioning into the configured state
   *
   * @param trigger     The trigger by which the state must be entered in order for the action to execute
   * @param entryAction Action to execute, providing details of the transition
   * @param classe0     Class argument
   * @tparam TArg0     Type of the first trigger argument
   * @return The receiver
   */
  def onEntryFrom[TArg0](trigger: TriggerWithParameters1[TArg0, S, T], entryAction: TArg0 => Unit): StateConfiguration[S, T] = {
    require(entryAction != null, "entryAction is null")
    onEntryFrom(trigger, (arg1: TArg0, t: Transition[S, T]) => entryAction(arg1))
  }

  /**
   * Specify an action that will execute when transitioning into the configured state
   *
   * @param trigger     The trigger by which the state must be entered in order for the action to execute
   * @param entryAction Action to execute, providing details of the transition
   * @param classe0     Class argument
   * @tparam TArg0     Type of the first trigger argument
   * @return The receiver
   */
  def onEntryFrom[TArg0](trigger: TriggerWithParameters1[TArg0, S, T], entryAction: (TArg0, Transition[S, T]) => Unit): StateConfiguration[S, T] = {
    require(trigger != null, "trigger is null")
    require(entryAction != null, "entryAction is null")
    representation.addEntryAction(
      trigger.underlyingTrigger,
      (t: Transition[S, T], args: Seq[(Type, Any)]) => entryAction(args(0)._2.asInstanceOf[TArg0], t))
    this
  }

  /**
   * Specify an action that will execute when transitioning into the configured state
   *
   * @param trigger     The trigger by which the state must be entered in order for the action to execute
   * @param entryAction Action to execute, providing details of the transition
   * @param classe0     Class argument
   * @param classe1     Class argument
   * @tparam TArg0     Type of the first trigger argument
   * @tparam TArg1     Type of the second trigger argument
   * @return The receiver
   */
  def onEntryFrom[TArg0, TArg1](trigger: TriggerWithParameters2[TArg0, TArg1, S, T], entryAction: (TArg0, TArg1) => Unit): StateConfiguration[S, T] = {
    require(entryAction != null, "entryAction is null")
    onEntryFrom(trigger, (arg1: TArg0, arg2: TArg1, t: Transition[S, T]) => entryAction(arg1, arg2))
  }

  /**
   * Specify an action that will execute when transitioning into the configured state
   *
   * @param trigger     The trigger by which the state must be entered in order for the action to execute
   * @param entryAction Action to execute, providing details of the transition
   * @param classe0     Class argument
   * @param classe1     Class argument
   * @tparam TArg0     Type of the first trigger argument
   * @tparam TArg1     Type of the second trigger argument
   * @return The receiver
   */
  def onEntryFrom[TArg0, TArg1](trigger: TriggerWithParameters2[TArg0, TArg1, S, T], entryAction: (TArg0, TArg1, Transition[S, T]) => Unit): StateConfiguration[S, T] = {
    require(trigger != null, "trigger is null")
    require(entryAction != null, "entryAction is null")
    representation.addEntryAction(
      trigger.underlyingTrigger,
      (t: Transition[S, T], args: Seq[(Type, Any)]) => entryAction(args(0)._2.asInstanceOf[TArg0], args(1)._2.asInstanceOf[TArg1], t))
    this
  }

  /**
   * Specify an action that will execute when transitioning into the configured state
   *
   * @param trigger     The trigger by which the state must be entered in order for the action to execute
   * @param entryAction Action to execute, providing details of the transition
   * @param classe0     Class argument
   * @param classe1     Class argument
   * @param classe2     Class argument
   * @tparam TArg0     Type of the first trigger argument
   * @tparam TArg1     Type of the second trigger argument
   * @tparam TArg2     Type of the third trigger argument
   * @return The receiver
   */
  def onEntryFrom[TArg0, TArg1, TArg2](
    trigger: TriggerWithParameters3[TArg0, TArg1, TArg2, S, T],
    entryAction: (TArg0, TArg1, TArg2) => Unit): StateConfiguration[S, T] = {
    require(entryAction != null, "entryAction is null")
    onEntryFrom(
      trigger,
      (a0: TArg0, a1: TArg1, a2: TArg2, _: Transition[S, T]) => entryAction(a0, a1, a2))
  }

  /**
   * Specify an action that will execute when transitioning into the configured state
   *
   * @param trigger     The trigger by which the state must be entered in order for the action to execute
   * @param entryAction Action to execute, providing details of the transition
   * @param classe0     Class argument
   * @param classe1     Class argument
   * @param classe2     Class argument
   * @tparam TArg0     Type of the first trigger argument
   * @tparam TArg1     Type of the second trigger argument
   * @tparam TArg2     Type of the third trigger argument
   * @return The receiver
   */
  def onEntryFrom[TArg0, TArg1, TArg2](
    trigger: TriggerWithParameters3[TArg0, TArg1, TArg2, S, T],
    entryAction: (TArg0, TArg1, TArg2, Transition[S, T]) => Unit): StateConfiguration[S, T] = {
    require(trigger != null, "trigger is null")
    require(entryAction != null, "entryAction is null")
    representation.addEntryAction(
      trigger.underlyingTrigger,
      (t: Transition[S, T], args: Seq[(Type, Any)]) => entryAction(
        args(0)._2.asInstanceOf[TArg0],
        args(1)._2.asInstanceOf[TArg1],
        args(2)._2.asInstanceOf[TArg2],
        t))
    this
  }

  /**
   * Specify an action that will execute when transitioning from the configured state
   *
   * @param exitAction Action to execute
   * @return The receiver
   */
  def onExit(exitAction: => Unit): StateConfiguration[S, T] = {
    return onExit(_ => exitAction)
  }

  /**
   * Specify an action that will execute when transitioning from the configured state
   *
   * @param exitAction Action to execute
   * @return The receiver
   */
  def onExit(exitAction: Transition[S, T] => Unit): StateConfiguration[S, T] = {
    require(exitAction != null, "exitAction is null")
    representation.addExitAction(exitAction)
    return this
  }

  /**
   * Sets the superstate that the configured state is a substate of
   * <p>
   * Substates inherit the allowed transitions of their superstate.
   * When entering directly into a substate from outside of the superstate,
   * entry actions for the superstate are executed.
   * Likewise when leaving from the substate to outside the supserstate,
   * exit actions for the superstate will execute.
   *
   * @param superstate The superstate
   * @return The receiver
   */
  def substateOf(superstate: S): StateConfiguration[S, T] = {
    val superRepresentation = lookup(superstate)
    representation.superstate = Some(superRepresentation)
    superRepresentation.addSubstate(representation)
    this
  }

  /**
   * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
   * function
   *
   * @param trigger                  The accepted trigger
   * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
   * @return The reciever
   */
  def permitDynamic(trigger: T, destinationStateSelector: => S): StateConfiguration[S, T] = {
    return permitDynamicIf(trigger, destinationStateSelector, StateConfiguration.NoGuard)
  }

  /**
   * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
   * function
   *
   * @param trigger                  The accepted trigger
   * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
   * @tparam TArg0                  Type of the first trigger argument
   * @return The receiver
   */
  def permitDynamic[TArg0](trigger: TriggerWithParameters1[TArg0, S, T], destinationStateSelector: (TArg0) => S): StateConfiguration[S, T] =
    permitDynamicIf[TArg0](trigger, destinationStateSelector, StateConfiguration.NoGuard)

  /**
   * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
   * function
   *
   * @param trigger                  The accepted trigger
   * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
   * @tparam TArg0                  Type of the first trigger argument
   * @tparam TArg1                  Type of the second trigger argument
   * @return The receiver
   */
  def permitDynamic[TArg0, TArg1](trigger: TriggerWithParameters2[TArg0, TArg1, S, T], destinationStateSelector: (TArg0, TArg1) => S): StateConfiguration[S, T] =
    permitDynamicIf[TArg0, TArg1](trigger, destinationStateSelector, StateConfiguration.NoGuard)

  /**
   * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
   * function
   *
   * @param trigger                  The accepted trigger
   * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
   * @tparam TArg0                  Type of the first trigger argument
   * @tparam TArg1                  Type of the second trigger argument
   * @tparam TArg2                  Type of the third trigger argument
   * @return The receiver
   */
  def permitDynamic[TArg0, TArg1, TArg2](trigger: TriggerWithParameters3[TArg0, TArg1, TArg2, S, T], destinationStateSelector: (TArg0, TArg1, TArg2) => S): StateConfiguration[S, T] =
    permitDynamicIf[TArg0, TArg1, TArg2](trigger, destinationStateSelector, StateConfiguration.NoGuard)

  /**
   * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
   * function
   *
   * @param trigger                  The accepted trigger
   * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
   * @param guard                    Function that must return true in order for the  trigger to be accepted
   * @return The reciever
   */
  def permitDynamicIf(trigger: T, destinationStateSelector: => S, guard: => Boolean): StateConfiguration[S, T] = {
    require(trigger != null, "trigger is null")
    require(destinationStateSelector != null, "destinationStateSelector is null")
    return permitDynamicIfInternal(
      trigger,
      _ => destinationStateSelector,
      guard)
  }

  /**
   * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
   * function
   *
   * @param trigger                  The accepted trigger
   * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
   * @param guard                    Function that must return true in order for the  trigger to be accepted
   * @tparam TArg0                  Type of the first trigger argument
   * @return The reciever
   */
  def permitDynamicIf[TArg0](trigger: TriggerWithParameters1[TArg0, S, T], destinationStateSelector: (TArg0) => S, guard: => Boolean): StateConfiguration[S, T] = {
    require(trigger != null, "trigger is null")
    require(destinationStateSelector != null, "destinationStateSelector is null")
    return permitDynamicIfInternal(
      trigger.underlyingTrigger,
      args => destinationStateSelector(args(0)._2.asInstanceOf[TArg0]),
      guard)
  }

  /**
   * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
   * function
   *
   * @param trigger                  The accepted trigger
   * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
   * @param guard                    Function that must return true in order for the  trigger to be accepted
   * @tparam TArg0                  Type of the first trigger argument
   * @tparam TArg1                  Type of the second trigger argument
   * @return The reciever
   */
  def permitDynamicIf[TArg0, TArg1](trigger: TriggerWithParameters2[TArg0, TArg1, S, T], destinationStateSelector: (TArg0, TArg1) => S, guard: => Boolean): StateConfiguration[S, T] = {
    require(trigger != null, "trigger is null")
    require(destinationStateSelector != null, "destinationStateSelector is null")
    return permitDynamicIfInternal(
      trigger.underlyingTrigger,
      args => destinationStateSelector(
        args(0)._2.asInstanceOf[TArg0],
        args(1)._2.asInstanceOf[TArg1]),
      guard)
  }

  /**
   * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
   * function
   *
   * @param trigger                  The accepted trigger
   * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
   * @param guard                    Function that must return true in order for the  trigger to be accepted
   * @tparam TArg0                  Type of the first trigger argument
   * @tparam TArg1                  Type of the second trigger argument
   * @tparam TArg2                  Type of the third trigger argument
   * @return The reciever
   */
  def permitDynamicIf[TArg0, TArg1, TArg2](trigger: TriggerWithParameters3[TArg0, TArg1, TArg2, S, T], destinationStateSelector: (TArg0, TArg1, TArg2) => S, guard: => Boolean): StateConfiguration[S, T] = {
    require(trigger != null, "trigger is null")
    require(destinationStateSelector != null, "destinationStateSelector is null")
    permitDynamicIfInternal(
      trigger.underlyingTrigger,
      args => destinationStateSelector(
        args(0)._2.asInstanceOf[TArg0],
        args(1)._2.asInstanceOf[TArg1],
        args(2)._2.asInstanceOf[TArg2]),
      guard)
  }

  def enforceNotIdentityTransition(destination: S): Unit = {
    if (destination.equals(representation.getUnderlyingState())) {
      throw new IllegalStateException("Permit() (and PermitIf()) require that the destination state is not equal to the source state. To accept a trigger without changing state, use either Ignore() or PermitReentry().")
    }
  }

  private def permitInternal(trigger: T, destinationState: S): StateConfiguration[S, T] =
    permitIfInternal(trigger, destinationState, StateConfiguration.NoGuard)

  private def permitIfInternal(trigger: T, destinationState: S, guard: => Boolean): StateConfiguration[S, T] = {
    representation.addTriggerBehaviour(new TransitioningTriggerBehaviour[S, T](trigger, destinationState, guard))
    this
  }

  private def permitDynamicInternal(trigger: T, destinationStateSelector: (Seq[(Type, Any)]) => S): StateConfiguration[S, T] =
    permitDynamicIfInternal(trigger, destinationStateSelector, StateConfiguration.NoGuard)

  private def permitDynamicIfInternal(trigger: T, destinationStateSelector: (Seq[(Type, Any)]) => S, guard: => Boolean): StateConfiguration[S, T] = {
    require(destinationStateSelector != null, "destinationStateSelector is null")
    representation.addTriggerBehaviour(new DynamicTriggerBehaviour(trigger, destinationStateSelector, guard))
    this
  }
}
