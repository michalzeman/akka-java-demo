package com.zemo.akka.demo.ping.pong;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

/**
 * Created by zemi on 05/06/2017.
 */
public class AkkaPingPongApp {

  public static void main(String[] ags) {
    final ActorSystem system = ActorSystem.create("ping-pong-akka");
    try {
      final ActorRef actorA = system.actorOf(PingPongActor.props(), "ActorA");
      final ActorRef actorB = system.actorOf(PingPongActor.props(), "ActorB");
      actorA.tell(new PingPongActor.StartMsg(actorB), ActorRef.noSender());
      System.out.println(">>> Press ENTER to exit <<<");
      System.in.read();
    } catch (Exception exp) {

    } finally {
      system.terminate();
    }
  }
}
