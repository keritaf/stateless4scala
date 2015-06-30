package example

import stateless4scala.StateMachine
import stateless4scala.StateMachineConfig
import org.junit.Test
import org.junit.Assert._
import org.slf4j.LoggerFactory

class PhoneCall {
  implicit val loggerFactory = LoggerFactory.getILoggerFactory

  @Test
  def testPhoneRings(): Unit = {
    val phoneCallConfig = new StateMachineConfig[State.Type, Trigger.Type]

    phoneCallConfig.configure(State.OffHook)
      .permit(Trigger.CallDialed, State.Ringing)

    phoneCallConfig.configure(State.Ringing)
      .permit(Trigger.HungUp, State.OffHook)
      .permit(Trigger.CallConnected, State.Connected)

    phoneCallConfig.configure(State.Connected)
      .onEntry(startCallTimer)
      .onExit(stopCallTimer)
      .permit(Trigger.LeftMessage, State.OffHook)
      .permit(Trigger.HungUp, State.OffHook)
      .permit(Trigger.PlacedOnHold, State.OnHold)

    // ...

    val phoneCall = new StateMachine[State.Type, Trigger.Type](State.OffHook, phoneCallConfig)

    phoneCall.fire(Trigger.CallDialed)
    assertEquals(State.Ringing, phoneCall.currentState())
  }

  private def stopCallTimer() {
    // ...
  }

  private def startCallTimer() {
    // ...
  }

  private object State extends Enumeration {
    type Type = Value
    val Ringing, Connected, OnHold, OffHook = Value
  }

  private object Trigger extends Enumeration {
    type Type = Value
    val CallDialed, CallConnected, PlacedOnHold, LeftMessage, HungUp = Value
  }
}
