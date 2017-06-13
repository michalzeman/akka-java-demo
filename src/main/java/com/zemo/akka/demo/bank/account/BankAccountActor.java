package com.zemo.akka.demo.bank.account;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.math.BigDecimal;

/**
 * Created by zemi on 09/06/2017.
 */
public class BankAccountActor extends AbstractActor {

  public static Props props(String id) {
    return Props.create(BankAccountActor.class, id);
  }

  static public class DepositMsg {
    public final BigDecimal amount;

    public DepositMsg(BigDecimal amount) {
      this.amount = amount;
    }
  }

  static public class WithdrawMsg {
    public final BigDecimal amount;

    public WithdrawMsg(BigDecimal amount) {
      this.amount = amount;
    }
  }

  static public class DoneMsg {}

  static public class FailedMsg {}

  private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

  private final String id;

  private BigDecimal balance = new BigDecimal(0);

  public BankAccountActor(String id) {
    this.id = id;
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(DepositMsg.class, depositMsg -> {
          log.info(this.self().path().name()+ "-> add new deposit: "+depositMsg.amount);
          updateBalance(depositMsg.amount);
        })
        .match(WithdrawMsg.class, withdrawMsg -> {
          log.info(this.self().path().name()+ "-> withdraw amount: "+withdrawMsg.amount);
          updateBalance(withdrawMsg.amount.negate());
        })
        .match(DoneMsg.class, doneMsg -> {
          log.info(this.self().path().name()+ "-> operation done, actor is going to stop");
          getContext().stop(getSelf());
        })
        .build();
  }

  /**
   * Update balance of bank account
   * @param amount
   */
  private void updateBalance(BigDecimal amount) {

  }

}
