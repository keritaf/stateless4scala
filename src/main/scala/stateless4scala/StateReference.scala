package stateless4scala

class StateReference[State] {
  private var internalState: State = _

  def state_=(state: State): Unit = {
    internalState = state
  }

  def state: State = internalState
}
