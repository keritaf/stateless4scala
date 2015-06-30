package stateless4scala

import scala.reflect.runtime.universe._

import org.slf4j.ILoggerFactory

import triggers._

/**
 * Models behaviour as transitions between a finite set of states
 *
 * @param <S> The type used to represent the states
 * @param <T> The type used to represent the triggers that cause state transitions
 * @param initialState The initial state
 * @param config       State machine configuration
 */
class StateMachine[S, T](
    initialState: S,
    protected val stateAccessor: () => S,
    protected val stateMutator: S => Unit,
    protected val config: StateMachineConfig[S, T] = new StateMachineConfig[S, T])(implicit val loggerFactory: ILoggerFactory) {

  protected val logger = loggerFactory.getLogger(classOf[StateMachine[S, T]].getName)

  stateMutator(initialState)
  if (config.isInitialStateEntryActionEnabled)
    currentRepresentation.enter(new Transition(initialState, initialState, null.asInstanceOf[T]))

  protected var unhandledTriggerAction: (S, T) => Unit = (state: S, trigger: T) => {
    throw new IllegalStateException(s"No valid leaving transitions are permitted from state '$state' for trigger '$trigger'. Consider ignoring the trigger.")
  }

  def this(
    initialState: S,
    reference: StateReference[S],
    config: StateMachineConfig[S, T])(implicit loggerFactory: ILoggerFactory) {
    this(initialState, () => reference.state, state => reference.state = state, config)
  }

  def this(initialState: S, config: StateMachineConfig[S, T])(implicit loggerFactory: ILoggerFactory) {
    this(initialState, new StateReference[S], config)
  }

  def this(initialState: S)(implicit loggerFactory: ILoggerFactory) {
    this(initialState, new StateReference[S], new StateMachineConfig[S, T])
  }

  def configure(state: S): StateConfiguration[S, T] = config.configure(state)

  /**
   * Current configuration
   *
   * @return Current configuration
   */
  def currentConfiguration: StateMachineConfig[S, T] = config

  /**
   * The current state
   *
   * @return The current state
   */
  def currentState: S = stateAccessor()

  protected def currentState_=(state: S): Unit = stateMutator(state)

  /**
   * The currently-permissible trigger values
   *
   * @return The currently-permissible trigger values
   */
  def permittedTriggers = currentRepresentation.permittedTriggers

  protected def currentRepresentation: StateRepresentation[S, T] = {
    config.getRepresentation(currentState) getOrElse new StateRepresentation[S, T](currentState)
  }

  /**
   * Transition from the current state via the specified trigger.
   * The target state is determined by the configuration of the current state.
   * Actions associated with leaving the current state and entering the new one
   * will be invoked
   *
   * @param trigger The trigger to fire
   */
  def fire(trigger: T): Unit = fireInternal(trigger)

  /**
   * Transition from the current state via the specified trigger.
   * The target state is determined by the configuration of the current state.
   * Actions associated with leaving the current state and entering the new one
   * will be invoked.
   *
   * @param trigger The trigger to fire
   * @param arg0    The first argument
   * @tparam TArg0 Type of the first trigger argument
   */
  def fire[TArg0: TypeTag](trigger: TriggerWithParameters1[TArg0, S, T], arg0: TArg0): Unit = {
    require(trigger != null, "trigger is null")
    fireInternal(
      trigger.underlyingTrigger,
      (typeOf[TArg0], arg0))
  }

  /**
   * Transition from the current state via the specified trigger.
   * The target state is determined by the configuration of the current state.
   * Actions associated with leaving the current state and entering the new one
   * will be invoked.
   *
   * @param trigger The trigger to fire
   * @param arg0    The first argument
   * @param arg1    The second argument
   * @tparam TArg0 Type of the first trigger argument
   * @tparam TArg1 Type of the second trigger argument
   */
  def fire[TArg0: TypeTag, TArg1: TypeTag](trigger: TriggerWithParameters2[TArg0, TArg1, S, T], arg0: TArg0, arg1: TArg1): Unit = {
    require(trigger != null, "trigger is null")
    fireInternal(
      trigger.underlyingTrigger,
      (typeOf[TArg0], arg0),
      (typeOf[TArg1], arg1))
  }

  /**
   * Transition from the current state via the specified trigger.
   * The target state is determined by the configuration of the current state.
   * Actions associated with leaving the current state and entering the new one
   * will be invoked.
   *
   * @param trigger The trigger to fire
   * @param arg0    The first argument
   * @param arg1    The second argument
   * @param arg2    The third argument
   * @tparam TArg0 Type of the first trigger argument
   * @tparam TArg1 Type of the second trigger argument
   * @tparam TArg2 Type of the third trigger argument
   */
  def fire[TArg0: TypeTag, TArg1: TypeTag, TArg2: TypeTag](trigger: TriggerWithParameters3[TArg0, TArg1, TArg2, S, T], arg0: TArg0, arg1: TArg1, arg2: TArg2): Unit = {
    require(trigger != null, "trigger is null")
    fireInternal(
      trigger.underlyingTrigger,
      (typeOf[TArg0], arg0),
      (typeOf[TArg1], arg1),
      (typeOf[TArg2], arg2))
  }

  def fire[TArg0: TypeTag, TArg1: TypeTag, TArg2: TypeTag, TArg3: TypeTag](trigger: TriggerWithParameters4[TArg0, TArg1, TArg2, TArg3, S, T], arg0: TArg0, arg1: TArg1, arg2: TArg2, arg3: TArg3): Unit = {
    require(trigger != null, "trigger is null")
    fireInternal(
      trigger.underlyingTrigger,
      (typeOf[TArg0], arg0),
      (typeOf[TArg1], arg1),
      (typeOf[TArg2], arg2),
      (typeOf[TArg3], arg3))
  }

  protected def fireInternal(trigger: T, args: (Type, Any)*) {
    logger.debug("Firing " + trigger)
    config.getTriggerConfiguration(trigger).foreach(_.validateParameters(args: _*))

    val triggerBehaviour = currentRepresentation.tryFindHandler(trigger)
    triggerBehaviour match {
      case None => unhandledTriggerAction.apply(currentRepresentation.getUnderlyingState(), trigger)
      case Some(t) =>
        val source = currentState
        val destination = new OutVar[S]
        if (t.resultsInTransitionFrom(source, args, destination)) {
          val transition = new Transition[S, T](source, destination.value, trigger)

          currentRepresentation.exit(transition)
          currentState = destination.value
          currentRepresentation.enter(transition, args: _*)
        }
    }
  }

  /**
   * Override the default behaviour of throwing an exception when an unhandled trigger is fired
   *
   * @param unhandledTriggerAction An action to call when an unhandled trigger is fired
   */
  def onUnhandledTrigger(unhandledTriggerAction: (S, T) => Unit) {
    if (unhandledTriggerAction == null) {
      throw new IllegalStateException("unhandledTriggerAction")
    }
    this.unhandledTriggerAction = unhandledTriggerAction
  }

  /**
   * Determine if the state machine is in the supplied state
   *
   * @param state The state to test for
   * @return True if the current state is equal to, or a substate of, the supplied state
   */
  def isInState(state: S) = currentRepresentation.isIncludedIn(state)

  /**
   * Returns true if {@code trigger} can be fired  in the current state
   *
   * @param trigger Trigger to test
   * @return True if the trigger can be fired, false otherwise
   */
  def canFire(trigger: T) = currentRepresentation.canHandle(trigger)

  /**
   * A human-readable representation of the state machine
   *
   * @return A description of the current state and permitted triggers
   */
  override def toString(): String = s"StateMachine {{ State = ${currentState}, PermittedTriggers = {{ ${permittedTriggers.map(_.toString()).mkString(", ")} }}}}"
}
