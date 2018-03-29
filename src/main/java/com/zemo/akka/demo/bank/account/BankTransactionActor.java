package com.zemo.akka.demo.bank.account;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.dsl.Creators;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.math.BigDecimal;

/**
 * Created by zemi on 10/06/2017.
 */
public class BankTransactionActor extends AbstractActor {

  public static Props props() {
    return Props.create(BankTransactionActor.class);
  }

  static class TransactionMsg {
    public final ActorRef from;
    public final ActorRef to;
    public final BigDecimal amount;

    public TransactionMsg(ActorRef from, ActorRef to, BigDecimal amount) {
      this.from = from;
      this.to = to;
      this.amount = amount;
    }
  }

  static public class DoneMsg {}

  static public class FailedMsg {}

  private ActorRef sender;

  private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(TransactionMsg.class,
            transactionMsg -> transactionMsg.from != null && transactionMsg.to != null,
            transactionMsg -> {
              log.info(this.self().path().name()+ "-> TransactionMsg start, amount: "+transactionMsg.amount);
              sender = getSender();
              transactionMsg.from.tell(new BankAccountActor.WithdrawMsg(transactionMsg.amount), getSelf());
              getContext().become(awaitFrom(transactionMsg.from, transactionMsg.to, transactionMsg.amount));
            })
        .build();
  }

  private Receive awaitFrom(ActorRef from, ActorRef to,BigDecimal amount) {
    return receiveBuilder()
        .match(BankAccountActor.DoneMsg.class, doneMsg -> {
          log.info(this.self().path().name()+ "-> awaitFrom Done!");
          to.tell(new BankAccountActor.DepositMsg(amount), getSelf());
          getContext().become(awaitTo(from, to, amount));
        })
        .match(BankAccountActor.FailedMsg.class, failedMsg -> {
          log.info(this.self().path().name()+ "-> awaitFrom Failed!");
          getContext().stop(getSelf());
        })
        .build();
  }

  private Receive awaitTo(ActorRef from, ActorRef to,BigDecimal amount) {
    return receiveBuilder()
        .match(BankAccountActor.DoneMsg.class, doneMsg -> {
          log.info(this.self().path().name()+ "-> awaitTo Done!");
          from.tell(new BankAccountActor.DoneMsg(), getSelf());
          sender.tell(new DoneMsg(), getSelf());
          getContext().stop(getSelf());
        })
        .match(BankAccountActor.FailedMsg.class, failedMsg -> {
          log.info(this.self().path().name()+ "-> awaitFrom failed!");
          from.tell(new BankAccountActor.DepositMsg(amount), getSelf());
          sender.tell(new FailedMsg(), getSelf());
          getContext().stop(getSelf());
        })
        .build();
  }
}
