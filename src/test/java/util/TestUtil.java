package util;

import dataentities.FinancialAddress;
import dataentities.Payout;

import java.time.LocalDateTime;
import java.time.Month;

public class TestUtil {

    static FinancialAddress financialAddress = new FinancialAddress("MSISDN", "256780334452");
    public static Payout payoutRequestCreationRejected = new Payout("1", "15.21", "ZMW", financialAddress, "MTN_MOMO_ZMM", "ZMB",
            "Payout #123", LocalDateTime.of(2000, Month.MARCH, 9, 17, 30));
    public static Payout payoutRequestCreationAcceptedFinalCompleted = new Payout("2", "15.21", "ZMW", financialAddress, "MTN_MOMO_ZMB", "ZMB",
            "Payout #123", LocalDateTime.of(2000, Month.MARCH, 9, 17, 33));
    public static  Payout payoutRequestCreationDuplicateIgnored = new Payout("2", "15.21", "ZMW", financialAddress, "MTN_MOMO_ZMB", "ZMB",
            "Payout #123", LocalDateTime.of(2000, Month.MARCH, 9, 17, 33));
    public static  Payout payoutRequestCreationUnknownError = new Payout("4", "15.21", "ZMW", financialAddress, "MTN_MOMO_ZMB", "ZMB",
            "Payout #123", LocalDateTime.of(2000, Month.MARCH, 9, 17, 40));
    public static  Payout payoutRequestCreationAcceptedFinalCancelled = new Payout("5", "15.21", "ZMW", financialAddress, "MTN_MOMO_ZMB", "ZMB",
            "Payout #123", LocalDateTime.of(2000, Month.MARCH, 9, 17, 45));
    public static  Payout payoutRequestCreationAcceptedFinalFailed = new Payout("6", "15.21", "ZMW", financialAddress, "MTN_MOMO_ZMB", "ZMB",
            "Payout #123", LocalDateTime.of(2000, Month.MARCH, 9, 17, 55));
    public static  Payout payoutRequestCreationAcceptedPending = new Payout("7", "15.21", "ZMW", financialAddress, "MTN_MOMO_ZMB", "ZMB",
            "Payout #123", LocalDateTime.of(2000, Month.MARCH, 9, 17, 57));
    public static  Payout payoutRequestCreationAcceptedSubmitted = new Payout("8", "15.21", "ZMW", financialAddress, "MTN_MOMO_ZMB", "ZMB",
            "Payout #123", LocalDateTime.of(2000, Month.MARCH, 9, 17, 59));
    public static  Payout payoutUnknownError = new Payout("9", "15.21", "ZMW", financialAddress, "MTN_MOMO_ZMB", "ZMB",
            "Payout #123", LocalDateTime.of(2000, Month.MARCH, 9, 18, 00));
}
