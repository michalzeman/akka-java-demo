package com.zemo.akka.demo.bank.account;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Created by zemi on 10/06/2017.
 */
public class BankTransactionActorTest {
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
  public void testTransactionSuccess() {
    final TestKit testProbe = new TestKit(system);

    final ActorRef bankAccountFrom = system.actorOf(BankAccountActor.props("test_account_from_131"), "bankAccountFrom");
    final ActorRef bankAccountTo = system.actorOf(BankAccountActor.props("test_account_to_132"), "bankAccountTo");
    final ActorRef bankTransaction = system.actorOf(BankTransactionActor.props(), "bankTransactionSuccess");

    bankAccountFrom.tell(new BankAccountActor.DepositMsg(BigDecimal.valueOf(100l)), testProbe.getRef());
    testProbe.expectMsgClass(BankAccountActor.DoneMsg.class);

    bankTransaction.tell(new BankTransactionActor.TransactionMsg(bankAccountFrom, bankAccountTo, BigDecimal.valueOf(40l)), testProbe.getRef());
    testProbe.expectMsgClass(BankTransactionActor.DoneMsg.class);
  }

  @Test
  public void testTransactionFail() {
    final TestKit testProbe = new TestKit(system);

    final TestKit bankAccountFrom = new TestKit(system);
    final TestKit bankAccountTo = new TestKit(system);
    final ActorRef bankTransaction = system.actorOf(BankTransactionActor.props(), "bankTransactionSuccess");

    bankTransaction.tell(new BankTransactionActor.TransactionMsg(bankAccountFrom.getRef(), bankAccountTo.getRef(), BigDecimal.valueOf(40l)), testProbe.getRef());
    bankAccountFrom.expectMsgClass(BankAccountActor.WithdrawMsg.class);

    bankTransaction.tell(new BankAccountActor.DoneMsg(), bankAccountFrom.getRef());
    BankAccountActor.DepositMsg depositMsgTo = (BankAccountActor.DepositMsg)bankAccountTo.expectMsgClass(BankAccountActor.DepositMsg.class);
    Assert.assertTrue(depositMsgTo.amount.equals(BigDecimal.valueOf(40l)));

    bankTransaction.tell(new BankAccountActor.FailedMsg(), bankAccountTo.getRef());
    BankAccountActor.DepositMsg depositMsg = (BankAccountActor.DepositMsg) bankAccountFrom.expectMsgClass(BankAccountActor.DepositMsg.class);
    Assert.assertTrue(depositMsg.amount.equals(BigDecimal.valueOf(40l)));
  }
}
