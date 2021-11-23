package part2Actors

import akka.actor.Status.Success
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

//  alice ! WirelessPhoneMessage("Hi!", ref = bob)

  // 1. create a counter actor
  // internal int counter
  // increment, decrement, print messages
  case class Increment(amount: Int)
  case class Decrement(amount: Int)
  case object Print

  class Counter extends Actor {
    var counter = 0
    override def receive: Receive = {
      case Increment(amount) => counter += amount
      case Decrement(amount) => counter -= amount
      case Print => println(s"[Counter Actor] the current count is $counter")
    }
  }

  val counterActor = system.actorOf(Props[Counter], "counter")

  counterActor ! Increment(125)

  counterActor ! Decrement(100)

  counterActor ! Print

  // 2. Bank acc as an Actor
  /*
  *  Deposit an amount, replying with Success, Failure messages
  * Withdraw an amount
  * Statement
  * interact with some other kind of actor
  * */

  case class Deposit(id: Int, amount: Int)
  case class Withdraw(id: Int, amount: Int)
  case object Statement
  case class Success(id: Int)
  case class Failure(id: Int)

  class Client(ref: ActorRef) extends Actor {
    override def receive: Receive = {
      case Deposit(id, amount) => ref ! Deposit(id, amount)
      case Withdraw(id, amount) => ref ! Withdraw(id, amount)
      case Success(id) => println(s"[Client Actor] The transaction: $id was Successfull")
      case Failure(id) => println(s"[Client Actor] The transaction: $id Failed")
      case Statement => ref ! Statement
      case amount: Int => println(s"[Client Actor] The amount on the account is $amount")
    }
  }

  class Bank extends Actor {
    var account = 0
    override def receive: Receive = {
      case Deposit(id, amount) => {
        if (amount < 0) {
          sender() ! Failure(id)
        } else {
          account += amount
          sender() ! Success(id)
        }
      }
      case Withdraw(id, amount) => {
        if (amount > account) {
          sender() ! Failure(id)
        } else {
          account -= amount
          sender() ! Success(id)
        }
      }
      case Statement => sender() ! account
    }
  }

  val bank = system.actorOf(Props[Bank], "bankActor")

  val clientProps = Props(new Client(bank))

  val client = system.actorOf(clientProps, "clientActor")

  client ! Deposit(1, 1000)

  client ! Deposit(2, 2000)

  client ! Withdraw(3, 1000)

  client ! Statement

  client ! Withdraw(4, 1000)

  client ! Withdraw(5, 1000)

  client ! Withdraw(6, 1000)

}
