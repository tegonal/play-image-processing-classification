package plugins

import akka.actor.ActorRef
import org.opencv.core.Mat
import akka.actor.Actor

import Plugin._
import akka.actor.PoisonPill

object Plugin {
  case class Image(image: Mat)

  case class Consumers(consumers: Set[ActorRef])
}

/**
 * A plugin is an actor which receives Consumers or Image messages.
 * The handling of the Image messages is implemented in the concrete subclasses.
 */
trait Plugin extends Actor {
  var consumers = Set.empty[ActorRef]

  def handleImageMessage(image: Image)

  def receive = {
    case Consumers(cs) =>
      cs foreach { c =>
        if (c != self) {
          if (!consumers.contains(c)) {
            consumers += c
          }
        }
      }

    case image @ Image(mat) =>
      handleImageMessage(image)
  }
}