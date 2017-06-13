package com.zemo.akka.demo.bank.account;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Created by zemi on 09/06/2017.
 */
public class BankAccountActorTest {

  static ActorSystem system;

  @BeforeClass
  public static void setup() {
    system = ActorSystem.create();
  }

  @AfterClass
  public static void teardown() {
    TestKit.shutdownActorSystem(system);
    system = null;
  }

  @Test
  public void testWithdraw() {
    final ActorRef bankAccount = system.actorOf(BankAccountActor.props("test_account_131"), "testBankAccountWithdraw");
    final TestKit testProbe = new TestKit(system);

    bankAccount.tell(new BankAccountActor.DepositMsg(new BigDecimal(100)), testProbe.getRef());
    testProbe.expectMsgClass(BankAccountActor.DoneMsg.class);

    bankAccount.tell(new BankAccountActor.WithdrawMsg(new BigDecimal(50)), testProbe.getRef());
    testProbe.expectMsgClass(BankAccountActor.DoneMsg.class);

    bankAccount.tell(new BankAccountActor.WithdrawMsg(new BigDecimal(60)), testProbe.getRef());
    testProbe.expectMsgClass(BankAccountActor.FailedMsg.class);
  }
}
