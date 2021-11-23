package part2Actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi!" => sender() ! "Hello there!" // replying to a message
      case message: String => println(s"[${self.path}] I have received $message from ${context.sender()}")
      case number: Int => println(s"[simple actor] I have received an number $number")
      case SpecialMessage(content) => println(s"[simple actor] I have received something special $content")
      case SendMessageToYourself(content) => self ! content
      case SayHiTo(ref) => ref ! "Hi!"
      case WirelessPhoneMessage(content, ref) => ref forward (content + "s") // I keep the original sender
    }
  }

  val system = ActorSystem("ActorCapabilitiesDemo")
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")

//  simpleActor ! "hello, actor!"

  // 1 - message can be of any type
  // a ) messages must be IMMUTABLE
  // b) messages must be serializable --> transform into SERIALIZABLE

  // in practice use case class and objects

//  simpleActor ! 42 // who is the sender ?

  case class SpecialMessage(contents: String)

//  simpleActor ! SpecialMessage("some special content")

  // 2 - actors have information about their context and about themselves

  // context.self === 'this' in OOP

  case class SendMessageToYourself(content: String)

//  simpleActor ! SendMessageToYourself("I am an Actor, and I am proud of it")

  val alice = system.actorOf(Props[SimpleActor], "alice")

  val bob = system.actorOf(Props[SimpleActor], "bob")

  case class SayHiTo(ref: ActorRef)

//  alice ! SayHiTo(bob)

  // dead letters
//  alice ! "Hi!"

  // 5 - forwarding messages
  // forwarding = sending a message with the original sender

  case class WirelessPhoneMessage(content: String, ref: ActorRef)

  alice ! WirelessPhoneMessage("Hi!", ref = bob)

}
